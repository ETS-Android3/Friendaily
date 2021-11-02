package com.example.comp90018_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.adapter.FriendAdapter;
import com.example.comp90018_project.R;
import com.example.comp90018_project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowFriendActivity extends AppCompatActivity {
    // if there is no user to be found, userfound will be null
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseUser currentUser;
    private ArrayList<Map<String,Object>> friendList = null;
    private ImageView backMain;
    private static final String TAG = "showFriend";
    public static final String EXTRA_MESSAGE = "com.example.comp90018_project.SHOW_MESSAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_friends);
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }else {
            backMain();
            friendView();
        }
    }

    private void backMain() {
        backMain = findViewById(R.id.friendBackMain);
        backMain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
    }

    private void friendView() {
        String UserID = currentUser.getUid();
        Log.i(TAG, UserID);
        CollectionReference friendRef = mDB.collection("users");
        Query query = friendRef.whereEqualTo("uid", UserID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    User user = new User(task.getResult().getDocuments().get(0).getData());
                    friendList = (ArrayList<Map<String,Object>>) user.getaddedFriends();
                    if (friendList != null) {
                        Log.d(TAG, "friend list" + friendList);
                        ListView FriendListview = (ListView) findViewById(R.id.friendsList);
                        List<Map<String, Object>> userfound_list = new ArrayList<Map<String, Object>>();
                        for (int i = 0; i < friendList.size(); i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            String avatar_url = (String) friendList.get(i).get("avatar_url");
                            if (avatar_url == null) {
                                map.put("avatar", R.drawable.a);
                            }
                            else {
                                map.put("avatar", friendList.get(i).get("avatar_url"));
                            }
                            map.put("name", friendList.get(i).get("username"));
                            userfound_list.add(map);
                        }
                        FriendAdapter adapter = new FriendAdapter(ShowFriendActivity.this);
                        adapter.setFriendList(userfound_list);
                        FriendListview.setAdapter(adapter);
                        FriendListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String selectUser = (String) friendList.get(position).get("uid");
                                Log.d(TAG, "selected:" + selectUser);
                                Intent intent = new Intent(ShowFriendActivity.this, ProfileActivity.class);
                                intent.putExtra(EXTRA_MESSAGE, selectUser);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        });

    }

    //if user does not log in, return to the Login
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(ShowFriendActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }
}