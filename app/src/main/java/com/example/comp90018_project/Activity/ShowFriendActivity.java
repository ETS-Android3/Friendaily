package com.example.comp90018_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.adapter.FriendAdapter;
import com.example.comp90018_project.R;
import com.example.comp90018_project.model.Friend;
import com.example.comp90018_project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
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
    //Connect to message - [My user ID] - [Unread]
    private CollectionReference myChat;
    private CollectionReference messageRef = FirebaseFirestore.getInstance().collection("messages");
    private ArrayList<Friend> friendsFound;

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
            Log.i(TAG, "reload");
            reload();
        }else {
            myChat = messageRef.document(currentUser.getUid()).collection("Chat");
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
                        friendsFound = new ArrayList<>();

                        //Construct friend list from exist friend
                        for (int i = 0; i < friendList.size(); i++) {
                            Map<String, Object> f = friendList.get(i);
                            Friend friend = new Friend((String) f.get("uid"), (String) f.get("username"), (String) f.get("avatar_url"));
                            friendsFound.add(friend);
                        }

                        //Get chat situation and update friendsFound List
                        myChat.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot result) {
                                List<DocumentSnapshot> dcs = result.getDocuments();
                                if(!dcs.isEmpty()){
                                    Log.i(TAG,"New the friend list is:" + friendsFound.toString() );
                                    for (DocumentSnapshot f : dcs) {
                                        Log.i(TAG,"Some one send a new msg:" + f.getData().toString());
                                        Friend friend = new Friend((String) f.getId(), f.getData());
                                        int i = friendsFound.indexOf(friend);
                                        Log.i(TAG,"This man is in the:" + i);
                                        if (i != -1) {
                                            //This user is a added friend
                                            Friend info = friendsFound.get(i);
                                            Log.i(TAG,"sender is:" + info.getUsername());
                                            friendsFound.remove(info);
                                            friend.setInfo(info);
                                            friendsFound.add(info);
                                        }
                                    }
                                }

                                //Sort this list so that friends will be shown in order
                                Collections.sort(friendsFound);

                                //Convert friendsFound list to the list of map
                                List<Map<String, Object>> userfound_list = new ArrayList<Map<String, Object>>();
                                for (Friend f: friendsFound) {
                                    userfound_list.add(f.toMap());
                                }

                                //Set adapter
                                FriendAdapter adapter = new FriendAdapter(ShowFriendActivity.this);
                                adapter.setFriendList(userfound_list);
                                FriendListview.setAdapter(adapter);
                                FriendListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        String selectUser = (String) userfound_list.get(position).get("uid");
                                        Log.d(TAG, "selected:" + selectUser);
                                        Intent intent = new Intent(ShowFriendActivity.this, ProfileActivity.class);
                                        intent.putExtra(EXTRA_MESSAGE, selectUser);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ShowFriendActivity.this, "Fail to get unread message!", Toast.LENGTH_LONG).show();
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