package com.example.comp90018_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.LoadImageView;
import com.example.comp90018_project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class ChatActivity extends AppCompatActivity {

    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private TextView username;
    private Button sendButton;
    private User chatUser;
    private User user;
    private String userId;

    private String message;
    private static final String TAG = "Chat";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        userId = currentUser.getUid();
        if (message != null) {
            findChatUser();
        }
        setContentView(R.layout.activity_chat);
    }

    private void findChatUser() {
        CollectionReference userRef = mDB.collection("users");
        Query query = userRef.whereEqualTo("uid", message);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                chatUser = new User(task.getResult().getDocuments().get(0).getData());
                username = (TextView) findViewById(R.id.chatName);
                username.setText(chatUser.getUsername());
            }
        });
    }
}
