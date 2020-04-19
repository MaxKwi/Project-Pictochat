package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.firebaseimagetest.RecyclerViewMain.ChatObject;
import com.example.firebaseimagetest.RecyclerViewMain.MediaAdapter;
import com.example.firebaseimagetest.RecyclerViewMain.MessageAdapter;
import com.example.firebaseimagetest.RecyclerViewMain.MessageObject;
import com.example.firebaseimagetest.RecyclerViewMain.RCAdapterMain;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView, mMedia;
    private RecyclerView.Adapter mAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mLayoutManager, mMediaLayoutManager;

    ImageView mSend, mAddMedia;
    EditText mMsgText;

    ChatObject mChatObject;

    ArrayList<MessageObject> messageList;

    DatabaseReference mChatDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageList = new ArrayList<>();

        mChatObject = (ChatObject) getIntent().getSerializableExtra("chatObject");

        mRecyclerView = findViewById(R.id.recyclerViewChat);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter(messageList);
        mRecyclerView.setAdapter(mAdapter);

        mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mMedia.setAdapter(mMediaAdapter);

        mMsgText = findViewById(R.id.messageEdit);

        mSend = findViewById(R.id.chatSend);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mAddMedia = findViewById(R.id.addMedia);
        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        //OneSignal.setSubscription(false);

        //chatID = getIntent().getExtras().getString("chatID");
        mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("messages");

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
                    ArrayList<String> mediaUrlList = new ArrayList<>();
                    if(dataSnapshot.child("text").getValue() != null)
                    {
                        message = dataSnapshot.child("text").getValue().toString();
                    }
                    if(dataSnapshot.child("creator").getValue() != null)
                    {
                        creatorID = dataSnapshot.child("creator").getValue().toString();
                    }
                    if(dataSnapshot.child("media").getChildrenCount() > 0)
                    {
                        for(DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren())
                        {
                            mediaUrlList.add(mediaSnapshot.getValue().toString());
                        }
                    }

                    MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, message, mediaUrlList);
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

    int totalMediaUploaded = 0;
    ArrayList<String> mediaIdList = new ArrayList<>();
    private void sendMessage()
    {
        String msgText = mMsgText.getText().toString();


        String messageId = mChatDb.push().getKey();
        final DatabaseReference newMessageDb = mChatDb.child(messageId);

        final Map newMessageMap = new HashMap<>();
        if(!msgText.toString().isEmpty())
        {
            newMessageMap.put("text", msgText);
        }
        newMessageMap.put("creator", FirebaseAuth.getInstance().getCurrentUser().getUid());


        if(!mediaUriList.isEmpty())
        {
            //totalMediaUploaded = 0;
            for(String mediaUri : mediaUriList)
            {
                String mediaId = newMessageDb.child("media").push().getKey();
                mediaIdList.add(mediaId);
                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child(messageId).child(mediaId);

                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());

                                totalMediaUploaded++;
                                if(totalMediaUploaded == mediaUriList.size())
                                {
                                    updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
                                }
                            }
                        });
                    }
                });
            }
        }
        else
        {
            if(!msgText.toString().isEmpty())
            {
                updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
            }
        }



        mMsgText.setText(null);
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap)
    {
        newMessageDb.updateChildren(newMessageMap);
        mMsgText.setText(null);
        mediaIdList.clear();
        mediaUriList.clear();
        totalMediaUploaded = 0;
        mMediaAdapter.notifyDataSetChanged();


        String message;

        if(newMessageMap.get("text") != null)
        {
            message = newMessageMap.get("text").toString();
        }
        else
        {
            message = "Sent Media";
        }

        for(Users mUser : mChatObject.getUserObjectArrayList())
        {
            if(!mUser.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            {
                new SendNotification(message, "New Message", mUser.getNotificationKey());
            }
        }
    }

    int PICK_IMAGE_INTENT = 1;
    ArrayList<String> mediaUriList = new ArrayList<>();
    private void openGallery()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            if(requestCode == PICK_IMAGE_INTENT)
            {
                if(data.getClipData() == null)
                {
                    mediaUriList.add(data.getData().toString());
                }
                else
                {
                    for(int i = 0; i < data.getClipData().getItemCount(); i++)
                    {
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

                mMediaAdapter.notifyDataSetChanged();
            }
        }
    }
}
