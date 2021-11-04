package com.example.comp90018_project.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.LoadImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileAdapter extends BaseAdapter {

    List<Map<String, Object>> profileList;
    LayoutInflater inflater;
    private static final String TAG = "Adapter";
    private FirebaseFirestore mDB;

    public ProfileAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        mDB = FirebaseFirestore.getInstance();
    }

    public void setProfileList(List<Map<String, Object>> profileList) {
        this.profileList = profileList;
    }

    @Override
    public int getCount() {
        return profileList.size();
    }

    @Override
    public Object getItem(int position) {
        return profileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.main_profile, null);

        LoadImageView avatar = view.findViewById(R.id.mainProfileAvatar);
        TextView username = (TextView) view.findViewById(R.id.mainProfileUsername);
        TextView email = (TextView) view.findViewById(R.id.mainProfileEmail);
        TextView uid = (TextView) view.findViewById(R.id.mainProfileUid);
        TextView bio = (TextView) view.findViewById(R.id.mainProfileBio);
        Button editProfile = (Button) view.findViewById(R.id.editProfile);
        Button logout = (Button) view.findViewById(R.id.logOutButton);

        Map map = this.profileList.get(position);
        if (map.get("avatar") != null) {
            avatar.loadImageFromURL((String) map.get("avatar"));
        } else {
            avatar.setImageResource(R.drawable.default_user_avatar);
        }
        username.setText((String) map.get("username"));
        email.setText((String) map.get("email"));
        uid.setText((String) map.get("uid"));
        bio.setText((String) map.get("bio"));
        return view;
    }
}
