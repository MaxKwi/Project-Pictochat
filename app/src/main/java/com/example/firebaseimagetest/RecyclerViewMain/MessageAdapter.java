package com.example.firebaseimagetest.RecyclerViewMain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.ChatActivity;
import com.example.firebaseimagetest.CircleTransform;
import com.example.firebaseimagetest.MainActivity;
import com.example.firebaseimagetest.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RCViewChatHolders>
{

    private List<MessageObject> messageList;
    private Context context;
    private String username = "";
    private Context mContext;

    public MessageAdapter(List<MessageObject> msgList){
        this.messageList = msgList;
    }

    @NonNull
    @Override
    public RCViewChatHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_message_item, null);
        RCViewChatHolders rcv = new RCViewChatHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final RCViewChatHolders holder, final int position) {

        holder.mMessage.setText(messageList.get(position).getText()); //message object and mmessage / sender redundency FIX
        holder.mSender.setText(messageList.get(position).getSenderId());
        String currentUid = messageList.get(position).getText();
        DatabaseReference pfpDb = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid).child("profileImageUrl");
        pfpDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String pfpUid = dataSnapshot.getValue().toString();
                if(!pfpUid.equals("default"))
                {
                    Picasso.with(mContext)
                            .load(pfpUid)
                            .fit()
                            .transform(new CircleTransform())
                            .into(holder.mUserPfp);
                }
                else
                {
                    holder.mUserPfp.setImageResource(R.drawable.ic_person_black_24dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

//        System.out.println("creating horizontal linear layout");
//        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(mContext);
//        LinearLayout linearLayout = new LinearLayout(mContext);
//        ViewGroup.LayoutParams prams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        linearLayout.setLayoutParams(prams);
//        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//        horizontalScrollView.addView(linearLayout);
//        holder.mChatLayout.addView(horizontalScrollView);
//        //holder.mHorisLayout.addView(linearLayout);
//
//        System.out.println("SIZE OF MEDIA URL LIST: " + messageList.get(holder.getAdapterPosition()).getMediaUrlList().size());
//        for(String mediaUri : messageList.get(holder.getAdapterPosition()).getMediaUrlList())
//        {
//            ImageView imageView = new ImageView(linearLayout.getContext());
////            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(320,240)); //80 60
//////            imageView.setMaxHeight(80); //20
//////            imageView.setMaxWidth(80);
//
//            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT));
//            imageView.setPadding(10,0,10,0);
//
////            imageView.setAdjustViewBounds(true);
//            Picasso.with(this.context)
//                    .load(mediaUri)
//                    .into(imageView);
//            linearLayout.addView(imageView);
//            System.out.println("ADDED IMAGE VIEW TO LAYOUT");
//        }

        holder.mViewMedia.setText(messageList.get(holder.getAdapterPosition()).getMediaUrlList().size() + " Attachments");

        holder.mViewMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ImageViewer.Builder(view.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                        .setStartPosition(0)
                        .show();
            }
        }); //code that enabled viewing the received media

    }


    @Override
    public int getItemCount() {
        return this.messageList.size();
    }

}
