package com.apushnikov.other351_mytelegram.database;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

/**=================================================================================
 * Интерфейс OnDataGetUrlFromStorage - действия, когда получает  URL картинки из хранилища в FirebaseStorage
 */
public interface OnDataGetUrlFromStorage {
    void onGetUrlFromStorage (Uri uri);
}
