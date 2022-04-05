package com.apushnikov.other351_mytelegram.ui.screens.settings;

import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.CHILD_BIO;
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

/**======================================================================================
 * ChangeBioFragment - фрагмент изменения информации о себе Bio
 */
public class ChangeBioFragment extends BaseChangeFragment {

    private EditText settings_input_bio;

    /**=================================================================================
     * EnterPhoneNamberFragment - Требуется пустой публичный конструктор
     */
    public ChangeBioFragment() {
        // Required empty public constructor
    }

    /**=================================================================================
     * Используйте этот фабричный метод для создания нового экземпляра этого фрагмента
     * с использованием предоставленных параметров.
     *
     * @return Новый экземпляр фрагмента ChangeNameFragment.
     */
    public static ChangeBioFragment newInstance() {
        ChangeBioFragment fragment = new ChangeBioFragment();
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
        return inflater.inflate(R.layout.fragment_change_bio, container, false);
    }

    /**=================================================================================
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();

        // Инициализация переменных
        settings_input_bio = requireView().findViewById(R.id.settings_input_bio);
    }

    /**=================================================================================
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();

        // заполняем экранные переменные
        settings_input_bio.setText(user.bio);
    }

    /**=================================================================================
     * changeName - Выбрали Подтвердить изменения
     */
    @Override
    public void change() {

        // Запоминаем текущий bio
        String newBio = settings_input_bio.getText().toString().trim();

        ref_database_root.child(NODE_USERS).child(current_uid).child(CHILD_BIO)
                .setValue(newBio)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Данные обновлены", Toast.LENGTH_SHORT).show();
                            // Запоминаем bio
                            user.bio = newBio;
                            // Возвращаемся по стеку назад
                            getParentFragmentManager().popBackStack();
                        }

                    }
                });
    }
}