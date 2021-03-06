package com.example.firebaseimagetest.RecyclerViewMain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.ChatActivity;
import com.example.firebaseimagetest.CircleTransform;
import com.example.firebaseimagetest.R;
import com.example.firebaseimagetest.UserInformation;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RCAdapterMain extends RecyclerView.Adapter<RCViewHoldersMain>{

    private List<ChatObject> chatLists;
    private Context context;

    private List<String> currentChatUids;
    private String singleChatUid;
    private List<String> currentChatUsernames;
    private String singleChatUsername;
    private List<String> finalUsernames;
    private boolean single = false;

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

        currentChatUsernames = new ArrayList<>();
        currentChatUids = new ArrayList<>();

        holder.mUsername.setText(chatLists.get(position).getChatId()); //run first
        //holder.mUsername.setText(getUsernames(position));
        //ARRAY

        //holder.mUsername.setText("yes");

        //System.out.println("RC ADAPTER MAIN SYSTEM OUT");

        String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        System.out.println("MUID IS " + mUid);
        System.out.println("NUMBER OF USERS IN TEMP UID FRIENDS" + UserInformation.chatList.get(position).tempUidFriendsInChat.size());
        System.out.println("POSITION RN " + position);
        if(UserInformation.chatList.get(position).tempUidFriendsInChat.size() > 1)
        {
            holder.mPfp.setImageResource(R.drawable.ic_group_black_24dp);
        }
        else if(UserInformation.chatList.get(position).tempUidFriendsInChat.size() == 1)
        {
            String targetUid = "";
            for(String currentUid : UserInformation.chatList.get(position).tempUidFriendsInChat)
            {
                if(currentUid != mUid)
                {
                    targetUid = currentUid;
                    break;
                }
            }
            DatabaseReference targetDb = FirebaseDatabase.getInstance().getReference().child("users").child(targetUid).child("profileImageUrl");
            targetDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String pfpUid = dataSnapshot.getValue().toString();
                    System.out.println("PFP UID OF THE FRIEND IS: "+ pfpUid);
                    if(!pfpUid.equals("default") && !pfpUid.equals(""))
                    {
                        Picasso.with(context)
                                .load(pfpUid)
                                .fit()
                                .transform(new CircleTransform())
                                .into(holder.mPfp);
                    }
                    else
                    {
                        holder.mPfp.setImageResource(R.drawable.ic_person_black_24dp);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), ChatActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("chatID", chatLists.get(holder.getAdapterPosition()).getChatId());
//                intent.putExtras(bundle);
                intent.putExtra("chatObject", chatLists.get(holder.getAdapterPosition()));
                view.getContext().startActivity(intent);

//                String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
//
//                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat").child(key).setValue(true);
//                FirebaseDatabase.getInstance().getReference("users").child(usersList.get(position).getUid()).child("chat").child(key).setValue(true);

            }
        });

        //holder.mUsername.setText(UserInformation.chatList.get(position).displayName);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                if(UserInformation.chatList.size() > 0)
                {
                    holder.mUsername.setText(UserInformation.chatList.get(position).displayName);

                }

            }
        }, 1000);

    }

//    private String getUsernames(int position)
//    {
//
//        single = false;
//        final String currentChatId = chatLists.get(position).getChatId();
//        DatabaseReference currentChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(currentChatId).child("info").child("users");
//        currentChatDb.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists())
//                {
//                    for(DataSnapshot ds : dataSnapshot.getChildren())
//                    {
//                        System.out.println(ds.getKey());
//                        if(!ds.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
//                        {
//                            currentChatUids.add(ds.getKey());
//                        }
//                    }
//
//                    if(currentChatUids.size() == 1) //single
//                    {
//                        single = true;
//                        singleChatUid = currentChatUids.get(0);
//                        System.out.println(singleChatUid);
//                    }
//
//                    DatabaseReference currentFriendsDb = FirebaseDatabase.getInstance().getReference().child("users");
//                    currentFriendsDb.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if(dataSnapshot.exists())
//                            {
//                                if(single)
//                                {
//                                    singleChatUsername = dataSnapshot.child(singleChatUid).child("username").getValue().toString();
//                                    System.out.println(singleChatUsername);
//                                }
//                                else if(!single)
//                                {
//                                    for(String currentUid : currentChatUids)
//                                    {
//                                        for(DataSnapshot ds : dataSnapshot.getChildren())
//                                        {
//                                            if(ds.child("userID").getValue().toString().equals(currentUid))
//                                            {
//                                                currentChatUsernames.add(ds.child(currentUid).child("username").getValue().toString());
//                                            }
//                                        }
//                                    }
//
//                                    for(int i = 0; i < currentChatUsernames.size(); i++)
//                                    {
//                                        if(i == 0)
//                                        {
//                                            singleChatUsername = currentChatUsernames.get(0);
//                                        }
//                                        else
//                                        {
//                                            singleChatUsername = singleChatUsername + ", " + currentChatUsernames.get(i);
//                                        }
//                                    }
//
//                                }
//
//                                //singlechatusername
//                                System.out.println(singleChatUsername);
//
//
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//        return singleChatUsername;
//
//    }


    @Override
    public int getItemCount() {
        return this.chatLists.size();
    }
}
