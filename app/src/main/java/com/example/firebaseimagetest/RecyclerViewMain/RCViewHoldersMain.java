package com.example.firebaseimagetest.RecyclerViewMain;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;

public class RCViewHoldersMain extends RecyclerView.ViewHolder{
    public TextView mUsername;
    public LinearLayout mLayout;

    public RCViewHoldersMain(View itemView){
        super(itemView);
        mUsername = itemView.findViewById(R.id.username);
        mLayout = itemView.findViewById(R.id.friendsLayout);
    }
}
