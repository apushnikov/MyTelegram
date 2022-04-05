package com.apushnikov.other351_mytelegram.models;

public class User {

    public String id = "";
    public String username = "";
    public String bio = "";
    public String phone = "";
    public String email = "";
    public String fullmane = "";
//    public String status = "";
    public String state = "";
    public String photoUrl = "empty";

    public User() {
    }

    public User(
            String id,
            String username,
            String bio,
            String phone,
            String email,
            String fullmane,
//            String status,
            String state,
            String photoUrl) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.phone = phone;
        this.email = email;
        this.fullmane = fullmane;
//        this.status = status;
        this.state = state;
        this.photoUrl = photoUrl;
    }


}
