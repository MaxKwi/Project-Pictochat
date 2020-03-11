package com.example.firebaseimagetest.RecyclerViewMain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.ChatActivity;
import com.example.firebaseimagetest.R;

import java.util.List;

public class RCAdapterMain extends RecyclerView.Adapter<RCViewHoldersMain>{

    private List<ChatObject> chatLists;
    private Context context;

    public RCAdapterMain(List<ChatObject> usersList, Context context){
        this.chatLists = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public RCViewHoldersMain onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_chat_item, null);
        RCViewHoldersMain rcv = new RCViewHoldersMain(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final RCViewHoldersMain holder, final int position) {
        holder.mUsername.setText(chatLists.get(position).getChatId());

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("chatID", chatLists.get(holder.getAdapterPosition()).getChatId());
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);

//                String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
//
//                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat").child(key).setValue(true);
//                FirebaseDatabase.getInstance().getReference("users").child(usersList.get(position).getUid()).child("chat").child(key).setValue(true);

            }
        });


    }


    @Override
    public int getItemCount() {
        return this.chatLists.size();
    }
}
