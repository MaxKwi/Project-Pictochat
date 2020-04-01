package com.example.firebaseimagetest.RecyclerViewChatCreate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;
import com.example.firebaseimagetest.Users;

import java.util.ArrayList;
import java.util.List;

public class RCAdapterChatCreate extends RecyclerView.Adapter<RCViewChatCreate>{

    private ArrayList<Users> usersList;

    public RCAdapterChatCreate(ArrayList<Users> usersList){
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public RCViewChatCreate onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_multiroom_user, null);
        RCViewChatCreate rcv = new RCViewChatCreate(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final RCViewChatCreate holder, final int position) {

        holder.mUsername.setText(usersList.get(position).getUsername());

        holder.mAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                usersList.get(holder.getAdapterPosition()).setSelected(b);
            }
        });
    }



    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
