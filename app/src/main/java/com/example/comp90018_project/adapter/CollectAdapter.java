package com.example.comp90018_project.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectAdapter extends BaseAdapter {
    List<Map<String, Object>> collected_momentList;
    LayoutInflater inflater;
    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private static final String TAG = "collect adapter";

    public CollectAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setCollectedMomentList(List<Map<String, Object>> liked_momentList) {
        this.collected_momentList = liked_momentList;
    }

    @Override
    public int getCount() {
        return collected_momentList.size();
    }

    @Override
    public Object getItem(int position) {
        return collected_momentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.collect_item_view, null);
        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        LoadImageView avatar = view.findViewById(R.id.collect_img_avatar);
        TextView name = (TextView) view.findViewById(R.id.collect_tv_name);
        LoadImageView image =  view.findViewById(R.id.collect_img_image);
        TextView content = (TextView) view.findViewById(R.id.collect_tv_content);
        TextView timestamp = (TextView) view.findViewById(R.id.collect_tv_time);
        ImageView collection =  view.findViewById(R.id.collect_img_collection);
        ImageView like = view.findViewById(R.id.collect_img_like);
        ImageView comment = view.findViewById(R.id.collect_img_comment);

        Map map = this.collected_momentList.get(position);
        String avatar_url = (String) map.get("avatar");
        String ts = (String) map.get("timestamp");

        String username = (String) map.get("name");
        String mom_content = (String) map.get("content");
        String mom_img_url = (String) map.get("image");
        String uid = (String) map.get("uid");

        if (avatar_url == null) {
            avatar.setImageResource(R.drawable.default_user_avatar);
        } else {
            avatar.loadImageFromURL(avatar_url);
        }

        timestamp.setText(ts);
        name.setText(username);
        content.setText(mom_content);

        if (mom_img_url == null) {
            image.setVisibility(View.GONE);
        }
        else {
            image.loadImageFromURL(mom_img_url);
        }

        collection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collection.setBackgroundResource(R.drawable.shoucang_before);
                Log.d(TAG, "like is unclicked, with " + username + "'s post");
                Moment newMom = new Moment(uid, ts, mom_content, mom_img_url, username, avatar_url);
                deleteMomentInCollect(currentUser.getUid(), newMom.toMap(), username);
            }
        });

        return view;
    }

    private void deleteMomentInCollect(String userID, Map<String, Object> newMoment, String username) {
        DocumentReference ref = mDB.collection("collections").document(userID);
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<Map<String, Object>> existing_moments = (ArrayList<Map<String, Object>>) transaction.get(ref).get("my_collected_moments");
                Map<String, Object> item_to_delete = null;
                for (Map<String, Object> map : existing_moments) {
                    if (((String) map.get("date")).equals( (String)newMoment.get("date"))) {
                        item_to_delete = map;
                    }
                }
                if (item_to_delete != null) {
                    existing_moments.remove(item_to_delete);
                    transaction.update(ref, "my_collected_moments", existing_moments);

                }
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, username + " delete successful!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, username + " delete failed!");
            }
        });
    }
}
