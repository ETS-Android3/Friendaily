package com.example.comp90018_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.adapter.FriendAdapter;
import com.example.comp90018_project.adapter.MomentAdapter;
import com.example.comp90018_project.model.Moment;
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
import com.google.firebase.storage.FirebaseStorage;
import com.example.comp90018_project.R;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.comp90018_project.Activity.LoginActivity.USERID;

public class ViewMomentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseUser currentUser;
    String TAG = "View Moment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!! starts!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_view_moments);
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            reload();
        } else {
            momentView();
        }
    }

    private void momentView() {
        String UserID = currentUser.getUid();
        Log.d(TAG, UserID);
        // Get moment query and set adapter
        //CollectionReference momentRef = mDB.collection("moments");
        DocumentReference docRef = mDB.collection("moments").document(USERID);
        //Query query = momentRef.whereEqualTo("uid", USERID);
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
                        ListView MomentListview = (ListView) findViewById(R.id.momentsList);
                        List<Map<String, Object>> momentfound_list = new ArrayList<Map<String, Object>>();
                        for (int i=moments_list.size() - 1; i >= 0; i--) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            Map<String, Object> moment_map = moments_list.get(i);
                            String avatar_url = (String) moment_map.get("user_avatar_url");
                            if (avatar_url == null) {
                                map.put("moment_image", null);
                            } else {
                                map.put("moment_image", moment_map.get("user_avatar_url"));
                            }
                            map.put("name", moment_map.get("username"));
                            map.put("content", moment_map.get("content"));
                            map.put("image", moment_map.get("image_url"));
                            map.put("timestamp", moment_map.get("date"));
                            momentfound_list.add(map);
                        }
                        MomentAdapter adapter = new MomentAdapter(ViewMomentActivity.this);
                        adapter.setMomentList(momentfound_list);
                        MomentListview.setAdapter(adapter);
                    }
                }
            }
        });
    }

    private void reload() {
        Intent intent = new Intent();
        intent.setClass(ViewMomentActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

}
