package com.apushnikov.other351_mytelegram.ui.screens.register;

import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.auth;
import static com.google.firebase.auth.PhoneAuthProvider.*;
import static com.google.firebase.auth.PhoneAuthProvider.getInstance;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.apushnikov.other351_mytelegram.MainActivity;
import com.apushnikov.other351_mytelegram.R;
//import com.apushnikov.other351_mytelegram.databinding.FragmentEnterPhoneNamberBinding;
//import com.apushnikov.other351_mytelegram.databinding.FragmentSettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**======================================================================================
 * EnterPhoneNamberFragment - фрагмент ввода телефонного номера
 */
public class EnterPhoneNamberFragment extends Fragment {

    private EditText register_input_phone_number;
    private FloatingActionButton register_btn_next;

    //Номер телефона
    private String phoneNumber;
    /** mCallback - обратный вызов*/
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    /**=================================================================================
     * EnterPhoneNamberFragment - Требуется пустой публичный конструктор
     */
    public EnterPhoneNamberFragment() {
        // Требуется пустой публичный конструктор
    }

    /**=================================================================================
     * Используйте этот фабричный метод для создания нового экземпляра этого фрагмента
     * с использованием предоставленных параметров.
     *
     * @return Новый экземпляр фрагмента EnterPhoneNamberFragment.
     */
    public static EnterPhoneNamberFragment newInstance() {
        EnterPhoneNamberFragment fragment = new EnterPhoneNamberFragment();
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
        // Раздуйте макет для этого фрагмента
        return inflater.inflate(R.layout.fragment_enter_phone_namber, container, false);
    }

    /**=================================================================================
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();
        // Инициализация переменных
        register_input_phone_number = requireView().findViewById(R.id.register_input_phone_number);
        register_btn_next = requireView().findViewById(R.id.register_btn_next);

        // Создаем обратный вызов
        mCallback = new OnVerificationStateChangedCallbacks() {
            // При успешной верификации
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                auth.signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Если успешно
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Добро пожаловать!", Toast.LENGTH_SHORT).show();
                            // Запускаем MainActivity
                            // TODO: по-моему, это неправильный вызов!!!!!
                            Intent intent = new Intent(requireActivity(), MainActivity.class);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                        // Если НЕ успешно
                        else {
                            Toast.makeText(getActivity(), "Проблема: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getActivity(), "Проблема: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            // Этот метод запускается, когда было отправлено СМС
            @Override
            public void onCodeSent(@NonNull String id, @NonNull ForceResendingToken token) {
                // Переход на EnterCodeFragment
                //Устанавливаем контейнер в фрагмент
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.registerDataContainer, EnterCodeFragment.newInstance(phoneNumber,id))
                        .addToBackStack(null)
                        .commit();
            }
        };


        // Навешивание реагирования на кнопку
        register_btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode();
            }
        });
    }

    /**=================================================================================
     * sendCode - посылаем для проверки кода
     */
    private void sendCode() {
        //Если введенный номер пуст
        if (register_input_phone_number.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.register_toast_enter_phone), Toast.LENGTH_SHORT).show();
        }
        //Если введенный номер НЕ пуст
        else {
            // идентификация пользователя
            authUser();
        }
    }

    /**=================================================================================
     * authUser - идентификация пользователя
     */
    private void authUser() {
        // Получили номер телефона
        phoneNumber = register_input_phone_number.getText().toString();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)       // Номер телефона для подтверждения
                        .setTimeout(60L, TimeUnit.SECONDS) // Тайм-аут и единица измерения
                        .setActivity(requireActivity())                 // Активность (для привязки обратного вызова)
                        .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

}