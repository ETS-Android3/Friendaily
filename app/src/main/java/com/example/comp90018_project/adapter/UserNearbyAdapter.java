package com.example.comp90018_project.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.BitmapTransfer;
import com.example.comp90018_project.Util.LoadImageView;
import com.example.comp90018_project.model.UserNearby;
import com.google.protobuf.StringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserNearbyAdapter extends BaseAdapter {
    List<Map<String, Object>> nearbyUsers;
    LayoutInflater inflater;
    private static final String TAG = "Adapter";

    public UserNearbyAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setUsersList(ArrayList<UserNearby> nearbyList) {
        this.nearbyUsers = new ArrayList<>();
        for (UserNearby user:nearbyList){
            nearbyUsers.add(user.toMap());
        }
    }

    @Override
    public int getCount() {
        return nearbyUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return nearbyUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View view = inflater.inflate(R.layout.nearby_list, null);

        LoadImageView avatar = view.findViewById(R.id.NearbyUserAvatar);
        TextView name = (TextView)view.findViewById(R.id.NearbyUserName);
        TextView distance = (TextView)view.findViewById(R.id.Distance);

        Map map = this.nearbyUsers.get(position);
        Bitmap bitmap;
        Log.i(TAG, String.valueOf(map));
        if (String.valueOf(map.get("avatar").getClass()).contains("String")) {
            avatar.loadImageFromURL((String) map.get("avatar"));
        }
        else {
            avatar.setImageResource((Integer) map.get("avatar"));
        }
        name.setText((String)map.get("name"));
        int dis =((Double) map.get("distance")).intValue();
        distance.setText((String.valueOf(dis) + "m away"));
        return view;
    }
}
