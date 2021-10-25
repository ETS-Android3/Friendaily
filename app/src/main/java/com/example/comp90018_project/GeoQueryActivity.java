package com.example.comp90018_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;

public class GeoQueryActivity extends AppCompatActivity {

    final String TAG = "GeoQuery";
    //find users within 1 kilometers
    final double radius = 1;
    final DatabaseReference localRef = FirebaseDatabase.getInstance().getReference("usersAvailable");
    final GeoFire geoFire = new GeoFire(localRef);
    final FirebaseAuth mAu = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_query);
        // TODO: 2021/10/25 layout
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAu.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(GeoQueryActivity.this,RegisterActivity.class);
            finish();
            startActivity(intent);
        }
    }

    /**
     * This function is used to store geological location of user
     * @param geo includes uid, location and the time changed
     */
    public void writeToFirebase(GeoLoc geo) {
        geoFire.setLocation(geo.getUid(), new GeoLocation(geo.getLat(), geo.getLng()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.i(TAG, "Has stored geo location in the realtime database ");
                Map<String, Object> last_changed = new HashMap<>();
                last_changed.put("last_changed", geo.getLast_changed());
                localRef.child(geo.getUid()).updateChildren(last_changed).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i(TAG, "Has updated the time ");
                    }
                });
            }
        });
    }

    public void findUserNeraby(GeoLoc geo, double radius){
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(geo.getLat(), geo.getLng()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot user, GeoLocation location) {
                String uid = user.getKey();
                Log.i(TAG, "found a user  "+ uid);

            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

}