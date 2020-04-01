package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.firebaseimagetest.RecyclerViewChatCreate.RCAdapterChatCreate;
import com.firebase.ui.auth.ui.phone.CountryListSpinner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiRoomChatActivity extends AppCompatActivity {

    Button createChatRoom;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    ArrayList<Users> friendsList;
    ArrayList<String> tempFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_room_chat);

        createChatRoom = findViewById(R.id.createChat);
        friendsList = new ArrayList<>();
        tempFriends = new ArrayList<>();

        InitializeRecyclerView();

        GetTempFriends();

        GetFriends();

        createChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateChat();
            }
        });

    }

    private void CreateChat()
    {

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        HashMap newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("users/" + myUid, true);

        boolean validChat = false;

        int selectedFriends = 0;

        for(Users mUser : friendsList)
        {
            if(mUser.getSelected())
            {
                selectedFriends++;
            }
        }

        if(selectedFriends > 1)
        {
            validChat = true;
        }


        if(validChat)
        {
            for(Users mUser : friendsList)
            {
                if(mUser.getSelected())
                {
                    newChatMap.put("users/" + mUser.getUid(), true);
                    FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid()).child("chat").child(key).setValue(true);

                }
            }




            DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
            chatInfoDb.updateChildren(newChatMap);

            FirebaseDatabase.getInstance().getReference().child("users").child(myUid).child("chat").child(key).setValue(true);

            Toast.makeText(this, "Chat Room Created!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MultiRoomChatActivity.this, MainActivity.class);
            startActivity(intent);

        }
        else
        {
            Toast.makeText(this, "Insufficient Amount of Users: " + selectedFriends, Toast.LENGTH_SHORT).show();
        }


    }

    private void InitializeRecyclerView()
    {
        mRecyclerView = findViewById(R.id.chatUsers);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RCAdapterChatCreate(friendsList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void GetTempFriends()
    {
        DatabaseReference friendsDb = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");
        friendsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //tempFriends.clear();

                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.getValue() != null)
                    {
                        for(DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            tempFriends.add(ds.getKey());
                        }
                        //mAdapter.notifyDataSetChanged();
                    }
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
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.getValue() != null)
                    {
                        for(DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            if(tempFriends.contains(ds.child("userID").getValue().toString()))
                            {
                                friendsList.add(new Users(ds.child("username").getValue().toString(), ds.child("userID").getValue().toString()));
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
