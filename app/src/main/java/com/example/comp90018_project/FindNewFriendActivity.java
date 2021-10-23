package com.example.comp90018_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class FindNewFriendActivity extends AppCompatActivity {
    private EditText content;
    // if there is no user to be found, userfound will be null
    private User userfound;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private static final String TAG = "FindNewFriend";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_find_new_friend);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }else {
            // TODO: 2021/10/23 Add construction of layout
        }
    }

    public void finduser(){
        if (content != null){
            String username = content.getText().toString();
            CollectionReference userRef = mDB.collection("users");
            Query query = userRef.whereEqualTo("username",username);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().getDocuments().get(0).getData() == null) {
                            //If this username isn't stored in database
                            Log.i(TAG, "We cannot find this user");
                            Toast.makeText(FindNewFriendActivity.this, "We cannot find this user", Toast.LENGTH_LONG).show();
                        } else {
                            //Get the corresponding email address
                            userfound = new User(task.getResult().getDocuments().get(0).getData());
                            Log.i(TAG, "Find user" );
                        }
                    }
                }
            });
        }else Toast.makeText(FindNewFriendActivity.this, "You don't search for a user", Toast.LENGTH_LONG).show();

    }
    //if user does not log in, return to the Login
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(FindNewFriendActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }
}