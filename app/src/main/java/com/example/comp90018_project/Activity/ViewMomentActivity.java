package com.example.comp90018_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.example.comp90018_project.R;

public class ViewMomentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseUser currentUser;
    String TAG = "View Moment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.moment_view_item);
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            reload();
        } else {
            momentView();
        }
    }

    private void momentView() {
        String UserID = currentUser.getUid();
        Log.i(TAG, UserID);
        // Get moment query and set adapter

    }

    private void reload() {
        Intent intent = new Intent();
        intent.setClass(ViewMomentActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

}
