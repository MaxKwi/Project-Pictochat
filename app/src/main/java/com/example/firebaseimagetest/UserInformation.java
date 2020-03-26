package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UserInformation {

    public static ArrayList<String> friendsList = new ArrayList<>();
    public static ArrayList<String> pendingList = new ArrayList<>();
    public static ArrayList<String> incomingList = new ArrayList<>();

    public void startFetching(){
        friendsList.clear();
        pendingList.clear();
        incomingList.clear();

        getFriend();
        getPending();
        getIncoming();
    }

    private boolean ArrayContain(String uid)
    {
        boolean contains = false;

        if(!friendsList.contains(uid) && !pendingList.contains(uid) && !incomingList.contains(uid))
        {
            contains = false;
        }
        else if(friendsList.contains(uid) || pendingList.contains(uid) || incomingList.contains(uid))
        {
            contains = true;
        }

        return contains;
    }

    private void getPending()
    {
        DatabaseReference pendingDB = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("pending");
        pendingDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    String uid = dataSnapshot.getRef().getKey();
                    if(uid != null && !ArrayContain(uid))
                    {
                        pendingList.add(uid);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String uid = dataSnapshot.getRef().getKey();
                    if(uid != null){
                        pendingList.remove(uid);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getIncoming()
    {
        final DatabaseReference incomingDB = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("incoming");
        incomingDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    String uid = dataSnapshot.getRef().getKey();
                    if(uid != null && !ArrayContain(uid))
                    {
                        incomingList.add(uid);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String uid = dataSnapshot.getRef().getKey();
                    if(uid != null){
                        incomingList.remove(uid);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFriend() {
        DatabaseReference friendDB = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("friends");
        friendDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    String uid = dataSnapshot.getRef().getKey();
//                    if(uid != null && !friendsList.contains(uid)){
//                        friendsList.add(uid);
//                    }

                    if(uid != null && !ArrayContain(uid)){
                        friendsList.add(uid);
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String uid = dataSnapshot.getRef().getKey();
                    if(uid != null){
                        friendsList.remove(uid);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
