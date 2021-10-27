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

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.LoadImageView;

import java.util.List;
import java.util.Map;

public class FriendAdapter extends BaseAdapter {

    List<Map<String, Object>> friendList;
    LayoutInflater inflater;
    private static final String TAG = "Adapter";

    public FriendAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setFriendList(List<Map<String, Object>> friendList) {
        this.friendList = friendList;
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.friend_list, null);

        LoadImageView avatar = view.findViewById(R.id.friendAvatar);
        TextView name = (TextView)view.findViewById(R.id.friendName);

        Map map = this.friendList.get(position);
        if (String.valueOf(map.get("avatar").getClass()).contains("String")) {
            avatar.loadImageFromURL((String) map.get("avatar"));
        }
        else {
            avatar.setImageResource((Integer) map.get("avatar"));
        }
        name.setText((String)map.get("name"));
        return view;
    }
}
