package com.apushnikov.other351_mytelegram.database;


import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.CHILD_PHOTO_URL;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.NODE_USERS;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.current_uid;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_database_root;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.user;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.apushnikov.other351_mytelegram.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**==================================================================================
 * DatabaseFuns - Здесь собраны фунции при работе с базой данных FirebaseDatabase, FirebaseStorage
 */
public class DatabaseFuns {

    //===========================================================================================
    // region: Методы
    //===========================================================================================

    /**=================================================================================
     * putFileToStorage - отправляет файл (картинку) в хранилище FirebaseStorage
     * <p>
     *     В случае успеха, вызывает интерфейс onPutFileToStorage для дальнейших действий
     *
     * @param uri - uri файла (картинки)
     * @param path - путь в хранилище FirebaseStorage
     * @param onDataPutFileToStorage
     */
    public void putFileToStorage(Uri uri, StorageReference path, OnDataPutFileToStorage onDataPutFileToStorage) {
        // Помещаем в Storage фото
        path.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            // В случае успешной вставки, вызываем интерфейс для дальнеших действий
            onDataPutFileToStorage.onPutFileToStorage(path);
        });
    }

    /**=================================================================================
     * getUrlFromStorage - Получаем URL фото из Storage
     *
     * @param path - путь в хранилище FirebaseStorage
     * @param onDataGetUrlFromStorage
     */
    public void getUrlFromStorage(StorageReference path, OnDataGetUrlFromStorage onDataGetUrlFromStorage) {
        // Получаем URL фото из Storage
        path.getDownloadUrl().addOnSuccessListener(uri -> {
            // В случае получения URL фото из Storage, вызываем интерфейс для дальнеших действий
            onDataGetUrlFromStorage.onGetUrlFromStorage(uri);
        });
    }

    /**=================================================================================
     * putUrlToDatabase - Вставляем URL фото в FirebaseDatabase
     *
     * @param photoUrl - uri фото
     * @param onDataPutUrlToDatabase
     */
    public void putUrlToDatabase(String photoUrl, OnDataPutUrlToDatabase onDataPutUrlToDatabase) {
        // вставляем в FirebaseDatabase
        ref_database_root.child(NODE_USERS)
                .child(current_uid)
                .child(CHILD_PHOTO_URL)
                .setValue(photoUrl)
                .addOnSuccessListener(unused -> {
                    // В случае вставки в FirebaseDatabase, вызываем интерфейс для дальнеших действий
                    onDataPutUrlToDatabase.onPutUrlToDatabase(photoUrl);
                });
    }

    /**==================================================================================
     * initUser - инициализация пользователя
     * <p>
     * Инициализация пользователя проходит во втором потоке
     * <p>
     * В случае успеха, вызывает интерфейс OnDataInitUser для дальнейших действий
     */
    public void initUser(OnDataInitUser onDataInitUser) {

        // Обращаемся к базе данных и скачиваем информацию ОДИН РАЗ!!
        ref_database_root.child(NODE_USERS).child(current_uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Скачиваем информацию о пользователе
                        user = snapshot.getValue(User.class);
                        // Если username пустое
                        if(user.username.isEmpty()) {
                            // То присваиваем пользователю в username в качестве значения ID
                            user.username = current_uid;
                        }
                        // В случае успеха, вызывает интерфейс OnDataInitUser для дальнейших действий
                        onDataInitUser.onInitUser();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }



    // endregion
}
