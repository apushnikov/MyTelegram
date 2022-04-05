package com.apushnikov.other351_mytelegram;

import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.CHILD_PHOTO_URL;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.FOLDER_PROFILE_IMAGE;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.NODE_USERS;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.auth;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.databaseFuns;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_database_root;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.current_uid;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_storage_root;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.user;
//import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.initFarebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
//import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.apushnikov.other351_mytelegram.activities.RegisterActivity;
import com.apushnikov.other351_mytelegram.cropper.CropImage;
import com.apushnikov.other351_mytelegram.database.OnDataInitUser;
import com.apushnikov.other351_mytelegram.databinding.ActivityMainBinding;
import com.apushnikov.other351_mytelegram.models.User;
import com.apushnikov.other351_mytelegram.ui.fragments.ChatsFragment;
import com.apushnikov.other351_mytelegram.ui.objects.AppDrawer;
import com.apushnikov.other351_mytelegram.utilites.AppStates;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**======================================================================================
 * MainActivity - стартовая активность
 */
public class MainActivity extends AppCompatActivity implements
        OnDataInitUser {

    //===========================================================================================
    // region: Поля и константы
    //==========================================================================================

    /** binding - привязка*/
    private ActivityMainBinding binding;

    public AppDrawer appDrawer;
    private Toolbar toolbar;

    // endregion

    //===========================================================================================
    // region: Методы
    //===========================================================================================

    /** =================================================================================
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // инициализация пользователя
        // Инициализация пользователя проходит во втором потоке
        // В случае успеха, вызывает интерфейс OnDataInitUser для дальнейших действий
        databaseFuns.initUser(this);

    }

    /**=================================================================================
     *Интерфейс onInitUser - действия, когда инициализировали пользователя
     */
    @Override
    public void onInitUser() {
        initFields();
        initFunc();
    }

    /**=================================================================================
     * initFunc - инициализация функциональностей
     */
    private void initFunc() {

        //Если пользователь авторизован
        if (auth.getCurrentUser() != null) {
            setSupportActionBar(toolbar);
            //Создаем AppDrawer
            appDrawer.create();
            //Устанавливаем контейнер в фрагмент
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dataContainer, new ChatsFragment()).commit();
        }
        //Если пользователь НЕ авторизован
        else {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**==================================================================================
     * initFealds - инициализация переменных
     */
    private void initFields() {

        toolbar = binding.mainToolbar;
        appDrawer = new AppDrawer(this, toolbar);
//        initUser();
    }

    /**==================================================================================
     * onStart
     */
    @Override
    protected void onStart() {
        super.onStart();

        // updateState - устанавливает статус, обновляет базу данных
        AppStates.updateState(AppStates.ONLINE);
    }

    /**==================================================================================
     * onStop
     */
    @Override
    protected void onStop() {
        super.onStop();

        // updateState - устанавливает статус, обновляет базу данных
        AppStates.updateState(AppStates.OFFLINE);
    }

    //TODO: Нужно hideKeyBoard перенести в Utilities.java
    /**==================================================================================
     * hideKeyBoard - Скрыть клавиатуру
     */
    public void hideKeyBoard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
    }


    // endregion

}