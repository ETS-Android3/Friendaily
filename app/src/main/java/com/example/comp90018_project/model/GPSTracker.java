package com.example.comp90018_project.model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Printer;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.comp90018_project.Activity.GeoQueryActivity;
import com.example.comp90018_project.Activity.MainActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.location.LocationCallback;

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
    private com.google.android.gms.location.LocationCallback locationCallback;

    final DatabaseReference localRef = FirebaseDatabase.getInstance().getReference("usersAvailable");
    final GeoFire geoFire = new GeoFire(localRef);
    final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    String TAG = "GPS";

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    //The minimum time between updates in milliseconds
    private final long MIN_MINUTES = 1000 * 60 * 10;

    //The minimum distance to update in meters
    private final long MIN_DISTANCE = 100;

    public GPSTracker(Context context, GeoFire geo, String uid) {
        this.context = context;
        this.geo = geo;
        this.uid = uid;
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult == null){
                    Log.i(TAG, "onLocationResult: We cannot get the location!");
                    return;
                }
                for(Location location:locationResult.getLocations()){
                    Log.i(TAG,"Latitude is: " + location.getLatitude());
                    Log.i(TAG,"Longitude is: " + location.getLongitude());
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                }
            }
        };
        Log.i(TAG, "GPSTracker:Begin to get location ");
        getLocation();
    }

    /**
     * It can set up GPS for track the change of location
     * And update location to the database once changed
     */
    @SuppressLint("MissingPermission")


    public void getLocation(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(MIN_MINUTES);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        //check the setting
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Log.i(TAG, "Fail to check setting:" + e.toString());
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult((Activity) context,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                FusedLocationProviderClient locationProviderClient = new FusedLocationProviderClient(context);
                locationProviderClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
            }
        });
    }

    /**
     * This function can stop using gps
     */
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
