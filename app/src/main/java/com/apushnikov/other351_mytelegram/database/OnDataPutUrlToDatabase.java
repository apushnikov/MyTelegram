package com.apushnikov.other351_mytelegram.database;

import android.net.Uri;

/**=================================================================================
 * Интерфейс OnDataPutUrlToDatabase - действия, после отправки полученного  URL в базу данных FirebaseDatabase
 */
public interface OnDataPutUrlToDatabase {
    void onPutUrlToDatabase (String photoUrl);
}
