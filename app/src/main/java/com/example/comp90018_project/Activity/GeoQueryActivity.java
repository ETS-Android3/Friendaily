package com.example.comp90018_project.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comp90018_project.adapter.FriendAdapter;
import com.example.comp90018_project.adapter.UserNearbyAdapter;
import com.example.comp90018_project.model.UserNearby;
import com.example.comp90018_project.model.GPSTracker;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.example.comp90018_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeoQueryActivity extends AppCompatActivity {

    private ArrayList<UserNearby> usersNearby;
    Map<String,Object> userInfo = new HashMap<>();

    private ImageView search;
    private ImageView backMain;
    private GPSTracker gps;

    final String TAG = "GeoQuery";
    //find users within 1 kilometers
    final double radius = 1;
    final DatabaseReference localRef = FirebaseDatabase.getInstance().getReference("usersAvailable");
    final CollectionReference userRef = FirebaseFirestore.getInstance().collection("users");
    final GeoFire geoFire = new GeoFire(localRef);
    final FirebaseAuth mAu = FirebaseAuth.getInstance();
    final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERM_CODE = 2;
    public static final String EXTRA_MESSAGE = "com.example.comp90018_project.FIND_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userInfo = new HashMap<>();
        setContentView(R.layout.activity_geo_query);
        usersNearby = new ArrayList<UserNearby>();
        search = findViewById(R.id.searchNearby);
        backMain = findViewById(R.id.nearByBackMain);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUsersAvailable();

            }
        });
        backMain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
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

        }else{
            if(gps == null){
                gps = new GPSTracker(this,geoFire, mAu.getCurrentUser().getUid());
            }
            findUserNearby(gps,radius);
        }



    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(gps == null){
                    gps = new GPSTracker(this,geoFire, mAu.getCurrentUser().getUid());
                }
                findUserNearby(gps,radius);
            }else {
                Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
            }
        }

    }


    protected  void onDestroy() {
        super.onDestroy();
        if(gps!=null){
            gps.stopUsingGPS();
        }

    }


    /**
     * Find every user whose location is stored in userAvailable in real time database
     * And find their information from user database
     * Store uid, distance, avatar url and username
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
                        GeoLocation loc = new GeoLocation(location.latitude,location.longitude);
                        GeoLocation center = new GeoLocation(geo.getLat(),geo.getLng());
                        double distance = GeoFireUtils.getDistanceBetween(loc,center);
                        //Check whether this location is within radius or not
                        //If it is, add to available users
                        if (distance <= radius){
                            //Retrieve this user's information from users database
                            addToList(uid,distance);

                        }
                    }
                }

                /**
                 * Remove the user that exit database
                 * @param user the user remove from database
                 */
                @Override
                public void onDataExited(DataSnapshot user) {
                    String uid = user.getKey();
                    for (UserNearby userNearby : usersNearby) {
                        if (userNearby.getUid() == uid) {
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
     * When we click the button: the found nearby users will be returned
     */
    public void showUsersAvailable (){
        ListView UserListview = (ListView) findViewById(R.id.nearbyList);
        UserNearbyAdapter adapter = new UserNearbyAdapter(GeoQueryActivity.this);
        if(usersNearby.size() == 0){
            Toast.makeText(this, "No user nearby!", Toast.LENGTH_SHORT).show();
        }else{
            Log.i(TAG, "Let's show the people nearby");
            adapter.setUsersList(usersNearby);
            UserListview.setAdapter(adapter);
            UserListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectUser = (String) usersNearby.get(position).getUid();
                    Log.d(TAG, "selected:" + selectUser);
                    Intent intent = new Intent(GeoQueryActivity.this, ProfileActivity.class);
                    intent.putExtra(EXTRA_MESSAGE, selectUser);
                    startActivity(intent);
                }
            });
        }

    }

    /**
     * Add this user to the user nearby list if they exist
     * @param uid
     * @param distance
     */
    public void addToList(String uid, double distance){
        userInfo = new HashMap<>();
        Query query = userRef.whereEqualTo("uid",uid);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().getDocuments().size() == 0) {
                    //dont find anything, do nothing
                } else {
                    //Get the corresponding information
                    Log.i(TAG, String.valueOf(task.getResult().getDocuments().get(0).getData()));
                    Map<String, Object> userGet = task.getResult().getDocuments().get(0).getData();
                    if(userGet.get("avatar_url") == null){
                        userInfo.put("avatar", R.drawable.default_user_avatar);
                    }else{
                        userInfo.put("avatar", userGet.get("avatar_url"));
                    }
                    userInfo.put("name", userGet.get("username"));
                    Log.i(TAG, String.valueOf(userInfo));
                    Log.i(TAG, "Find user");
                    UserNearby newUser = new UserNearby((String) uid, distance,userInfo);
                    Log.i(TAG, "Add user to the list!");
                    usersNearby.add(newUser);
                }
            }
        });

    }

    /**
     *If this user don't wannt to share their location or log off
     * Their location will be removed from database
     */
    public void makeUnavailable(){
        localRef.child(mAu.getCurrentUser().getUid()).removeValue();

    }

}