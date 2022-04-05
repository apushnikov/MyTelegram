package com.apushnikov.other351_mytelegram.utilites;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.apushnikov.other351_mytelegram.R;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Target;

import de.hdodenhof.circleimageview.CircleImageView;

public class Utilities {

    /**==================================================================================
     * hideKeyBoard - Скрыть клавиатуру
     */
/*    public static void hideKeyBoard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
    }*/


    /**==================================================================================
     *downloadAndSetImage - Скачивает и устанавливает картинку в указанный view
     *
     * @param url - url-адрес картинки
     * @param view - должен быть типа ImageView
     */
    public static void downloadAndSetImage(String url, View view) {
        // Используем библиотеку Picasso - загружаем фото и помещаем во View
        Picasso.get()
                .load(url)
                .fit()                                      // Попытайтесь изменить размер изображения,
                                                            // чтобы оно точно соответствовало границам
                                                            // целевого ImageView
                .placeholder(R.drawable.default_photo)      // Если нет связи с интернетом
                                                            // картина по умолчанию
                .into((ImageView) view);
    }


}
