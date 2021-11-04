package com.example.comp90018_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.R;
import com.example.comp90018_project.adapter.MomentAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCollectionActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private static final String TAG = "My collection";
    private FirebaseUser currentUser;
    private String USERID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth =  FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_my_collection);
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }else {
            // TODO: 2021/10/23 Add construction of layout
            USERID = currentUser.getUid();
            myCollectedView();
        }
    }

    private void myCollectedView() {
        DocumentReference docRef = mDB.collection("collections").document(USERID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d(TAG, "===============================TRY TO GET LIKE MOMENTS LIST ========================================");

                    Log.d(TAG, "===============================" +  task.getResult().getData().size() + "========================================");
                    ArrayList<Map<String, Object>> moments_list = (ArrayList<Map<String, Object>>) task.getResult().getData().get("my_collected_moments");
                    if (moments_list != null) {
                        Log.d(TAG, "collected moments list get");
                        ListView MomentListview = (ListView) findViewById(R.id.collect_momentsList);
                        List<Map<String, Object>> momentfound_list = new ArrayList<Map<String, Object>>();
                        for (int i=0; i < moments_list.size(); i++) {
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
                            map.put("image", moment_map.get("image_download_url"));
                            map.put("timestamp", moment_map.get("date"));
                            Log.d(TAG, "the userID is ------------ " + moment_map.get("uid") + " ------------------");

                            Log.d(TAG, "the time stamp is ------------ " + moment_map.get("date") + " ------------------");
                            momentfound_list.add(map);
                        }
                        MomentAdapter adapter = new MomentAdapter(MyCollectionActivity.this);
                        adapter.setMomentList(momentfound_list);
                        MomentListview.setAdapter(adapter);
                    }
                }
            }
        });
    }

    //if user does not log in, return to the Login
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(MyCollectionActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

}
