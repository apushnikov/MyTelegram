package com.apushnikov.other351_mytelegram.ui.screens.settings;

import static android.app.Activity.RESULT_OK;
import static com.apushnikov.other351_mytelegram.myApplication.MyApplication.logFile;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.CHILD_PHOTO_URL;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.FOLDER_PROFILE_IMAGE;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.NODE_USERS;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.auth;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.databaseFuns;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_database_root;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_storage_root;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.current_uid;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.user;
import static com.apushnikov.other351_mytelegram.utilites.Utilities.downloadAndSetImage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.apushnikov.other351_mytelegram.MainActivity;
import com.apushnikov.other351_mytelegram.R;
import com.apushnikov.other351_mytelegram.activities.RegisterActivity;
import com.apushnikov.other351_mytelegram.cropper.CropImage;
import com.apushnikov.other351_mytelegram.cropper.CropImageView;
import com.apushnikov.other351_mytelegram.database.OnDataGetUrlFromStorage;
import com.apushnikov.other351_mytelegram.database.OnDataPutFileToStorage;
import com.apushnikov.other351_mytelegram.database.OnDataPutUrlToDatabase;
import com.apushnikov.other351_mytelegram.databinding.FragmentSettingsBinding;
import com.apushnikov.other351_mytelegram.ui.screens.base.BaseFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;

/**=================================================================================
 * SettingsFragment - фрагмент настроек
 */
public class SettingsFragment extends BaseFragment implements
        OnDataPutFileToStorage,
        OnDataGetUrlFromStorage,
        OnDataPutUrlToDatabase {

    //===========================================================================================
    // region: Поля и константы
    //==========================================================================================

    /** fragmentChatsBinding - привязка для чатов */
    private FragmentSettingsBinding binding;

    // endregion

    //===========================================================================================
    // region: Методы
    //===========================================================================================

    //TODO: Перенести в BaseFragment.java. Сделать этот фрагмен наследником от BaseFragment.java???

    /**=================================================================================
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();

        // TODO: Перенести в BaseFragment
        // Выключаем драйвер
        ((MainActivity) requireActivity()).appDrawer.disableDrawer();

    }

    //TODO: Перенести в BaseFragment.java. Сделать этот фрагмен наследником от BaseFragment.java???

    /**=================================================================================
     * onStop
     */
    @Override
    public void onStop() {
        super.onStop();

        // TODO: Перенести в BaseFragment
        // Включаем драйвер
        ((MainActivity) requireActivity()).appDrawer.enableDrawer();
    }

    /**=================================================================================
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Раздуйте макет для этого фрагмента
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        // Возвращаем рутовый элемент
        return binding.getRoot();
    }

    /**=================================================================================
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        // Включаем меню
        setHasOptionsMenu(true);

        initFields();
    }

    /**=================================================================================
     * initFields - инициализация экранных переменных
     */
    private void initFields() {

        binding.settingsUsername.setText(user.username);
        binding.settingsBio.setText(user.bio);
        binding.settingsPhoneNumber.setText(user.phone);
        binding.settingsEmail.setText(user.email);
        binding.settingsFullName.setText(user.fullmane);
//        binding.settingsStatus.setText(user.status);
        binding.settingsStatus.setText(user.state);

        binding.settingsBtnChangeUsername.setOnClickListener(view -> {
            //Устанавливаем контейнер в фрагмент
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dataContainer, ChangeUsernameFragment.newInstance())
                    .addToBackStack(null)
                    .commit();

        });

        binding.settingsBtnChangeBio.setOnClickListener(view -> {
            //Устанавливаем контейнер в фрагмент
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dataContainer, ChangeBioFragment.newInstance())
                    .addToBackStack(null)
                    .commit();

        });

        binding.settingsChangePhoto.setOnClickListener(view -> changePhotoUser());

        // Скачивает фото пользователя и устанавливает картинку в указанный view
        downloadAndSetImage(user.photoUrl,binding.settingsUserPhoto);

    }

    /**=================================================================================
     * changePhotoUser - изменение фото пользователя
     */
    private void changePhotoUser() {

        logFile.writeLogFile("SettingsFragment: changePhotoUser: Начало");

        // Вызываем активити CropImage - результат работы перехватываем в onActivityResult в MainActivity
        // Вызываем для фрагмента
        CropImage.activity()
                .setAspectRatio(1,1)    // Кроппер будет пропорционален, не растягивается
                .setRequestedSize(600,600)  // размер, до которого нужно изменить размер обрезанного изображения
                .setCropShape(CropImageView.CropShape.OVAL)     // Кроппер овальный
                .start((MainActivity) requireActivity(), this);


        logFile.writeLogFile("SettingsFragment: changePhotoUser: Конец");

    }

    /**=================================================================================
     * onCreateOptionsMenu - создание меню
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        requireActivity().getMenuInflater().inflate(R.menu.settings_action_menu, menu);
    }

    /**=================================================================================
     * onOptionsItemSelected - реакция на выбор меню
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Выбрали Выход
            case R.id.settings_menu_exit:

                auth.signOut();
                // Запускаем снова авторизацию RegisterActivity
                Intent intent = new Intent(requireActivity(), RegisterActivity.class);
                startActivity(intent);
                requireActivity().finish();
                break;

            // Выбрали Изменить имя
            case R.id.settings_menu_change_name:

                //Устанавливаем контейнер в фрагмент
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.dataContainer, ChangeNameFragment.newInstance())
//                        .replace(R.id.dataContainer, new ChangeNameFragment())
                        .addToBackStack(null)
                        .commit();
                break;


        }
        return true;
    }

    /**=================================================================================
     * onActivityResult - перехватываем результат работы CropImage.activity()
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {
            // Получаем uri обрезанной картинки
            Uri uri = CropImage.getActivityResult(data).getUri();
            // Помещаем в Storage
            // Определяем путь в Storage
            StorageReference path = ref_storage_root.child(FOLDER_PROFILE_IMAGE)
                    .child(current_uid);
            // отправляет фото в хранилище FirebaseStorage
            // В случае успеха, вызывает интерфейс onPutFileToStorage для дальнейших действий
            databaseFuns.putFileToStorage(uri, path, this);
        }
    }

    /**=================================================================================
     *Интерфейс OnDataPutFileToStorage - действия, когда разместили фото в FirebaseStorage
     */
    @Override
    public void onPutFileToStorage(StorageReference path) {
        // Получаем URL фото из Storage
        // В случае успеха, вызывает интерфейс onGetUrlFromStorage для дальнейших действий
        databaseFuns.getUrlFromStorage(path, this);
    }

    /**=================================================================================
     *Интерфейс onGetUrlFromStorage - действия, когда получили URL фото из Storage
     */
    @Override
    public void onGetUrlFromStorage(Uri uri) {
        String photoUrl = uri.toString();
        // отправляет полученый URL в базу данных
        // В случае успеха, вызывает интерфейс onGetUrlFromStorage для дальнейших действий
        databaseFuns.putUrlToDatabase(photoUrl, this);
    }

    /**=================================================================================
     *Интерфейс onPutUrlToDatabase - действия, после отправки полученного  URL в базу данных FirebaseDatabase
     */
    @Override
    public void onPutUrlToDatabase(String photoUrl) {
        // Скачивает и устанавливает картинку в указанный view
        downloadAndSetImage(photoUrl,binding.settingsUserPhoto);
        Toast.makeText(getActivity(), "Данные обновлены", Toast.LENGTH_SHORT).show();
        user.photoUrl = photoUrl;
        // Обновляет хеадер с новыми данными
        ((MainActivity) requireActivity()).appDrawer.updateHeader();
    }

    // endregion

}