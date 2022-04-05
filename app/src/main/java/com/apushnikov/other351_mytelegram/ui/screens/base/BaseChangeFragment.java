package com.apushnikov.other351_mytelegram.ui.screens.base;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.apushnikov.other351_mytelegram.MainActivity;
import com.apushnikov.other351_mytelegram.R;

public class BaseChangeFragment extends Fragment {

    /**=================================================================================
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();

        setHasOptionsMenu(true);
        // Выключаем драйвер
        ((MainActivity) requireActivity()).appDrawer.disableDrawer();

        // Скрываем клавиатуру
        ((MainActivity) requireActivity()).hideKeyBoard();

    }

    /**=================================================================================
     * onStop
     */
    @Override
    public void onStop() {
        super.onStop();

        // Включаем драйвер
        ((MainActivity) requireActivity()).appDrawer.enableDrawer();

        // Скрываем клавиатуру
//        ((MainActivity) requireActivity()).hideKeyBoard();

    }

    /**=================================================================================
     * onCreateOptionsMenu - создаем меню
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        // Раздуваем меню
        requireActivity().getMenuInflater().inflate(R.menu.settings_menu_confirm, menu);
    }

    /**=================================================================================
     * onOptionsItemSelected - реагируем на меню
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Выбрали Подтвердить изменения
            case R.id.settings_confirm_change:
                change();
                break;
        }
        return true;
    }

    /**=================================================================================
     * changeName - Выбрали Подтвердить изменения
     */
    public void change() {
    }

}
