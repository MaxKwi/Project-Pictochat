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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RCAdapterMain extends RecyclerView.Adapter<RCViewHoldersMain>{

    private List<ChatObject> chatLists;
    private Context context;

    private List<String> currentChatUids;
    private String singleChatUid;
    private List<String> currentChatUsernames;
    private String singleChatUsername;
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

        //ARRAY

//        final String currentChatId = chatLists.get(position).getChatId();
//        DatabaseReference currentChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(currentChatId).child("info").child("users");
//        currentChatDb.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists())
//                {
//                    for(DataSnapshot ds : dataSnapshot.getChildren())
//                    {
//                        //System.out.println(ds.getKey());
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
//                        //System.out.println(singleChatUid);
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
//                                    //System.out.println(singleChatUsername);
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
//                                holder.mUsername.setText(singleChatUsername);
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


    }


    @Override
    public int getItemCount() {
        return this.chatLists.size();
    }
}
