package com.example.comp90018_project.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Comment {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String userID;
    private String cid;
    private String date;
    private String content;
    private String username;
    private String user_avatar_url;

    public Comment(String uid,String cid, String date, String content, String username, String user_avatar_url){
        this.userID= uid;
        this.cid = cid;
        this.date = date;
        this.content = content;
        this.username = username;
        this.user_avatar_url = user_avatar_url;
    }

    public String getUserID() {
        return userID;
    }

    public String getCid() {
        return cid;
    }

    public String getContent(){
        return content;
    }

    public Comment(Map<String,Object> comment){
        this.userID= comment.get("uid").toString();
        this.cid = comment.get("cid").toString();
        this.date = comment.get("date").toString();
        this.content = comment.get("content").toString();
        this.username = comment.get("username").toString();
        this.user_avatar_url = comment.get("user_avatar_url").toString();
    }


    public String getDate(){
//        Date date = new Date(timestamp);
//        return simpleDateFormat.format(date);
        return date;
    }

    public String getUsername() {
        return username;
    }

    public String getUser_avatar_url() {
        return user_avatar_url;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> comment = new HashMap<>();

        comment.put("uid",userID);
        comment.put("cid",cid);
        comment.put("date",date);
        comment.put("content", content);
        comment.put("username", username);
        comment.put("user_avatar_url", user_avatar_url);
        return comment;
    }
}
