package com.example.firebaseimagetest.RecyclerViewMain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RCViewChatHolders>
{

    private List<MessageObject> messageList;
    private Context context;

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

        holder.mMessage.setText(messageList.get(position).getText());
        holder.mSender.setText(messageList.get(position).getSenderId());

        if(messageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty())
        {
            holder.mViewMedia.setVisibility(View.GONE);
        }

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
