package com.apushnikov.other351_mytelegram.ui.fragments;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apushnikov.other351_mytelegram.databinding.FragmentChatsBinding;
import com.apushnikov.other351_mytelegram.ui.screens.base.BaseFragment;

/**=================================================================================
 * ChatsFragment - фрагмент чатов
 */
public class ChatsFragment extends BaseFragment {

    /** fragmentChatsBinding - привязка для чатов*/
    private FragmentChatsBinding binding;


    /**=================================================================================
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Раздуйте макет для этого фрагмента
        binding = FragmentChatsBinding.inflate(getLayoutInflater());
        // Возвращаем рутовый элемент
        return binding.getRoot();
    }

    /**=================================================================================
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();
    }
}