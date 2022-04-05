package com.apushnikov.other351_mytelegram.myApplication;

import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.initFarebase;

import android.app.Application;

import com.apushnikov.other351_mytelegram.log_file.LogFile;

/**========================================================================================
 * MyApplication - Базовый класс для поддержания глобального состояния приложения.
 * <p>
 * Вы можете предоставить свою собственную реализацию, создав подкласс и указав полное имя
 * этого подкласса в качестве атрибута «android: name» в теге <application> вашего
 * AndroidManifest.xml.
 * <p>
 * Класс Application или ваш подкласс класса Application создается перед любым другим классом
 * при создании процесса для вашего приложения / пакета.
 * ========================================================================================
 */
public class MyApplication extends Application {

    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    public static LogFile logFile;

    @Override
    public void onCreate() {
        super.onCreate();

        /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
         * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
         */
        logFile = new LogFile(this);

        initFarebase();

    }
}
