package com.example.comp90018_project.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.comp90018_project.adapter.FriendAdapter;
import com.example.comp90018_project.R;
import com.example.comp90018_project.adapter.MessageAdapter;
import com.example.comp90018_project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private EditText content;
    // if there is no user to be found, userfound will be null
    private User user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private String userId;
    private static final String TAG = "FindNewFriend";
    public static final String EXTRA_MESSAGE = "com.example.comp90018_project.FIND_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_message);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }else {
            // TODO: 2021/10/23 Add construction of layout
            userId = currentUser.getUid();
            finduser();
        }
    }

    private void finduser(){
        CollectionReference userRef = mDB.collection("users");
        Query query = userRef.whereEqualTo("uid", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                User user = new User(task.getResult().getDocuments().get(0).getData());
                ArrayList<Map<String,Object>> pendingFriends = (ArrayList<Map<String,Object>>) user.getpendingFriends();
                if (pendingFriends != null) {
                    Log.d(TAG, "pendingFriends" + pendingFriends);
                    ListView PendingListview = (ListView) findViewById(R.id.pendingFrientsList);
                    List<Map<String, Object>> PendingList = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < pendingFriends.size(); i++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        String avatar_url = (String) pendingFriends.get(i).get("avatar_url");
                        if (avatar_url == null) {
                            map.put("avatar", R.drawable.a);
                        }
                        else {
                            map.put("avatar", pendingFriends.get(i).get("avatar_url"));
                        }
                        map.put("name", pendingFriends.get(i).get("username"));
                        map.put("currentUserId", userId);
                        map.put("currentUser", user.toMap());
                        map.put("pendingFriend", pendingFriends.get(i));
                        PendingList.add(map);
                    }
                    MessageAdapter adapter = new MessageAdapter(MessageActivity.this);
                    adapter.setFriendList(PendingList);
                    PendingListview.setAdapter(adapter);
                }
            }
        });
    }

    private void uploadToFireStore(User friends, boolean agree) {
        DocumentReference userRef = mDB.collection("users").document(userId);

        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<User> pendingFriends = (ArrayList<User>) transaction.get(userRef).get("pendingFriends");
                ArrayList<User> addedFriends = (ArrayList<User>) transaction.get(userRef).get("addedFriends");
                pendingFriends.remove(friends);
                if (agree) {
                    addedFriends.add(friends);
                }
                transaction.update(userRef, "pendingFriends", pendingFriends);
                transaction.update(userRef, "addedFriends", addedFriends);
                // success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Transaction failure.", e);
            }
        });
    }

    //if user does not log in, return to the Login
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(MessageActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }
}