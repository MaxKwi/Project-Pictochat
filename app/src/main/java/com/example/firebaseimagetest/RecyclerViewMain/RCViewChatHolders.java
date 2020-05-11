package com.example.firebaseimagetest.RecyclerViewMain;

import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;

public class RCViewChatHolders extends RecyclerView.ViewHolder{
    public TextView mMessage, mSender;
    public Button mViewMedia;
    public LinearLayout mLayout;
    public LinearLayout mChatLayout;
    public ImageView mUserPfp;
    //public HorizontalScrollView mHorisLayout;
    //public LinearLayout mImagesLayout;

    public RCViewChatHolders(View itemView){
        super(itemView);
        mSender = itemView.findViewById(R.id.sender);
        mLayout = itemView.findViewById(R.id.chatLayout);
        mMessage = itemView.findViewById(R.id.message);
        mViewMedia = itemView.findViewById(R.id.viewMedia);
        mChatLayout = itemView.findViewById(R.id.chatLayout);
        mUserPfp = itemView.findViewById(R.id.chatPfp);
        //mHorisLayout = itemView.findViewById(R.id.HorisScrollView);
        //mImagesLayout = itemView.findViewById(R.id.mediaLinearLayout);
    }
}
