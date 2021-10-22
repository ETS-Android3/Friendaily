package com.example.comp90018_project;

import java.util.HashMap;
import java.util.Map;

/**
 * This is use to create a instance which record user id, moment id, time
 */
public class Like {
    private String uid;
    private String mid;
    private Long timestamp;

    public Like (String uid, String mid, Long timestamp){
        this.uid = uid;
        this.mid = mid;
        this.timestamp = timestamp;
    }

    public Like (Map<String,Object> like){
        this.uid = like.get("uid").toString();
        this.mid = like.get("mid").toString();
        this.timestamp = (long)like.get("timestamp");
    }

    public Map<String,Object> toMap(){
        Map like = new HashMap();
        like.put("uid",uid);
        like.put("mid",mid);
        like.put("timestamp",timestamp);
        return like;
    }
}
