package com.apushnikov.other351_mytelegram.ui.screens.settings;

import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.CHILD_FULLNAME;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.NODE_USERS;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_database_root;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.current_uid;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.apushnikov.other351_mytelegram.MainActivity;
import com.apushnikov.other351_mytelegram.R;
import com.apushnikov.other351_mytelegram.ui.screens.base.BaseChangeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.List;

/**======================================================================================
 * ChangeNameFragment - фрагмент изменения имени и фамилии
 */
public class ChangeNameFragment extends BaseChangeFragment {

    //===========================================================================================
    // region: Поля и константы
    //==========================================================================================

    //TODO: Не получается использовать binding - фрагмент получается сжатым

    // Это влючить при использовании binding
    /** fragmentChatsBinding - привязка для чатов */
//    private FragmentChangeNameBinding binding;


    //TODO: необходимо привести переменные к одному стилю, например edEMail, btnSingUp

    // Это влючить при использовании БЕЗ binding
    private EditText settings_input_name;
    private EditText settings_input_surname;


    // endregion

    //===========================================================================================
    // region: Методы
    //===========================================================================================

    // Это влючить при использовании БЕЗ binding
    /**=================================================================================
     * EnterPhoneNamberFragment - Требуется пустой публичный конструктор
     */
    public ChangeNameFragment() {
        // Required empty public constructor
    }

    // Это влючить при использовании БЕЗ binding
    /**=================================================================================
     * Используйте этот фабричный метод для создания нового экземпляра этого фрагмента
     * с использованием предоставленных параметров.
     *
     * @return Новый экземпляр фрагмента ChangeNameFragment.
     */
    public static ChangeNameFragment newInstance() {

        ChangeNameFragment fragment = new ChangeNameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    // Это влючить при использовании БЕЗ binding
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

        // Это влючить при использовании БЕЗ binding
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_name, container, false);

        // Это влючить при использовании binding
        // Раздуйте макет для этого фрагмента
//        binding = FragmentChangeNameBinding.inflate(getLayoutInflater());
        // Возвращаем рутовый элемент
//        return binding.getRoot();

    }

    /**=================================================================================
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();

        // Это влючить при использовании БЕЗ binding
        // Инициализация переменных
        settings_input_name = requireView().findViewById(R.id.settings_input_name);
        settings_input_surname = requireView().findViewById(R.id.settings_input_surname);

    }

    /**=================================================================================
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();

        initFullnameList();
    }

    /**=================================================================================
     * initFullnameList - Разделяем fullmane на имя и фамилию и заполняем экранные переменные
     */
    private void initFullnameList() {

        // Это влючить при использовании БЕЗ binding
        // Разделяем fullmane на имя и фамилию и заполняем экранные переменные
        List<String> fullNameList = Arrays.asList(user.fullmane.split(" "));
        if (fullNameList.size() > 1) {
            settings_input_name.setText(fullNameList.get(0));
            settings_input_surname.setText(fullNameList.get(1));
        } else
            settings_input_name.setText(fullNameList.get(0));

        // Это влючить при использовании binding
        // Разделяем fullmane на имя и фамилию и заполняем экранные переменные
/*        List<String> fullNameList = Arrays.asList(user.fullmane.split(" "));
        if (fullNameList.size() > 1) {
            binding.settingsInputName.setText(fullNameList.get(0));
            binding.settingsInputSurname.setText(fullNameList.get(1));
        } else
            binding.settingsInputName.setText(fullNameList.get(0));*/

    }

    /**=================================================================================
     * changeName - Выбрали Подтвердить изменения
     */
    @Override
    public void change() {

        // Это влючить при использовании БЕЗ binding
        // Запоминаем текущий логин и пароль
        String name = settings_input_name.getText().toString().trim();
        String surname = settings_input_surname.getText().toString().trim();

        // Это влючить при использовании binding
        // Запоминаем текущий логин и пароль
//        String name = binding.settingsInputName.getText().toString().trim();
//        String surname = binding.settingsInputSurname.getText().toString().trim();



        //Если имя пусто
        if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show();
        }
        //Если имя НЕ пусто
        else {
            //Собираем fullname из имени и фамилии
            String fullname = name + " " + surname;
            // Обновляем базу данных
            ref_database_root.child(NODE_USERS).child(current_uid).child(CHILD_FULLNAME)
                    .setValue(fullname)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Данные обновлены", Toast.LENGTH_SHORT).show();
                        // Запоминаем fullmane
                        user.fullmane = fullname;
                        // Обновляет хеадер с новыми данными
                        ((MainActivity) requireActivity()).appDrawer.updateHeader();
                        // Возвращаемся по стеку назад
                        getParentFragmentManager().popBackStack();
                    }

                }
            });
        }
    }

// endregion

}