package com.example.comp90018_project.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.LoadImageView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class MomentAdapter extends BaseAdapter {
    List<Map<String, Object>> momentList;
    LayoutInflater inflater;
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
        LoadImageView avatar = view.findViewById(R.id.img_avatar);
        TextView name = (TextView) view.findViewById(R.id.tv_name);
        LoadImageView image =  view.findViewById(R.id.img_image);
        TextView content = (TextView) view.findViewById(R.id.tv_content);
        TextView timestamp = (TextView) view.findViewById(R.id.tv_time);
        ImageView collection =  view.findViewById(R.id.img_collection);
        ImageView like = view.findViewById(R.id.img_like);
        ImageView comment = view.findViewById(R.id.img_comment);

        Map map = this.momentList.get(position);
        if (String.valueOf(map.get("avatar").getClass()).contains("String")) {
            avatar.loadImageFromURL((String) map.get("avatar"));
        } else {
            avatar.setImageResource((Integer) map.get("avatar"));
        }
        timestamp.setText((String) map.get("timestamp"));
        name.setText((String) map.get("name"));
        content.setText((String) map.get("content"));
        if (map.get("image") == null) {
            image.setVisibility(View.GONE);
        }
        else {
            image.loadImageFromURL((String) map.get("image"));
        }
        // set onclick listener for collections, likes, commments
        return view;
    }


}
