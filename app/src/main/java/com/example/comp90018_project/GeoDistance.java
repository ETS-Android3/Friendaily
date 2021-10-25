package com.example.comp90018_project;

public class GeoDistance implements Comparable<GeoDistance>{
    private String uid;
    private double distance;

    public GeoDistance(String uid, double distance){
        this.uid = uid;
        this.distance = distance;
    }

    public String getUid() {return uid;}

    public double getDistance() {return distance;}

    @Override
    public int compareTo(GeoDistance geoDistance) {
        return (int) (geoDistance.distance - this.distance );
    }
}
