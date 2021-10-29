package com.example.comp90018_project.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.comp90018_project.Util.GeoDistance;
import com.example.comp90018_project.model.GPSTracker;
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
    private List< Map <String,Object> > usersNearby;
    private static final int LOCATION_PERM_CODE = 2;
    final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_geo_query);
        usersNearby = new ArrayList<Map<String,Object>>();
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
        //Permission will be asked when we create gps tracker
        if(ContextCompat.checkSelfPermission(this,PERMISSION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,PERMISSION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {PERMISSION}, LOCATION_PERM_CODE);

        }

        if(gps == null){
            gps = new GPSTracker(this,geoFire, mAu.getCurrentUser().getUid());
        }


//        GeoLoc geo = new GeoLoc(currentUser.getUid(),51.5074,0.1278,System.currentTimeMillis());
//        writeToFirebase(geo);
//        showUsersAvailable(geo);
    }

    protected  void onDestroy() {
        super.onDestroy();
        if(gps!=null){
            gps.stopUsingGPS();
        }

    }


    /**
     * Find every user whose location is stored in userAvailable in real time database
     * @param geo the geoLoc instance of center
     * @param radius the range of distance
     */
    public void findUserNearby(GPSTracker geo, double radius) {
        if (gps.isConnected()) {
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(geo.getLat(), geo.getLng()), radius);
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                @Override
                public void onDataEntered(DataSnapshot user, GeoLocation location) {
                    //Find users nearby
                    //This event listener will return users who has existed in database and new user nearby
                    String uid = user.getKey();
                    if (!uid.equals(mAu.getCurrentUser().getUid()) && user.getValue() != null) {
                        Log.i(TAG, "found a user  " + uid);
                        Map<String, Object> loc = new HashMap<>();
                        loc.put("lat", location.latitude);
                        loc.put("lng", location.longitude);
                        loc.put("uid", uid);
                        usersNearby.add(loc);
                    }
                }

                /**
                 * Remove the user that exit from database
                 *
                 * @param user the user remove from database
                 */
                @Override
                public void onDataExited(DataSnapshot user) {
                    String uid = user.getKey();
                    for (Map<String, Object> userNearby : usersNearby) {
                        if (userNearby.get("uid") == uid) {
                            usersNearby.remove(userNearby);
                        }
                    }
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

    /**
     * When we click the button: find nearby user, a existing nearby user list will be returned
     * Nearby users are arranged based on the distance, start with the nearest
     * @param geo the location of the current user
     * @return the list of nearby users
     */
    public ArrayList<GeoDistance> showUsersAvailable (GPSTracker geo){
        ArrayList<GeoDistance> geoDistances = new ArrayList<GeoDistance>();
        GeoLocation center = new GeoLocation(geo.getLat(),geo.getLng());
        if(usersNearby.size() != 0){
            for( Map <String,Object>  user:usersNearby){
                GeoLocation userLoction = new GeoLocation((double) user.get("lat"),(double) user.get("lng"));
                double distance = GeoFireUtils.getDistanceBetween(userLoction,center);
                if (distance <= radius){
                    GeoDistance geodistance = new GeoDistance((String) user.get("uid"), distance);
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

    /**
     *If this user don't wannt to share their location or log off
     * Their location will be removed from database
     */
    public void makeUnavailable(){
        localRef.child(mAu.getCurrentUser().getUid()).removeValue();

    }

}