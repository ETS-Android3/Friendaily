package com.example.comp90018_project.model;

import java.util.HashMap;
import java.util.Map;

public class UserNearby implements Comparable<UserNearby>{
    private String uid;
    private double distance;
    private String username;
    private Object avatar_url;

    public UserNearby(String uid, double distance, Map<String,Object> info){
        this.uid = uid;
        this.distance = distance;
        this.username = (String) info.get("name");
        this.avatar_url = info.get("avatar");
    }

    public String getUid() {return uid;}

    public double getDistance() {return distance;}

    public Object getAvatar_url() {return avatar_url;}

    public String getUsername() {return username;}

    public Map<String, Object> toMap(){
        Map<String,Object>  user = new HashMap<>();
        user.put("name",username);
        user.put("avatar",avatar_url);
        user.put("distance",distance);
        user.put("uid",uid);
        return user;
    }

    @Override
    public int compareTo(UserNearby userNearby) {
        return (int) (userNearby.distance - this.distance );
    }
}
