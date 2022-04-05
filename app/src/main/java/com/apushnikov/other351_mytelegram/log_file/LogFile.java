package com.apushnikov.other351_mytelegram.log_file;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
 * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
 * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
 */
public class LogFile {

    Context context;

    private final static String FILE_NAME = "document.txt";
    private FileOutputStream fos;

    private int currentNum = 0;

    /**OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     * ЭТО ДЛЯ ТЕСТИРОВАНИЯ
     * OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
     */
    public LogFile(Context context) {
        this.context = context;
        try {
            fos = new FileOutputStream(getExternalPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        };
    }

    private File getExternalPath() {
        return new File(context.getExternalFilesDir(null), FILE_NAME);
    }

    public void writeLogFile(String stringToWriteLog) {
        ++currentNum;
        stringToWriteLog = "" + currentNum +": " + Thread.currentThread().getName() + ": " + stringToWriteLog + "\n";
        try {
            fos.write(stringToWriteLog.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeLogFile() {
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
