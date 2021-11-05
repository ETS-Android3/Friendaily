package com.example.comp90018_project.model;

import java.util.HashMap;
import java.util.Map;
import com.example.comp90018_project.R;

/**
 * This instance is for represent a friend in friend list
 * It can be ordered by the time of chatting
 */
public class Friend extends User implements Comparable<Friend>{
    private Object unread_count;
    private long chat_date;

    public Friend(String uid, String username, String avatarUrl, Double unread_count, long chat_date){
        super(uid, null, username, null,null,avatarUrl);
        this.unread_count = unread_count;
        this.chat_date = chat_date;
    }

    public Friend(String uid, String username, String avatarUrl){
        super(uid, null, username, null,null,avatarUrl);
        this.unread_count = 0.0;
        this.chat_date = 0;
    }

    public Friend(String uid, Map<String,Object> info){
        super();
        this.setUid(uid);
        if(info.get("unread_count")!=null){
            this.unread_count = info.get("unread_count");
        }else this.unread_count = 0.0;

        if(info.get("last_chat_date")!=null){
            this.chat_date = (long) info.get("last_chat_date");
        }else this.chat_date = 0;

    }

    public Map<String,Object> toMap(){
        HashMap<String,Object> map = new HashMap<>();
        map.put("uid",this.getUid());
        if(this.getAvatarUrl() == null){
            map.put("avatar", R.drawable.default_user_avatar);
        } else map.put("avatar", this.getAvatarUrl());
        map.put("name",this.getUsername());
        map.put("unread_count",unread_count);
        map.put("last_chat",chat_date);
        return map;
    }

    public void setInfo(Friend f){
        this.setAvatarUrl(f.getAvatarUrl());
        this.setUsername(f.getUsername());
    }

    public Object getUnread_count(){
        return unread_count;
    }

    public long getChat_date(){
        return  chat_date;
    }

    /**
     * If this friend's message haven't been read, put it in the front
     * A friend who chat with us recently will be put in the front
     * @param o
     * @return
     */
    @Override
    public int compareTo(Friend o) {
        if((int)this.unread_count > 0 && (int)o.unread_count > 0){
            if(this.chat_date >= o.chat_date) return 1;
            else return -1;
        }
        if((int)this.unread_count > 0) return 1;
        else return -1;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Friend){
            if(this.getUid().equals(((Friend) o).getUid())) {
                return true;
            }else return false;
        }
        return false;
    }
}
