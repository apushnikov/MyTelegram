package com.apushnikov.other351_mytelegram.activities;

//import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.initFarebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

        import com.apushnikov.other351_mytelegram.R;
import com.apushnikov.other351_mytelegram.databinding.ActivityMainBinding;
import com.apushnikov.other351_mytelegram.databinding.ActivityRegisterBinding;
        import com.apushnikov.other351_mytelegram.ui.screens.register.EnterChooseLoginMethodFragment;

/**======================================================================================
 * RegisterActivity - регистрация пользователя
 */
public class RegisterActivity extends AppCompatActivity {

    /** binding - привязка*/
    private ActivityRegisterBinding binding;
    private Toolbar toolbar;

    /**=======================================================================================
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**=======================================================================================
     * onStart
     */
    @Override
    protected void onStart() {
        super.onStart();

        //Инициализируем тулбар
        toolbar = binding.registerToolbar;
        setSupportActionBar(toolbar);
        // Заголовок
        setTitle(getString(R.string.register_title_your_phone));
        //Устанавливаем контейнер в фрагмент
        getSupportFragmentManager().beginTransaction()
                .add(R.id.registerDataContainer, EnterChooseLoginMethodFragment.newInstance())
                .commit();
    }

}