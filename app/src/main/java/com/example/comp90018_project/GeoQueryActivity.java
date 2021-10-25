package com.example.comp90018_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoFireUtils;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoQueryActivity extends AppCompatActivity {

    final String TAG = "GeoQuery";
    //find users within 1 kilometers
    final double radius = 1;
    final DatabaseReference localRef = FirebaseDatabase.getInstance().getReference("usersAvailable");
    final GeoFire geoFire = new GeoFire(localRef);
    final FirebaseAuth mAu = FirebaseAuth.getInstance();
    private List<GeoLoc> usersNearby;

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
        usersNearby = new ArrayList<GeoLoc>();
//        GeoLoc geo = new GeoLoc(currentUser.getUid(),51.5074,0.1278,System.currentTimeMillis());
//        writeToFirebase(geo);
//        showUsersAvailable(geo);
    }

    // TODO: 2021/10/26 Add geo location detector

    /**
     * This function is used to store geological location of user
     * @param geo includes uid, location and the time changed
     */
    public void writeToFirebase(GeoLoc geo) {
        geoFire.setLocation(geo.getUid(), new GeoLocation(geo.getLat(), geo.getLng()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //Update users' location to the database
                Log.i(TAG, "Has stored geo location in the realtime database ");
                Map<String, Object> last_changed = new HashMap<>();
                last_changed.put("last_changed", geo.getLast_changed());
                localRef.child(geo.getUid()).updateChildren(last_changed).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i(TAG, "Has updated the time ");
                        //After all the information, including location and the update time, stored in the database
                        //Begin to search for nearby user
                        findUserNearby(geo, radius);
                    }
                });
            }
        });
    }

    /**
     * Find every user whose location is stored in userAvailable in real time database
     * @param geo the geoLoc instance of center
     * @param radius the range of distance
     */
    public void findUserNearby(GeoLoc geo, double radius){
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(geo.getLat(), geo.getLng()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot user, GeoLocation location) {
                //Find users nearby
                //This event listener will return users who has existed in database and new user nearby
                String uid = user.getKey();
                if(!uid.equals(mAu.getCurrentUser().getUid())){
                    Log.i(TAG, "found a user  "+ uid);
                    Map <String,Object> userLoc = (Map<String, Object>) user.getValue();
                    userLoc.put("uid",uid);
                    GeoLoc userNearby = new GeoLoc(userLoc);
                    usersNearby.add(userNearby);


                }


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

    /**
     * When we click the button: find nearby user, a existing nearby user list will be returned
     * Nearby users are arranged based on the distance, start with the nearest
     * @param geo the location of the current user
     * @return the list of nearby users
     */
    public ArrayList<GeoDistance> showUsersAvailable (GeoLoc geo){
        ArrayList<GeoDistance> geoDistances = new ArrayList<GeoDistance>();
        GeoLocation center = new GeoLocation(geo.getLat(),geo.getLng());
        if(usersNearby.size() != 0){
            for(GeoLoc user:usersNearby){
                GeoLocation userLoction = new GeoLocation(user.getLat(),user.getLng());
                double distance = GeoFireUtils.getDistanceBetween(userLoction,center);
                if (distance <= radius){
                    GeoDistance geodistance = new GeoDistance(user.getUid(), distance);
                    geoDistances.add(geodistance);
                }
            }
            Collections.sort(geoDistances);
            return geoDistances;
        }else{
            Log.i(TAG, "Cannot find user nearby");
            return null;
        }

    }

}