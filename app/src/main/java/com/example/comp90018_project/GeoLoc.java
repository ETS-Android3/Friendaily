package com.example.comp90018_project;

import java.util.HashMap;
import java.util.Map;

public class GeoLoc {
    private String uid;
    private double lat;
    private double lng;
    private Long last_changed;

    public GeoLoc(String uid,double lat, double lng, Long last_changed){
        this.uid = uid;
        this.lat = lat;
        this.lng = lng;
        this.last_changed = last_changed;

    }

    public  GeoLoc(Map<String,Object> geo){
        this.uid = (String) geo.get("uid");
        this.lat = (double) geo.get("lat");
        this.lng = (double) geo.get("lng");
        this.last_changed = (Long) geo.get("last_changed");
    }

    public Map<String,Object> toMap(){
        Map<String,Object> geo = new HashMap<>();
        geo.put("uid",uid);
        geo.put("lat",lat);
        geo.put("lng",lng);
        geo.put("last_changed",last_changed);
        return geo;
    }

    public String getUid() {return uid;}

    public Double getLat() {return lat;}

    public Double getLng() {return lng;}

    public Long getLast_changed() {return last_changed;}
}
