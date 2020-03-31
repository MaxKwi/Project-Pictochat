package com.example.firebaseimagetest;

import java.io.Serializable;

public class Users implements Serializable {
    private String username;
    private String uid;
    private String notificationKey;

    public Users(String uid)
    {
        this.uid = uid;
    }

    public Users(String username, String uid){
        this.username = username;
        this.uid = uid;
    }

    public String getUid(){
        return uid;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getUsername(){
        return username;
    }

    public String getNotificationKey() { return notificationKey; }

    public void setUsername(String username){
        this.username = username;
    }

    public void setNotificationKey(String notificationKey)
    {
        this.notificationKey = notificationKey;
    }
}
