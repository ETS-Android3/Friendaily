package com.example.comp90018_project.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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

public class FindNewFriendActivity extends AppCompatActivity {
    private EditText content;
    private ImageButton search;
    private ImageView backMain;
    // if there is no user to be found, userfound will be null
    private User userfound;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private static final String TAG = "FindNewFriend";
    public static final String EXTRA_MESSAGE = "com.example.comp90018_project.FIND_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_find_new_friend);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }else {
            searchEvent();
        }
    }

    private void searchEvent() {
        search = findViewById(R.id.searchButton);
        backMain = findViewById(R.id.backMain);
        search.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finduser();
                        Log.i(TAG, String.valueOf(userfound));
                    }
                }
        );
        backMain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
    }

    public void finduser(){
        content = findViewById(R.id.search_bar);
        String search_content = content.getText().toString();
        if (!search_content.isEmpty()){
            CollectionReference userRef = mDB.collection("users");
            if (search_content.contains("@")) {
                Query emailQuery = userRef.whereEqualTo("email", search_content);
                getQuery(emailQuery);
            }
            else {
                Query query = userRef.whereEqualTo("username", search_content);
                getQuery(query);
            }
        }
        else {
            ListView FriendListview = (ListView) findViewById(R.id.searchList);
            Toast.makeText(FindNewFriendActivity.this, "You don't search for a user", Toast.LENGTH_LONG).show();
            FriendListview.setAdapter(null);
        }

    }

    private void getQuery(Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ListView FriendListview = (ListView) findViewById(R.id.searchList);
                try {
                    if (task.getResult().getDocuments().get(0).getData() == null) {
                        //If this username isn't stored in database
                        Log.i(TAG, "We cannot find this user");
                        Toast.makeText(FindNewFriendActivity.this, "We cannot find this user", Toast.LENGTH_LONG).show();
                    } else {
                        //Get the corresponding email address
                        Log.i(TAG, String.valueOf(task.getResult().getDocuments().get(0).getData()));
                        userfound = new User(task.getResult().getDocuments().get(0).getData());
                        Log.i(TAG, "Find user");
                    }
                    if (userfound != null) {
                        List<Map<String, Object>> userfound_list = new ArrayList<Map<String, Object>>();
                        Map<String, Object> map = new HashMap<String, Object>();
                        if (userfound.getAvatarUrl() == null) {
                            map.put("avatar", R.drawable.a);
                        }
                        else {
                            map.put("avatar", userfound.getAvatarUrl());
                        }
                        map.put("name", userfound.getUsername());
                        userfound_list.add(map);
                        FriendAdapter adapter = new FriendAdapter(FindNewFriendActivity.this);
                        adapter.setFriendList(userfound_list);
                        FriendListview.setAdapter(adapter);
                        FriendListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String selectUser = userfound.getUid();
                                Log.d(TAG, "selected:" + selectUser);
                                Intent intent = new Intent(FindNewFriendActivity.this, ProfileActivity.class);
                                intent.putExtra(EXTRA_MESSAGE, selectUser);
                                startActivity(intent);
                            }
                        });
                    }
                }
                catch (java.lang.IndexOutOfBoundsException e) {
                    Log.i(TAG, "User is not exist");
                    Toast.makeText(FindNewFriendActivity.this, "User is not exist", Toast.LENGTH_LONG).show();
                    FriendListview.setAdapter(null);
                }
            }
        });
    }

    //if user does not log in, return to the Login
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(FindNewFriendActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }
}