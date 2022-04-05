package com.apushnikov.other351_mytelegram.utilites;

import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.CHILD_STATE;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.NODE_USERS;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.current_uid;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.ref_database_root;
import static com.apushnikov.other351_mytelegram.utilites.GlobalConstants.user;

import com.google.android.gms.tasks.OnSuccessListener;

/**=================================================================================
 * AppStates - статусы состояния пользователя в сети
 */
public enum AppStates {
    ONLINE("В сети"),
    OFFLINE("был недавно"),
    TYPING("печатает");

    private final String state;

    private  String ttt;

    AppStates(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    /**=================================================================================
     * updateState - устанавливает статус, обновляет базу данных
     *
     * @param appStates - статус
     */
    public static void updateState(AppStates appStates) {
        ref_database_root.child(NODE_USERS).child(current_uid).child(CHILD_STATE)
                .setValue(appStates.getState())
                .addOnSuccessListener(unused -> {
                    user.state = appStates.getState();
                });
    }

}
