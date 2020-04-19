package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.firebaseimagetest.RecyclerViewMain.ChatObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class UserInformation {

    public static ArrayList<String> friendsList = new ArrayList<>();
    public static ArrayList<String> pendingList = new ArrayList<>();
    public static ArrayList<String> incomingList = new ArrayList<>();
    public static ArrayList<ChatInfo> chatList = new ArrayList<>();

    public void startFetching(){
        friendsList.clear();
        pendingList.clear();
        incomingList.clear();
        chatList.clear();

        getFriend();
        getPending();
        getIncoming();
        getChats();
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

    private void getChats() //gets all chats under current user
    {
        chatList.clear();
        DatabaseReference mUserChatDb = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat");
        mUserChatDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        String chatId = ds.getRef().getKey();

                        if(chatId != null && !chatList.contains(chatId)){
                            chatList.add(new ChatInfo(chatId));
                        }
                    }


                    //System.out.println("CURRENT SIZE OF CHAT IDS: " + chatList.size());

                    getUsersInChat();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void getUsersInChat() //will get all of the users in the current chat
    {
        DatabaseReference mChatDb = FirebaseDatabase.getInstance().getReference().child("chat");
        mChatDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {

                    //String usersInCurrentChat = "";
                    for(ChatInfo childObject : chatList)
                    {
                        String usersInCurrentChat = "";
                        for(DataSnapshot cs : dataSnapshot.child(childObject.chatId).child("info").child("users").getChildren())
                        {
                            if(!cs.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                            {
                                usersInCurrentChat = cs.getKey();
                                childObject.tempUidFriendsInChat.add(usersInCurrentChat);
                            }
                        }

                        //System.out.println("USERS ASIDE FROM URSELF IN ONE GIVEN CHAT" + childObject.tempUidFriendsInChat.size());

                    }

                    //completed

                    GetFriends();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void GetFriends()
    {
        DatabaseReference friendsDb = FirebaseDatabase.getInstance().getReference().child("users");
        friendsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getValue() != null)
                {

                    for(ChatInfo childObject : chatList)
                    {
                        System.out.println("LENGTH OF TEMP FRIENDS: " + childObject.tempUidFriendsInChat.size());
                        for(String currentFriends : childObject.tempUidFriendsInChat)
                        {
                            System.out.println("CHECK PHASE");
                            String currentUsername = dataSnapshot.child(currentFriends).child("username").getValue().toString();
                            childObject.currentFriendsInChat.add(currentUsername);
                            System.out.println(currentUsername);

                        }
                        if(childObject.currentFriendsInChat.size() == 1)
                        {
                            childObject.displayName = childObject.currentFriendsInChat.get(0);
                        }
                        else
                        {
                            String finalDisplay = "";
                            for(int i = 0; i < childObject.currentFriendsInChat.size(); i++)
                            {
                                finalDisplay = finalDisplay + childObject.currentFriendsInChat.get(i);
                                if(i != childObject.currentFriendsInChat.size() -1)
                                {
                                    finalDisplay = finalDisplay + ", ";
                                }
                                childObject.displayName = finalDisplay;
                            }
                        }

                    }

                    //completed
                    System.out.println("CHAT SCAN USER INFORMATION COMPLETE: " + chatList.size());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
