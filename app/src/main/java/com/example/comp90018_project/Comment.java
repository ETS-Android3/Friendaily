package com.example.comp90018_project;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Comment {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String uid;
    private String mid;
    private Long timestamp;
    private String content;

    public Comment(String uid,String mid,Long timestamp, Date date, String content){
        this.uid= uid;
        this.mid= mid;
        this.timestamp = timestamp;
        this.content = content;

    }

    public Comment(Map<String,Object> moment){
        this.uid= moment.get("uid").toString();
        this.mid= moment.get("mid").toString();
        this.timestamp = (Long) moment.get("timestamp");
        this.content = moment.get("content").toString();
    }

    public String getContent(){
        return content;
    }

    public  String getDate(){

        Date date = new Date(timestamp);
        return simpleDateFormat.format(date);
    }


    public Map<String,Object> toMap(){
        Map<String,Object> comment = new HashMap<>();
        Date date = new Date(System.currentTimeMillis());
        String DateTime = simpleDateFormat.format(date);
        comment.put("uid",uid);
        comment.put("mid",mid);
        comment.put("timestamp",timestamp);
        comment.put("content", content);
        return comment;
    }
}
