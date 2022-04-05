package com.apushnikov.other351_mytelegram.ui.objects;

import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.user;
import static com.apushnikov.other351_mytelegram.utilites.Utilities.downloadAndSetImage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.apushnikov.other351_mytelegram.R;
import com.apushnikov.other351_mytelegram.ui.screens.settings.SettingsFragment;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import java.util.Objects;

/**======================================================================================
 * AppDrawer - создает Drawer (левую панель управления)
 */
public class AppDrawer {

    //===========================================================================================
    // region: Поля и константы
    //==========================================================================================

    private AppCompatActivity mainActivity;
    private Toolbar toolbar;

    private Drawer drawer;
    private AccountHeader header;
    private DrawerLayout drawerLayout;
    /** Текущий профиль пользователя*/
    private ProfileDrawerItem currentProfile;

    // endregion

    //===========================================================================================
    // region: Методы
    //===========================================================================================

    /**=======================================================================================
     * AppDrawer = конструктор
     * @param mainActivity
     * @param toolbar
     */
    public AppDrawer(AppCompatActivity mainActivity, Toolbar toolbar) {
        this.mainActivity = mainActivity;
        this.toolbar = toolbar;
    }

    /**======================================================================================
     * create - AppDrawer
     */
    public void create() {

        // initLoader - драйвер не умеет скачивать картинки.
        // Необходимо инициализировать специальны драйвер DrawerImageLoader, который позволит
        // работать с библиотекой Picasso
        initLoader();

        // Создание header
        createHeader();
        // Создание драйвера
        createDrawer();
        drawerLayout = drawer.getDrawerLayout();
    }


    /**================================================================================
     * createDrawer = создание драйвера
     */
    private void createDrawer() {
        drawer = new DrawerBuilder()
                .withActivity(mainActivity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withSelectedItem(-1)
                .withAccountHeader(header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(100)
                                .withIconTintingEnabled(true)
                                .withName("Создать группу")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_create_groups),
                        new PrimaryDrawerItem().withIdentifier(101)
                                .withIconTintingEnabled(true)
                                .withName("Создать секретный чат")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_secret_chat),
                        new PrimaryDrawerItem().withIdentifier(102)
                                .withIconTintingEnabled(true)
                                .withName("Создать канал")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_create_channel),
                        new PrimaryDrawerItem().withIdentifier(103)
                                .withIconTintingEnabled(true)
                                .withName("Контакты")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_contacts),
                        new PrimaryDrawerItem().withIdentifier(104)
                                .withIconTintingEnabled(true)
                                .withName("Звонки")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_phone),
                        new PrimaryDrawerItem().withIdentifier(105)
                                .withIconTintingEnabled(true)
                                .withName("Избранное")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_favorites),
                        new PrimaryDrawerItem().withIdentifier(106)
                                .withIconTintingEnabled(true)
                                .withName("Настройки")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_settings),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(107)
                                .withIconTintingEnabled(true)
                                .withName("Пригласить друзей")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_invate),
                        new PrimaryDrawerItem().withIdentifier(108)
                                .withIconTintingEnabled(true)
                                .withName("Вопросы о Телеграм")
                                .withSelectable(false)
                                .withIcon(R.drawable.ic_menu_help)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(@Nullable View view, int i, @NonNull IDrawerItem<?> iDrawerItem) {
                        switch(i) {
                            case 7:
                                //Устанавливаем контейнер в фрагмент
                                mainActivity.getSupportFragmentManager().beginTransaction()
                                        .addToBackStack(null)
                                        .replace(R.id.dataContainer, new SettingsFragment()).commit();
                        }

                        return false;
                    }
                }).build();
    }

    /**=================================================================================
     * createHeader - создание Header
     */
    private void createHeader() {

        // TODO: как обойти проблему - виден только телефон или е-майл
        // Инициализируем профиль пользователя
        currentProfile = new ProfileDrawerItem()
                .withName(user.fullmane)
                .withEmail(user.phone)
                .withIcon(user.photoUrl)
                .withIdentifier(200);

        // Инициализируем хеадер
        header = new AccountHeaderBuilder()
                .withActivity(mainActivity)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(currentProfile)
                .build();
    }

    /**=================================================================================
     * updateHeader - Обновляет хеадер
     */
    public void updateHeader() {
        // Обновляем текущий профиль
        currentProfile
                .withName(user.fullmane)
                .withEmail(user.phone)
                .withIcon(user.photoUrl);

        // Обновляем хеадер по обновленному профилю
        header.updateProfile(currentProfile);
    }

    /**=================================================================================
     * initLoader - драйвер не умеет скачивать картинки.
     * Необходимо инициализировать специальны драйвер DrawerImageLoader, который позволит работать
     * с библиотекой Picasso
     */
    public void initLoader() {
        // Создаем объект DrawerImageLoader
        DrawerImageLoader.Companion.init(new DrawerImageLoader.IDrawerImageLoader() {
            @Override
            public void set(@NonNull ImageView imageView, @NonNull Uri uri, @NonNull Drawable drawable) {
                downloadAndSetImage(uri.toString(),imageView);
            }

            @Override
            public void set(@NonNull ImageView imageView, @NonNull Uri uri, @NonNull Drawable drawable, @Nullable String s) {
                downloadAndSetImage(uri.toString(),imageView);
            }

            @Override
            public void cancel(@NonNull ImageView imageView) {

            }

            @NonNull
            @Override
            public Drawable placeholder(@NonNull Context context) {
                return null;
            }

            @NonNull
            @Override
            public Drawable placeholder(@NonNull Context context, @Nullable String s) {
                return null;
            }
        });

    }

    /**=================================================================================
     * disableDrawer - отключаем драйвер
     */
    public void disableDrawer() {

        Objects.requireNonNull(drawer.getActionBarDrawerToggle()).setDrawerIndicatorEnabled(false);
        Objects.requireNonNull(mainActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        // Навешиваем на toolbar возврат по стеку назад
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.getSupportFragmentManager().popBackStack();
            }
        });
    }

    /**=================================================================================
     * enableDrawer - включаем драйвер
     */
    public void enableDrawer() {

        Objects.requireNonNull(mainActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        Objects.requireNonNull(drawer.getActionBarDrawerToggle()).setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        // Навешиваем на toolbar что бы драйвер открывался
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer();
            }
        });
    }

// endregion

}




