package com.example.comp90018_project.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.comp90018_project.R;
import com.example.comp90018_project.adapter.MomentAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMomentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseUser currentUser;
    private ImageView backMain;
    private static final String TAG = "MyMoment";
    private List<DocumentSnapshot> momentList;
    private String USERID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_my_moment);
        backMain = findViewById(R.id.myMomentBackMain);
        currentUser = mAuth.getCurrentUser();
        USERID = currentUser.getUid();
        if (currentUser == null) {
            reload();
        } else {
            momentView();
            backMain.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }
            );
        }
    }

    private void momentView() {
        // Get moment query and set adapter
        DocumentReference docRef = mDB.collection("moments").document(USERID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d(TAG, "===============================TRY TO GET MOMENTS LIST ========================================");

                    Log.d(TAG, "===============================" +  task.getResult().getData().size() + "========================================");
                    ArrayList<Map<String, Object>> moments_list = (ArrayList<Map<String, Object>>) task.getResult().getData().get("all_friends_moments");
                    if (moments_list != null) {
                        Log.d(TAG, "moments list get");
                        ListView MomentListview = (ListView) findViewById(R.id.myMomentList);
                        List<Map<String, Object>> momentfound_list = new ArrayList<Map<String, Object>>();
                        for (int i=0; i < moments_list.size(); i++) {
                            if (moments_list.get(i).get("uid").equals(USERID)) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                Map<String, Object> moment_map = moments_list.get(i);
                                String avatar_url = (String) moment_map.get("user_avatar_url");
                                if (avatar_url == null) {
                                    map.put("avatar", "");
                                } else {
                                    map.put("avatar", moment_map.get("user_avatar_url"));
                                }
                                map.put("uid", moment_map.get("uid"));
                                map.put("name", moment_map.get("username"));
                                map.put("content", moment_map.get("content"));
                                map.put("image", moment_map.get("image_url"));
                                map.put("timestamp", moment_map.get("date"));
                                map.put("activity", moment_map.get("myMoment"));
                                momentfound_list.add(map);
                            }
                        }
                        MomentAdapter adapter = new MomentAdapter(MyMomentActivity.this);
                        adapter.setMomentList(momentfound_list);
                        MomentListview.setAdapter(adapter);
                    }
                }
            }
        });
    }


    //if user does not log in, return to the login page
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(MyMomentActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }
}