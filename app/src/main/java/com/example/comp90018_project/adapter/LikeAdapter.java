package com.example.comp90018_project.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.LoadImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class LikeAdapter extends BaseAdapter {
    List<Map<String, Object>> liked_momentList;
    LayoutInflater inflater;
    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private static final String TAG = "like adapter";

    public LikeAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setlikedMomentList(List<Map<String, Object>> liked_momentList) {
        this.liked_momentList = liked_momentList;
    }

    @Override
    public int getCount() {
        return liked_momentList.size();
    }

    @Override
    public Object getItem(int position) {
        return liked_momentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.like_view_item, null);
        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        LoadImageView avatar = view.findViewById(R.id.like_img_avatar);
        TextView name = (TextView) view.findViewById(R.id.like_tv_name);
        LoadImageView image =  view.findViewById(R.id.like_img_image);
        TextView content = (TextView) view.findViewById(R.id.like_tv_content);
        TextView timestamp = (TextView) view.findViewById(R.id.like_tv_time);
        ImageView collection =  view.findViewById(R.id.like_img_collection);
        ImageView like = view.findViewById(R.id.like_img_like);
        ImageView comment = view.findViewById(R.id.like_img_comment);

        return view;
    }

}
