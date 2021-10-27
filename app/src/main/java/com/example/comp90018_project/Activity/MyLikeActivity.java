package com.example.comp90018_project.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.comp90018_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MyLikeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private static final String TAG = "MyLike";
    private List<DocumentSnapshot> likeList;
    private String USERID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_like);
        mAuth =  FirebaseAuth.getInstance();
        setContentView(R.layout.activity_find_new_friend);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }else {
            // TODO: 2021/10/23 Add construction of layout
            USERID = currentUser.getUid();
        }
    }

    /**
     * Get the like list for this user from database
     */
    public void getLikeList(){
        CollectionReference likesRef = mDB.collection("likes");
        Query query = likesRef.whereEqualTo("uid",USERID).orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() != 0) {
                        //This user like something
                        likeList = task.getResult().getDocuments();
                        Log.i(TAG, "Find like list" );

                    } else {

                    }
                }
            }
        });

    }

    //if user does not log in, return to the Login
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(MyLikeActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }
}