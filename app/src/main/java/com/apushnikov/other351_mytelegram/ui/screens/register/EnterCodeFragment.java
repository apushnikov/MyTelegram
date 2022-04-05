package com.apushnikov.other351_mytelegram.ui.screens.register;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.apushnikov.other351_mytelegram.MainActivity;
import com.apushnikov.other351_mytelegram.R;
import com.apushnikov.other351_mytelegram.databinding.FragmentEnterCodeBinding;
import com.apushnikov.other351_mytelegram.databinding.FragmentEnterPhoneNamberBinding;
import com.apushnikov.other351_mytelegram.utilites.GlobalConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;

/**=========================================================================================
 * EnterCodeFragment - проверяем введенный код, присланный по СМС
 */
class EnterCodeFragment extends Fragment {

    /**register_input_code - введенный код*/
    private EditText register_input_code;

    private static final String ARG_PHONENUMBER = "phoneNumber";
    private static final String ARG_ID = "id";
    private String phoneNumber;
    private String id;

    /**=================================================================================
     * EnterCodeFragment - Требуется пустой публичный конструктор
     */
    public EnterCodeFragment() {
        // Требуется пустой публичный конструктор
    }

    /**=================================================================================
     * Используйте этот фабричный метод для создания нового экземпляра этого фрагмента
     * с использованием предоставленных параметров.
     *
     * @return Новый экземпляр фрагмента EnterCodeFragment.
     * @param phoneNumber
     * @param id
     */
    public static EnterCodeFragment newInstance(String phoneNumber, String id) {
        EnterCodeFragment fragment = new EnterCodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONENUMBER, phoneNumber);
        args.putString(ARG_ID, id);
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
            phoneNumber = getArguments().getString(ARG_PHONENUMBER);
            id = getArguments().getString(ARG_ID);
        }
    }

    /**=================================================================================
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Раздуйте макет для этого фрагмента
        return inflater.inflate(R.layout.fragment_enter_code, container, false);
    }

    /**=================================================================================
     * onStart
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onStart() {
        super.onStart();

        //TODO: хотим в активити RegisterActivity установить введенный номер телефона. Это правильно?
        requireView().setAccessibilityPaneTitle(phoneNumber);

        // введенный код
        register_input_code = requireView().findViewById(R.id.register_input_code);

        //Повесим слушатель на введенный код
        register_input_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Считываем введенный код
                // TODO: Зачем Считываем введенный код? мы же его не передаем
                String string = register_input_code.getText().toString();
                if (string.length() == 6) {
                    enterCode();
                }

            }
        });

    }

    /**=================================================================================
     * enterCode - верификация введенного кода
     */
    private void enterCode() {
        // Считываем введенный код
        String code = register_input_code.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id, code);
        GlobalConstants.auth.signInWithCredential(credential)
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

}