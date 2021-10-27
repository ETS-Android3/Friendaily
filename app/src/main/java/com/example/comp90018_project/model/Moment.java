package com.example.comp90018_project.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Moment {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String uid;
    private Long timestamp;
    private String content;
    private String image_url;

    public Moment(String uid, Long timestamp, String content, String image_url){
        this.uid= uid;
        this.timestamp = timestamp;
        this.content = content;
        this.image_url = image_url;

    }

    public Moment(Map<String,Object> moment){
        this.uid= moment.get("uid").toString();
        this.timestamp = (Long) moment.get("timestamp");
        this.content = moment.get("content").toString();
        this.image_url = moment.get("image_download_url").toString();
    }

    public String getContent(){
        return content;
    }

    public  String getDate(){
        Date date = new Date(timestamp);
        return simpleDateFormat.format(date);
    }

    public String getImage_url(){
        return image_url;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> moment = new HashMap<>();
        Date date = new Date(System.currentTimeMillis());
        String DateTime = simpleDateFormat.format(date);
        moment.put("uid",uid);
        moment.put("timestamp",timestamp);
        moment.put("content", content);
        moment.put("image_download_url",image_url);
        return moment;
    }

}
