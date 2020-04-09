package com.example.firebaseimagetest.RecyclerViewChatCreate;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;

public class RCViewChatCreate extends RecyclerView.ViewHolder{
    public TextView mUsername;
    public CheckBox mAdd;

    public RCViewChatCreate(View itemView){
        super(itemView);
        mUsername = itemView.findViewById(R.id.username);
        mAdd = itemView.findViewById(R.id.selectUser);
    }
}
