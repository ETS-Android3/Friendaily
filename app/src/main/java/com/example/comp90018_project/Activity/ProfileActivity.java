package com.example.comp90018_project.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.R;
import com.example.comp90018_project.adapter.FriendAdapter;
import com.example.comp90018_project.adapter.MomentAdapter;
import com.example.comp90018_project.adapter.ProfileAdapter;
import com.example.comp90018_project.model.User;
import com.example.comp90018_project.Util.LoadImageView;
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

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private LoadImageView avatar;
    private ImageView backMain;
    private TextView username;
    private TextView email;
    private TextView uid;
    private Button button;
    private Button deleteButton;
    private User searchedUser;
    private User user;
    private String userId;

    private String message;
    private String messageType = "Chat";
    private static final String TAG = "profile";
    public static final String EXTRA_MESSAGE = "com.example.comp90018_project.PROFILE_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        message = intent.getStringExtra(ShowFriendActivity.EXTRA_MESSAGE);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        userId = currentUser.getUid();
        if (message == null) {
            message = intent.getStringExtra(FindNewFriendActivity.EXTRA_MESSAGE);
            messageType = "Add";
        }
        if (message == null) {
            message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
            Log.i(TAG, message);
            messageType = "Chat";
        }
        findUser();
        setContentView(R.layout.profile);
        setProfileView();
        backMain = findViewById(R.id.profileBackMain);
        backMain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
    }

    private void setProfileView() {
        avatar = (LoadImageView)findViewById(R.id.profileAvatar);
        username = (TextView)findViewById(R.id.profileUsername);
        email = (TextView)findViewById(R.id.profileEmail);
        uid = (TextView)findViewById(R.id.profileUid);
        button = (Button)findViewById(R.id.profileButton);
        deleteButton = (Button)findViewById(R.id.deleteFriendButton);

        uid.setText(message);
        button.setText(messageType);

        if (messageType.equals("Add")) {
            deleteButton.setVisibility(View.GONE);
        }

        CollectionReference userRef = mDB.collection("users");
        Query query = userRef.whereEqualTo("uid", message);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                searchedUser = new User(task.getResult().getDocuments().get(0).getData());
                username.setText(searchedUser.getUsername());
                email.setText(searchedUser.getEmail());
                String avatar_url = searchedUser.getAvatarUrl();
                if (avatar_url == null) {
                    avatar.setImageResource(R.drawable.default_user_avatar);
                } else {
                    avatar.loadImageFromURL(avatar_url);
                }
                updateFriend();
            }
        });

        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (messageType.equals("Add")) {
                            uploadToFireStore(user);
                        }
                        else {
                            Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                            String selectUser = message;
                            Log.d(TAG, "selected:" + selectUser);
                            intent.putExtra(EXTRA_MESSAGE, selectUser);
                            startActivity(intent);
                        }
                    }
                }
        );

        deleteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFriend();
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
        );
    }

    private void deleteFriend() {
        DocumentReference userRef = mDB.collection("users").document(userId);
        findsearchedUser();
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<HashMap<String, Object>> addedFriends = (ArrayList<HashMap<String, Object>>) transaction.get(userRef).get("friends");
                assert addedFriends != null;
                HashMap<String, Object> searchedUserMap = (HashMap<String, Object>) searchedUser.toMap();
                searchedUserMap.remove("pendingFriends");
                searchedUserMap.remove("friends");
                addedFriends.remove(searchedUserMap);
                transaction.update(userRef, "friends", addedFriends);
                Log.i(TAG, "Delete friend " + searchedUser.getUsername() + " !!");
                return null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Transaction failure.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Transaction failure.", e);
            }
        });

        DocumentReference otherUserRef = mDB.collection("users").document(message);
        findsearchedUser();
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<HashMap<String, Object>> addedFriends = (ArrayList<HashMap<String, Object>>) transaction.get(otherUserRef).get("friends");
                assert addedFriends != null;
                HashMap<String, Object> userMap = (HashMap<String, Object>) user.toMap();
                userMap.remove("pendingFriends");
                userMap.remove("friends");
                addedFriends.remove(userMap);
                transaction.update(otherUserRef, "friends", addedFriends);
                Log.i(TAG, "Delete friend " + user.getUsername() + " !!");
                return null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Transaction failure.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Delete friend failure.", e);
            }
        });
    }

    private void uploadToFireStore(User friends) {
        DocumentReference userRef = mDB.collection("users").document(message);
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<HashMap<String, Object>> pendingFriends = (ArrayList<HashMap<String, Object>>) transaction.get(userRef).get("pendingFriends");
                ArrayList<HashMap<String, Object>> addedFriends = (ArrayList<HashMap<String, Object>>) transaction.get(userRef).get("friends");
                assert pendingFriends != null;
                assert addedFriends != null;
//                Looper.prepare();
                if (checkUser(addedFriends)) {
                    Log.i(TAG, "Already friends!!");
                    //                    Toast.makeText(ProfileActivity.this, "Already friends!!", Toast.LENGTH_LONG).show();
                }
                else if (friends.getUid().equals(message)) {
                    Log.i(TAG, "Cannot friend yourself!");
                }
                else if (!checkUser(pendingFriends)) {
                    HashMap<String, Object> friendMap = (HashMap<String, Object>) friends.toMap();
                    friendMap.remove("pendingFriends");
                    friendMap.remove("friends");
                    Log.i(TAG, "hash: " + String.valueOf(friendMap));
                    pendingFriends.add(friendMap);
                    transaction.update(userRef, "pendingFriends", pendingFriends);
                    Log.i(TAG, "Friend request succeeded!");
//                    Toast.makeText(ProfileActivity.this, "Friend request succeeded!", Toast.LENGTH_LONG).show();
                }
                else {
//                    Toast.makeText(ProfileActivity.this, "Already sent friend request!!", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Already sent friend request!!");
                }
                // success
//                Looper.loop();
                return null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Transaction failure.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Send friend request failure.", e);
            }
        });
    }

    private void updateFriend() {
        DocumentReference userRef = mDB.collection("users").document(userId);
        findsearchedUser();
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<HashMap<String, Object>> addedFriends = (ArrayList<HashMap<String, Object>>) transaction.get(userRef).get("friends");
                assert addedFriends != null;
                HashMap<String, Object> searchedUserMap = (HashMap<String, Object>) searchedUser.toMap();
                searchedUserMap.remove("pendingFriends");
                searchedUserMap.remove("friends");
                for (int i = 0; i < addedFriends.size(); i++) {
                    HashMap<String, Object> friend = addedFriends.get(i);
                    if (friend.get("uid").equals(searchedUserMap.get("uid"))) {
                        addedFriends.remove(friend);
                        addedFriends.add(searchedUserMap);
                        break;
                    }
                }
                transaction.update(userRef, "friends", addedFriends);
                Log.i(TAG, "Update friend " + searchedUser.getUsername() + " !");
                return null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Transaction failure.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Update friend failure.", e);
            }
        });
    }

    private void findUser() {
        CollectionReference userRef = mDB.collection("users");
        Query query = userRef.whereEqualTo("uid", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                user = new User(task.getResult().getDocuments().get(0).getData());
            }
        });
    }

    private void findsearchedUser() {
        CollectionReference userRef = mDB.collection("users");
        Query query = userRef.whereEqualTo("uid", message);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                searchedUser = new User(task.getResult().getDocuments().get(0).getData());
            }
        });
    }

    private boolean checkUser(ArrayList<HashMap<String, Object>> pendingFriendsList) {
        for (int i = 0; i < pendingFriendsList.size(); i++) {
            if (pendingFriendsList.get(i) != null && pendingFriendsList.get(i).get("uid").equals(userId)) {
                return true;
            }
        }
        return false;
    }
}
