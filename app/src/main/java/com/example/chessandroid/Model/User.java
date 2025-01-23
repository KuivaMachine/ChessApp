package com.example.chessandroid.Model;

import android.graphics.Bitmap;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String nickname;
    private String email;
    private String password;
    private Bitmap image;
    private String mode;

    public User() {
    }

    public User(String nickname, String email, String password, Bitmap image) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.image = image;
    }

    public User(String nickname, String email, String password, String mode) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.mode=mode;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "User{" +
                "password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
