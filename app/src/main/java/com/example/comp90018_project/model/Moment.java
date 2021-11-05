package com.example.comp90018_project.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Moment {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String userID;
    private String date;
    private String content;
    private String image_url;
    private String username;
    private String user_avatar_url;

    public Moment(String uid, String date, String content, String image_url, String username, String user_avatar_url){
        this.userID= uid;
        this.date = date;
        this.content = content;
        this.image_url = image_url;
        this.username = username;
        this.user_avatar_url = user_avatar_url;
    }

    public Moment(Map<String,Object> moment){
        this.userID= moment.get("uid").toString();
        this.date = moment.get("date").toString();
        this.content = moment.get("content").toString();
        this.image_url = moment.get("image_download_url").toString();
        this.username = moment.get("username").toString();
        this.user_avatar_url = moment.get("user_avatar_url").toString();
    }

    public String getContent(){
        return content;
    }

    public String getDate(){
        return date;
    }

    public String getImage_url(){
        return image_url;
    }

    public String getUsername() {
        return username;
    }

    public String getUser_avatar_url() {
        return user_avatar_url;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> moment = new HashMap<>();
        moment.put("uid",userID);
        moment.put("date",date);
        moment.put("content", content);
        moment.put("image_download_url",image_url);
        moment.put("username", username);
        moment.put("user_avatar_url", user_avatar_url);
        return moment;
    }

}
