package com.example.comp90018_project.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.LoadImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends BaseAdapter {
    List<Map<String, Object>> commentList;
    LayoutInflater inflater;
    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private static final String TAG = "comment adapter";

    public CommentAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setCommentList(List<Map<String, Object>> commentList) {
        this.commentList = commentList;
    }

    @Override
    public int getCount() {
        return commentList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.comment_view_item, null);
        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        LoadImageView avatar = view.findViewById(R.id.comment_img_avatar);
        TextView name = (TextView) view.findViewById(R.id.comment_name);
        TextView content = (TextView) view.findViewById(R.id.comment_content);
        TextView timestamp = (TextView) view.findViewById(R.id.comment_time);

        Map map = this.commentList.get(position);
        String avatar_url = (String) map.get("avatar");
        String ts = (String) map.get("timestamp");
        Log.d(TAG, "what read from timestamp is !!!!!!!!!!!! " + ts + " !!!!!!!!!!!!!!!!!!");
        Log.d(TAG, "what avatar_url !!!!!!!!!!!! " + avatar_url + " !!!!!!!!!!!!!!!!!!");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(ts);
            long millis = date.getTime() + 39600000;
            String username = (String) map.get("name");
            String comment_content = (String) map.get("content");
            if (avatar_url.equals("")) {
                avatar.setImageResource(R.drawable.a);
            } else {
                avatar.loadImageFromURL(avatar_url);
            }
            timestamp.setText(ts);
            name.setText(username);
            content.setText(comment_content);
        } catch (ParseException e) {
            System.out.println(e);
        }
        return view;
    }
}
