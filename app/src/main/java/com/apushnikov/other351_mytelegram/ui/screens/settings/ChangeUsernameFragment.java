package com.apushnikov.other351_mytelegram.ui.screens.settings;

import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.CHILD_USERNAME;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.NODE_USERNAMES;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.NODE_USERS;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_database_root;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.current_uid;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.user;

import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.apushnikov.other351_mytelegram.R;
import com.apushnikov.other351_mytelegram.ui.screens.base.BaseChangeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.Objects;

/**======================================================================================
 * ChangeUsernameFragment - фрагмент изменения Username
 */
public class ChangeUsernameFragment extends BaseChangeFragment {


    //TODO: Ошибка!!! – если в username поставить точку, приложение вылетает ИСПРАВИТЬ!!!

    private String newUsername;

    private EditText settings_input_username;

    /**=================================================================================
     * ChangeUsernameFragment - Требуется пустой публичный конструктор
     */
    public ChangeUsernameFragment() {
        // Required empty public constructor
    }

    /**=================================================================================
     * Используйте этот фабричный метод для создания нового экземпляра этого фрагмента
     * с использованием предоставленных параметров.
     *
     * @return Новый экземпляр фрагмента ChangeUsernameFragment.
     */
    public static ChangeUsernameFragment newInstance() {
        ChangeUsernameFragment fragment = new ChangeUsernameFragment();
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
        return inflater.inflate(R.layout.fragment_change_username, container, false);
    }

    /**=================================================================================
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();

        // Инициализация переменных
        settings_input_username = requireView().findViewById(R.id.settings_input_username);
    }

    /**=================================================================================
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();

        // заполняем экранные переменные
        settings_input_username.setText(user.username);
    }

    /**=================================================================================
     * change - Выбрали Подтвердить изменения. Проверка на уникальность username
     */
    @Override
    public void change() {

        // Запоминаем текущий username и переводим его в маленькие буквы
        newUsername = settings_input_username.getText().toString().trim().toLowerCase(Locale.getDefault());
        // Если новый newUsername пуст
        if (newUsername.isEmpty()) {
            Toast.makeText(getActivity(), "Поле пустое", Toast.LENGTH_SHORT).show();
        }
        // Если новый newUsername НЕ пуст
        else {
            ref_database_root.child(NODE_USERNAMES)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // Если в базе есть как наш новый newUsername
                            if (snapshot.hasChild(newUsername)) {
                                Toast.makeText(getActivity(), "Такой пользователь уже существует", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                changeUsername();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }
    }

    /**=================================================================================
     * changeUsername - Изменить Username и сохранить в базе
     */
    private void changeUsername() {
        ref_database_root.child(NODE_USERNAMES).child(newUsername).setValue(current_uid)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            updateCurrentUsername();
                        }

                    }
                });
    }

    /**=================================================================================
     * updateCurrentUsername - Обновляем текущий Username в базе
     */
    private void updateCurrentUsername() {
        ref_database_root.child(NODE_USERS).child(current_uid).child(CHILD_USERNAME)
                .setValue(newUsername)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Старые Username удаляем
                            deleteOldUsername();
                        }
                        else {
                            Toast.makeText(getActivity(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    /**=================================================================================
     * deleteOldUsername - Старые Username удаляем
     */
    private void deleteOldUsername() {
        ref_database_root.child(NODE_USERNAMES).child(user.username).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Данные обновлены", Toast.LENGTH_SHORT).show();
                            // Возвращаемся по стеку назад
                            getParentFragmentManager().popBackStack();
                            //Обновляем модель данных
                            user.username = newUsername;
                        }
                        else {
                            Toast.makeText(getActivity(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


}