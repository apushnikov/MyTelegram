package com.apushnikov.other351_mytelegram.ui.screens.register;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.apushnikov.other351_mytelegram.R;

/**======================================================================================
 * EnterChooseLoginMethodFragment - фрагмент выбора метода регистрации и входа
 */
public class EnterChooseLoginMethodFragment extends Fragment {

    private Button btnSingEMail;
    private Button btnSingPhone;

    /**=================================================================================
     * EnterChooseLoginMethodFragment - Требуется пустой публичный конструктор
     */
    public EnterChooseLoginMethodFragment() {
        // Required empty public constructor
    }


    /**=================================================================================
     * Используйте этот фабричный метод для создания нового экземпляра этого фрагмента
     * с использованием предоставленных параметров.
     *
     * @return Новый экземпляр фрагмента EnterChooseLoginMethodFragment.
     */
    public static EnterChooseLoginMethodFragment newInstance() {

        EnterChooseLoginMethodFragment fragment = new EnterChooseLoginMethodFragment();
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
        return inflater.inflate(R.layout.fragment_enter_choose_login_method, container, false);
    }

    /**=================================================================================
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();

        // Инициализация переменных
        btnSingEMail = requireView().findViewById(R.id.btnSingEMail);
        btnSingPhone = requireView().findViewById(R.id.btnSingPhone);

        // Навешивание реагирования на кнопку
        btnSingEMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methodSingEMail();
            }
        });

        // Навешивание реагирования на кнопку
        btnSingPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methodSingPhone();
            }
        });

    }

    /**=================================================================================
     * methodSingEMail - выбран метод входа по e-mail и паролю
     */
    private void methodSingEMail() {

        //Устанавливаем контейнер в фрагмент
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.registerDataContainer, EnterEMailFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    /**=================================================================================
     * methodSingPhone - выбран метод входа по номеру телефона
     */
    private void methodSingPhone() {

        //Устанавливаем контейнер в фрагмент
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.registerDataContainer, EnterPhoneNamberFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

}