package com.example.firebaseimagetest.RecyclerViewPending;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;

public class RCViewPending extends RecyclerView.ViewHolder{
    public TextView mUsername;
    public Button pAdd;
    public Button pRevoke;

    public RCViewPending(View itemView){
        super(itemView);
        mUsername = itemView.findViewById(R.id.username);
        pAdd = itemView.findViewById(R.id.accept_friend);
        pRevoke = itemView.findViewById(R.id.revoke_friend);

    }
}
