package com.apushnikov.other351_mytelegram.ui.screens.register;

import static com.apushnikov.other351_mytelegram.myApplication.MyApplication.logFile;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.NODE_USERS;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.auth;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_database_root;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.apushnikov.other351_mytelegram.MainActivity;
import com.apushnikov.other351_mytelegram.R;
import com.apushnikov.other351_mytelegram.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**======================================================================================
 * EnterEMailFragment - фрагмент регистрации или входа по E-mail
 */
public class EnterEMailFragment extends Fragment {

    // TODO: Необходимо хорошенько протестироваь модуль входа!!!!

    private EditText edEMail;
    private EditText edPassword;
    private Button btnSingUp;
    private Button btnSingIn;

    /**=================================================================================
     * EnterPhoneNamberFragment - Требуется пустой публичный конструктор
     */
    public EnterEMailFragment() {
        // Required empty public constructor
    }

    /**=================================================================================
     * Используйте этот фабричный метод для создания нового экземпляра этого фрагмента
     * с использованием предоставленных параметров.
     *
     * @return Новый экземпляр фрагмента EnterEMailFragment.
     */
    public static EnterEMailFragment newInstance() {

        EnterEMailFragment fragment = new EnterEMailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    /**=================================================================================
     * onCreate
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    /**=================================================================================
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_enter_e_mail, container, false);
    }

    /**=================================================================================
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();

        // Инициализация переменных
        edEMail = requireView().findViewById(R.id.edEMail);
        edPassword = requireView().findViewById(R.id.edPassword);
        btnSingUp = requireView().findViewById(R.id.btnSingEMail);
        btnSingIn = requireView().findViewById(R.id.btnSingPhone);

        // Навешивание реагирования на кнопку
        btnSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singUp();
            }
        });

        // Навешивание реагирования на кнопку
        btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singIn();
            }
        });

    }

    /**=================================================================================
     * singUp - регистрация пользователя по e-mail и паролю
     */
    private void singUp() {

        // Запоминаем текущий логин и пароль
        String currentEMail = edEMail.getText().toString().trim();
        String currentPassword = edPassword.getText().toString().trim();

        // Если логин и пароль не пусты
        if (!TextUtils.isEmpty(currentEMail) && !TextUtils.isEmpty(currentPassword)) {
            // Создаем нового пользователя по EMail и паролю
            auth.createUserWithEmailAndPassword(currentEMail, currentPassword).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Пользователь успешно создался
                            if (task.isSuccessful()) {
                                // Берем текущего пользователя
                                FirebaseUser user = auth.getCurrentUser();
                                // Запускаем проверку реальности e-mail
                                assert user != null;
                                sendEmailVerification(user, currentEMail);
                            }
                            // Пользователь НЕ создался
                            else {
                                Toast.makeText(getActivity(), getString(R.string.register_toast_user_NOT_registered), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
        // Если логин и пароль пусты
        else {
            Toast.makeText(getActivity(), getString(R.string.register_toast_enter_email_and_password), Toast.LENGTH_SHORT).show();
        }

    }

    /**=================================================================================
     * sendEmailVerification - проверка реальность e-mail
     */
    private void sendEmailVerification(FirebaseUser user, String eMail) {

        Toast.makeText(getActivity(), getString(R.string.register_toast_check_your_email_and_confirm_your_email), Toast.LENGTH_SHORT).show();
        // sendEmailVerification - запускаем проверку e-mail
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Если проверка e-mail успешна
                if (task.isSuccessful()) {

                    //TODO: НЕ ПОЛУЧАЕТСЯ В оригинале используется updateChildren(dateMap) и MutableMap
                    // 12. Работа с базой данных Firebase. Создаем первые ноды. Пишем Telegram для Android на Kotlin.
/*                                Map<String, Object> dateMap = new M
                                ref_database_root.child(NODE_USERS).child(uid).updateChildren();*/

                    // Заполняем данные пользователя
                    // Определяем uid текущего пользователя
                    String id = Objects.requireNonNull(user).getUid();
                    String username = id;
                    String phone = "";
                    String bio = "";
                    String email = eMail;
                    String fullmane = "";
                    String status = "";
                    String photoUrl = "";
                    // Создаем нового пользователя
                    User newUser = new User(
                            id,
                            username,
                            phone,
                            bio,
                            email,
                            fullmane,
                            status,
                            photoUrl);
                    // Помещаем пользователя в базу данных
                    ref_database_root.child(NODE_USERS).child(id).setValue(newUser).
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task2) {
                                    // Усли размещение пользователя в базу данных прошло успешно
                                    if (task2.isSuccessful()) {
                                        //TODO: Фрагмент ниже повторяет фрагмен из EnterCodeFragment.java
                                        // надо не запускать новый интент, а возвращаться к
                                        // созданному MainActivity
                                        Toast.makeText(getActivity(), "Добро пожаловать!", Toast.LENGTH_SHORT).show();
                                        // Запускаем MainActivity
                                        // TODO: по-моему, это неправильный вызов!!!!!
                                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    }
                                    // Усли размещение пользователя в базу данных НЕ прошло успешно
                                    else {
                                        Toast.makeText(getActivity(), Objects.requireNonNull(task2.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                // Если проверка e-mail НЕ успешна
                else {
                    Toast.makeText(getActivity(), getString(R.string.register_toast_mail_check_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**=================================================================================
     * singIn - вход пользователя по e-mail и паролю
     */
    private void singIn() {

        // Запоминаем текущий логин и пароль
        String currentEMail = edEMail.getText().toString().trim();
        String currentPassword = edPassword.getText().toString().trim();

        // Если логин и пароль не пусты
        if (!TextUtils.isEmpty(currentEMail) && !TextUtils.isEmpty(currentPassword)) {
            // Пытаемся войти по EMail и паролю
            auth.signInWithEmailAndPassword(currentEMail, currentPassword).
                    addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Если пользователь успешно вошел
                            if (task.isSuccessful()) {
                                //TODO: Фрагмент ниже повторяет фрагмен из EnterCodeFragment.java
                                // надо не запускать новый интент, а возвращаться к
                                // созданному MainActivity
                                Toast.makeText(getActivity(), "Добро пожаловать!", Toast.LENGTH_SHORT).show();
                                // Запускаем MainActivity
                                // TODO: по-моему, это неправильный вызов!!!!!
                                Intent intent = new Intent(requireActivity(), MainActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            }
                            // Если пользователь НЕ вошел
                            else {
//                                notSigned();
                                Toast.makeText(getActivity(), "Пользователь НЕ вошел", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }


}