package com.example.comp90018_project.model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.Printer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class GPSTracker extends Service implements LocationListener {

    private Context context;
    private double lat;
    private double lng;
    private GeoFire geo;
    private String uid;
    private LocationManager locationManager;
    private boolean connected = false;
    final DatabaseReference localRef = FirebaseDatabase.getInstance().getReference("usersAvailable");
    final GeoFire geoFire = new GeoFire(localRef);
    final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    String TAG = "GPS";

    //The minimum time between updates in milliseconds
    private final long MIN_MINUTES = 1000 * 60 * 10;

    //The minimum distance to update in meters
    private final long MIN_DISTANCE = 100;

    public GPSTracker(Context context, GeoFire geo, String uid) {
        this.context = context;
        this.geo = geo;
        this.uid = uid;
        Log.i(TAG, "GPSTracker:Begin to get location ");
        getLocation();
    }


    public void getLocation() {
        if(ContextCompat.checkSelfPermission(context,PERMISSION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,PERMISSION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(context, "GPS Permission is Required.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "GPS Permission is Required.");
            return;
        }
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!(isGPSEnable || isNetworkEnable)) {
            Log.i(TAG, "located fail!");
            Toast.makeText(context, "located fail!", Toast.LENGTH_LONG).show();
        } else {
            connected = true;
//            if (isNetworkEnable) {
//                Log.i(TAG, "network is ok");
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                        MIN_MINUTES,
//                        MIN_DISTANCE,
//                        new LocationListener() {
//                            @Override
//                            public void onLocationChanged(@NonNull Location location) {
//                                if (location != null) {
//                                    lat = location.getLatitude();
//                                    lng = location.getAltitude();
//                                    updateDatabase();
//                                }
//                            }
//                        });
//                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                if (location != null) {
//                    lat = location.getLatitude();
//                    lng = location.getAltitude();
//                    updateDatabase();
//                }
//            }
            if(isGPSEnable){
                Log.i(TAG, "GPS is ok");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_MINUTES,
                        MIN_DISTANCE,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                if (location != null) {
                                    lat = location.getLatitude();
                                    lng = location.getAltitude();
                                    updateDatabase();
                                }
                            }
                            public void onStatusChanged(String provider,
                                                        int status,
                                                        Bundle extras){

                            }
                        });
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getAltitude();
                    updateDatabase();
                }
            }
        }

    }

    public void stopUsingGPS(){
        if(locationManager!=null){
            locationManager.removeUpdates(this);
        }
    }

    /**
     * This function is used to store geological location of user
     */
    public void updateDatabase(){
        geoFire.setLocation(uid, new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.i("Location update", "Update the location");
            }
        });

    }


    public Double getLat() {return lat;}

    public Double getLng() {return lng;}

    public boolean isConnected(){return connected;}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }



}
