package com.apushnikov.other351_mytelegram.database;

import com.google.firebase.storage.StorageReference;

/**=================================================================================
 * Интерфейс OnDataPutFileToStorage - действия, когда разместили файл (или изображение) в FirebaseStorage
 */
public interface OnDataPutFileToStorage {
    void onPutFileToStorage (StorageReference path);
}
