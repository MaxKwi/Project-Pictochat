package com.example.firebaseimagetest.RecyclerViewFollow;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;

public class RCViewHolders extends RecyclerView.ViewHolder{
    public TextView mUsername;
    public Button mAdd;

    public RCViewHolders(View itemView){
        super(itemView);
        mUsername = itemView.findViewById(R.id.username);
        mAdd = itemView.findViewById(R.id.add);
    }
}
