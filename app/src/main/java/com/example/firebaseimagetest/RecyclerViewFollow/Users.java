package com.example.firebaseimagetest.RecyclerViewFollow;

public class Users {
    private String username;
    private String uid;

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

    public void setUsername(String username){
        this.username = username;
    }
}
