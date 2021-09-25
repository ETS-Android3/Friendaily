package com.example.comp90018_project;

public class User {

    private String uid;
    private String email;
    private String username;
    private String password;

    public User() {

    }

    public User(String uid, String email, String username, String password) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
