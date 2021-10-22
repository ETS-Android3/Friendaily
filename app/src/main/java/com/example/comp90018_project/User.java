package com.example.comp90018_project;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String uid;
    private String email;
    private String username;
    private String password;
    private String bio;
    private String avatarUrl;

    public User(String email, String username, String password) {
        this.uid = null;
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = null;
        this.avatarUrl = null;
    }
    public User(String uid, String email, String username, String password, String bio, String avatarUrl) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public User(Map<String,Object> user) {
        this.uid = user.get("uid").toString();
        this.email = user.get("email").toString();
        this.username = user.get("username").toString();
        this.password = user.get("password").toString();
        this.bio = user.get("bio").toString();
        this.avatarUrl = user.get("avatar_url").toString();
    }

    /**
     * Return a map for update database
     * @return Map<Sring,String></Sring,String>
     */
    public Map<String,String> toMap(){
        Map<String,String> user = new HashMap<>();
        user.put("uid",uid);
        user.put("username",username);
        user.put("email", email);
        user.put("password",password);
        user.put("bio", bio);
        user.put("avatar_url", avatarUrl);
        return user;
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

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }

    public void setAvatarUrl(String url) {
        this.avatarUrl = url;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
