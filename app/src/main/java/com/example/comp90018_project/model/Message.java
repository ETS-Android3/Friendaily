package com.example.comp90018_project.model;

import java.util.HashMap;
import java.util.Map;

public class Message implements Comparable<Message>{
    private String sender;
    private String msgid;
    private String receiver;
    private String content;
    private long date;
    private Boolean Read;

    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.msgid = null;
        date = System.currentTimeMillis();
    }

    public Message(Map<String,Object> msg){
        this.sender = (String) msg.get("sender");
        this.receiver = (String) msg.get("receiver");
        this.content = (String) msg.get("content");
        this.date = (long) msg.get("date");
    }

    public Map<String,Object> toMap(){
        Map<String,Object> message = new HashMap<>();
        message.put("sender", this.sender);
        message.put("receiver",this.receiver);
        message.put("content",content);
        message.put("date",date);
        return message;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public long getDate() {
        return date;
    }

    @Override
    /**
     * If the id of two message is the same, then they are the same message
     * Else we will compare than according to date
     */
    public int compareTo(Message o) {
        if(this.msgid.equals(o.msgid)){
            return 0;
        }else{
            if((this.date - o.getDate()) > 0) {
                return 1;
            }else return -1;
        }

    }

    @Override
    public boolean equals (Object  obj){
        if(obj instanceof Message){
            if(this.msgid.equals(((Message) obj).msgid)) return true;
            else return false;
        }else return false;
    }

}
