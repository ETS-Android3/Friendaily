package com.example.comp90018_project.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {

    private String uid;
    private String email;
    private String username;
    private String password;
    private String bio;
    private String avatarUrl;
    private ArrayList<Map<String,Object>> pendingFriends;
    private ArrayList<Map<String,Object>> addedFriends;

    public User(String email, String username, String password) {
        this.uid = null;
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = null;
        this.avatarUrl = null;
        this.pendingFriends = new ArrayList<Map<String,Object>>();
        this.addedFriends = new ArrayList<Map<String,Object>>();
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
        if (user.get("bio") != null) {
            this.bio = user.get("bio").toString();
        }
        if (user.get("avatar_url") != null) {
            this.avatarUrl = user.get("avatar_url").toString();
        }
        this.pendingFriends = (ArrayList<Map<String,Object>>) user.get("pendingFriends");
        this.addedFriends = (ArrayList<Map<String,Object>>) user.get("friends");
    }

    public User() {
        this.uid = null;
        this.email = null;
        this.username = null;
        this.password = null;
        this.bio = null;
        this.avatarUrl = null;
        this.pendingFriends = new ArrayList<Map<String,Object>>();
        this.addedFriends = new ArrayList<Map<String,Object>>();
    }

    /**
     * Return a map for update database
     * @return Map<Sring,String></Sring,String>
     */
    public Map<String,Object> toMap(){
        Map<String,Object> user = new HashMap<>();
        user.put("uid",uid);
        user.put("username",username);
        user.put("email", email);
        user.put("password",password);
        user.put("bio", bio);
        user.put("avatar_url", avatarUrl);
        user.put("pendingFriends", pendingFriends);
        user.put("friends", addedFriends);
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

    public void AddPendingFriends(Map<String,Object> pendingFriends) {
        this.pendingFriends.add(pendingFriends);
    }

    public void removePendingFriends(User pendingFriends) {
        this.pendingFriends.remove(pendingFriends);
    }

    public ArrayList<Map<String,Object>> getpendingFriends() {
        return pendingFriends;
    }

    public void addFriends(Map<String,Object> addedFriends) {
        this.addedFriends.add(addedFriends);
    }

    public void removeFriends(Map<String,Object> addedFriends) {
        this.addedFriends.remove(addedFriends);
    }

    public ArrayList<Map<String,Object>> getaddedFriends() {
        return addedFriends;
    }
}
