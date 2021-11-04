package com.example.comp90018_project.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;


import androidx.annotation.NonNull;

import com.example.comp90018_project.Activity.MainActivity;
import com.example.comp90018_project.Activity.PostMomentActivity;
import com.example.comp90018_project.Activity.ViewCommentActivity;

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.LoadImageView;
import com.example.comp90018_project.model.Moment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MomentAdapter extends BaseAdapter {
    List<Map<String, Object>> momentList;
    LayoutInflater inflater;
    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private static final String TAG = "moment adapter";

    public MomentAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setMomentList(List<Map<String, Object>> momentList) {
        this.momentList = momentList;
    }

    @Override
    public int getCount() {
        return momentList.size();
    }

    @Override
    public Object getItem(int position) {
        return momentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.moment_view_item, null);
        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        LoadImageView avatar = view.findViewById(R.id.img_avatar);
        TextView name = (TextView) view.findViewById(R.id.tv_name);
        LoadImageView image =  view.findViewById(R.id.img_image);
        TextView content = (TextView) view.findViewById(R.id.tv_content);
        TextView timestamp = (TextView) view.findViewById(R.id.tv_time);
        ImageView collection =  view.findViewById(R.id.img_collection);
        ImageView like = view.findViewById(R.id.img_like);
        ImageView comment = view.findViewById(R.id.img_comment);

        Map map = this.momentList.get(position);
        String avatar_url = (String) map.get("avatar");
        String ts = (String) map.get("timestamp");
        Log.d(TAG, "what read from timestamp is !!!!!!!!!!!! " + ts + " !!!!!!!!!!!!!!!!!!");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(ts);
            long millis = date.getTime() + 39600000;
            String username = (String) map.get("name");
            String mom_content = (String) map.get("content");
            String mom_img_url = (String) map.get("image");
            String uid = (String) map.get("uid");

            if (avatar_url == null) {
                avatar.setImageResource(R.drawable.default_user_avatar);
            } else {
                avatar.loadImageFromURL(avatar_url);
            }
            Log.d(TAG, "The userID is ++++++++++++++++ " + uid + " ++++++++++++++++++++");
            timestamp.setText(ts);
            name.setText(username);
            content.setText(mom_content);

            if (mom_img_url == null) {
                image.setVisibility(View.GONE);
            }
            else {
                image.loadImageFromURL(mom_img_url);
            }


            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    like.setBackgroundResource(R.drawable.dianzan_after);
                    Log.d(TAG, "like is clicked, with " + username + "'s post");
                    Moment newMom = new Moment(uid, millis, mom_content, mom_img_url, username, avatar_url);
                    postMomentToLike(currentUser.getUid(), newMom.toMap(), username);
                }
            });

            collection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    collection.setBackgroundResource(R.drawable.shoucang_after);
                    Log.d(TAG, "collection is clicked, with " + username + "'s post");
                    Moment newMom = new Moment(uid, millis, mom_content, mom_img_url, username, avatar_url);
                    postMomentToCollection(currentUser.getUid(), newMom.toMap(), username);
                }
            });

            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d(TAG, "collection is clicked, with " + username + "'s post");
                    Intent intent = new Intent(view.getContext(), ViewCommentActivity.class);
                    intent.putExtra("date", ts);
                    intent.putExtra("uid", uid);
                    intent.putExtra("username", username);
                    view.getContext().startActivity(intent);
                }
            });

        } catch (ParseException e) {
            System.out.println(e);
        }

        return view;
    }

    private void postMomentToCollection(String userID, Map<String, Object> newMoment, String username) {
//        Log.d(TAG, "The userID is ++++++++++++++++ " + userID + " ++++++++++++++++++++");
        DocumentReference ref = mDB.collection("collections").document(userID);
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<Map<String, Object>> existing_moments = (ArrayList<Map<String, Object>>) transaction.get(ref).get("my_collected_moments");
                existing_moments.add(newMoment);
                transaction.update(ref, "my_collected_moments", existing_moments);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, username + " Post successful!");
                //Toast.makeText(ViewMomentActivity.class, "Post failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, username + " Post failed!");
                // Toast.makeText(PostMomentActivity.this, "Post failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void postMomentToLike(String userID, Map<String, Object> newMoment, String username) {
        //Log.d(TAG, "The userID is ++++++++++++++++ " + userID + " ++++++++++++++++++++");
        DocumentReference ref = mDB.collection("likes").document(userID);
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<Map<String, Object>> existing_moments = (ArrayList<Map<String, Object>>) transaction.get(ref).get("my_like_moments");
                existing_moments.add(newMoment);
                transaction.update(ref, "my_like_moments", existing_moments);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, username + " Post successful!");
                //Toast.makeText(ViewMomentActivity.class, "Post failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, username + " Post failed!");
                // Toast.makeText(PostMomentActivity.this, "Post failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

}
