package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.firebaseimagetest.RecyclerViewMain.ChatObject;
import com.example.firebaseimagetest.RecyclerViewMain.MessageAdapter;
import com.example.firebaseimagetest.RecyclerViewMain.MessageObject;
import com.example.firebaseimagetest.RecyclerViewMain.RCAdapterMain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    Button mSend;
    EditText mMsgText;

    String chatID;

    ArrayList<MessageObject> messageList;

    DatabaseReference mChatDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageList = new ArrayList<>();



        mRecyclerView = findViewById(R.id.recyclerViewChat);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter(messageList);
        mRecyclerView.setAdapter(mAdapter);

        mMsgText = findViewById(R.id.messageEdit);

        mSend = findViewById(R.id.chatSend);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        chatID = getIntent().getExtras().getString("chatID");
        mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID);

        getChatMessages();

    }

    private void getChatMessages()
    {
        mChatDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    String creatorID = "", message = "";
                    if(dataSnapshot.child("text") != null)
                    {
                        message = dataSnapshot.child("text").getValue().toString();
                    }
                    if(dataSnapshot.child("creator") != null)
                    {
                        creatorID = dataSnapshot.child("creator").getValue().toString();
                    }

                    MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, message);
                    messageList.add(mMessage);
                    mLayoutManager.scrollToPosition(messageList.size() - 1);
                    mAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage()
    {
        String msgText = mMsgText.getText().toString();

        if(!msgText.isEmpty())
        {
            DatabaseReference newMessageDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).push();

            Map newMessageMap = new HashMap<>();
            newMessageMap.put("text", msgText);
            newMessageMap.put("creator", FirebaseAuth.getInstance().getCurrentUser().getUid());

            newMessageDb.updateChildren(newMessageMap);
        }

        mMsgText.setText(null);
    }


}
