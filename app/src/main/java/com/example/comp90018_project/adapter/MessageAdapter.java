package com.example.comp90018_project.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.comp90018_project.Activity.ProfileActivity;
import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.LoadImageView;
import com.example.comp90018_project.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends BaseAdapter {

    List<Map<String, Object>> messageList;
    LayoutInflater inflater;
    private static final String TAG = "Adapter";
    private FirebaseFirestore mDB;

    public MessageAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        mDB = FirebaseFirestore.getInstance();
    }

    public void setFriendList(List<Map<String, Object>> friendList) {
        this.messageList = friendList;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.message_list, null);

        LoadImageView avatar = view.findViewById(R.id.pendingFriendAvatar);
        TextView name = (TextView)view.findViewById(R.id.pendingFriendUsername);
        Button agree_btn = (Button) view.findViewById(R.id.agreeButton);
        Button disagree_btn = (Button) view.findViewById(R.id.disagreeButton);

        Map map = this.messageList.get(position);
        if (String.valueOf(map.get("avatar").getClass()).contains("String")) {
            avatar.loadImageFromURL((String) map.get("avatar"));
        }
        else {
            avatar.setImageResource((Integer) map.get("avatar"));
        }
        name.setText((String)map.get("name"));

        agree_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadToFireStore((String) map.get("currentUserId"), (HashMap<String, Object>) map.get("pendingFriend"), true, false);
                        uploadToFireStore((String) ((HashMap<String, Object>) map.get("pendingFriend")).get("uid"), (HashMap<String, Object>) map.get("currentUser"), true, true);
                        view.setVisibility(View.GONE);
                    }
                }
        );

        disagree_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadToFireStore((String) map.get("currentUserId"), (HashMap<String, Object>) map.get("pendingFriend"), false, false);
                        view.setVisibility(View.GONE);
                    }
                }
        );
        return view;
    }

    private void uploadToFireStore(String userId, HashMap<String, Object> pendingFriend, boolean agree, boolean another) {
        DocumentReference userRef = mDB.collection("users").document(userId);
        pendingFriend.remove("pendingFriends");
        pendingFriend.remove("friends");
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<HashMap<String, Object>> pendingFriends = (ArrayList<HashMap<String, Object>>) transaction.get(userRef).get("pendingFriends");
                ArrayList<HashMap<String, Object>> addedFriends = (ArrayList<HashMap<String, Object>>) transaction.get(userRef).get("friends");
                assert pendingFriends != null;
                if (!another) {
                    if (agree) {
                        if (!addedFriends.contains(pendingFriend)) {
                            pendingFriends.remove(pendingFriend);
                            addedFriends.add(pendingFriend);
                            transaction.update(userRef, "pendingFriends", pendingFriends);
                            transaction.update(userRef, "friends", addedFriends);
                        }
                        else {
                            Log.i(TAG, "Already friends!!");
                        }
                    }
                    else {
                        pendingFriends.remove(pendingFriend);
                        transaction.update(userRef, "pendingFriends", pendingFriends);
                    }
                }
                else {
                    addedFriends.add(pendingFriend);
                    transaction.update(userRef, "friends", addedFriends);
                }

                // success
                return null;
            }
        });
    }
}
