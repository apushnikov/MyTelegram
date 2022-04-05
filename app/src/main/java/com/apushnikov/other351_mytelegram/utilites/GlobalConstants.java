package com.apushnikov.other351_mytelegram.utilites;

import com.apushnikov.other351_mytelegram.database.DatabaseFuns;
import com.apushnikov.other351_mytelegram.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class GlobalConstants {

    /**databaseFuns - Фунции базы данных FirebaseDatabase, FirebaseStorage*/
    public static DatabaseFuns databaseFuns;

    /**auth - Авторизация*/
    public static FirebaseAuth auth;
    /**current_uid - уникальный uid пользователя*/
    public static String current_uid;

    /**ref_database_root - корень базы данных Database*/
    public static DatabaseReference ref_database_root = null;
    /**ref_storage_root - корень базы данных Storage*/
    public static StorageReference ref_storage_root = null;

    public static User user;

    /** FOLDER_PROFILE_IMAGE - Константа для папки, в которой хранится изображение пользователя
     * в базе Storage*/
    public static String FOLDER_PROFILE_IMAGE = "profile_image";

    /** NODE_USERS - Константа для моды users*/
    public static String NODE_USERS = "users";
    public static String NODE_USERNAMES = "usernames";

    public static String CHILD_ID = "id";
    public static String CHILD_USERNAME = "username";
    public static String CHILD_BIO = "bio";
    public static String CHILD_PHONE = "phone";
    public static String CHILD_EMAIL = "email";
    public static String CHILD_FULLNAME = "fullmane";
//    public static String CHILD_STATUS = "status";
    public static String CHILD_PHOTO_URL = "photoUrl";
    public static String CHILD_STATE = "state";

    /**=================================================================================
     * initFarebase - инициализируем базу данных Firebase
     */
    public static void initFarebase() {

        // Инициализируем фунции базы данных FirebaseDatabase, FirebaseStorage
        databaseFuns = new DatabaseFuns();

        // Инициализируем Firebase auth
        auth = FirebaseAuth.getInstance();
        // Чтобы применить язык приложения по умолчанию, а не задавать его явно. auth.useAppLanguage();
        auth.setLanguageCode("ru");

        // Инициализируем ссылку на корневой элемент FirebaseDatabase ref_database_root
        ref_database_root = FirebaseDatabase.getInstance().getReference();
        // Инициализируем ссылку на корневой элемент FirebaseStorage ref_storage_root
        ref_storage_root = FirebaseStorage.getInstance().getReference();

        user = new User();

        // Инициализируем уникальный uid пользователя
        current_uid = Objects.requireNonNull(auth.getCurrentUser()).getUid().toString();
    }



}
