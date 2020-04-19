package com.example.firebaseimagetest.RecyclerViewMain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RCViewChatHolders>
{

    private List<MessageObject> messageList;
    private Context context;
    private String username = "";

    public MessageAdapter(List<MessageObject> msgList){
        this.messageList = msgList;
    }

    @NonNull
    @Override
    public RCViewChatHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_message_item, null);
        RCViewChatHolders rcv = new RCViewChatHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final RCViewChatHolders holder, final int position) {

        holder.mMessage.setText(messageList.get(position).getText()); //message object and mmessage / sender redundency FIX
        holder.mSender.setText(messageList.get(position).getSenderId());
        //System.out.println(messageList.get(position).getText());
        DatabaseReference usernameDB = FirebaseDatabase.getInstance().getReference().child("users").child(messageList.get(position).getText()).child("username");
        usernameDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                username = dataSnapshot.getValue().toString();
                holder.mMessage.setText(username);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(messageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty())
        {
            holder.mViewMedia.setVisibility(View.GONE);
        }

        holder.mViewMedia.setText(messageList.get(holder.getAdapterPosition()).getMediaUrlList().size() + " Attachments");

        holder.mViewMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageViewer.Builder(view.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                        .setStartPosition(0)
                        .show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return this.messageList.size();
    }

}
