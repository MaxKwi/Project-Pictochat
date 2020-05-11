package com.example.firebaseimagetest.RecyclerViewPending;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;
import com.example.firebaseimagetest.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.List;

public class RCAdapterPending extends RecyclerView.Adapter<RCViewPending>{

    private List<Users> usersList;
    private Context context;

    public RCAdapterPending(List<Users> usersList, Context context){
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public RCViewPending onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_pending_item, null);
        RCViewPending rcv = new RCViewPending(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final RCViewPending holder, final int position) {
        holder.mUsername.setText(usersList.get(position).getUsername());

        holder.pAdd.setText("Accept");
        holder.pRevoke.setText("Reject");



        holder.pAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String uid = usersList.get(holder.getLayoutPosition()).getUid();
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("incoming").child(uid).removeValue();
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").child(uid).setValue(true);
                FirebaseDatabase.getInstance().getReference("users").child(uid).child("pending").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                FirebaseDatabase.getInstance().getReference("users").child(uid).child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                createChat(position, FirebaseAuth.getInstance().getCurrentUser().getUid(), uid);
                //InitializeChatID(FirebaseAuth.getInstance().getCurrentUser().getUid(), uid);
                usersList.remove(holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
                notifyItemRangeChanged(holder.getLayoutPosition(), usersList.size());
            }
        });

        holder.pRevoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String uid = usersList.get(holder.getLayoutPosition()).getUid();
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("incoming").child(uid).removeValue();
                FirebaseDatabase.getInstance().getReference("users").child(uid).child("pending").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                usersList.remove(holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
                notifyItemRangeChanged(holder.getLayoutPosition(), usersList.size());
            }
        });
    }

    private void createChat(int position, String myUid, String otherUid)
    {

        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        System.out.println("CHAT VALUES: " + position + ", " + myUid + ", " + otherUid);

        HashMap newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("users/" + otherUid, true);
        newChatMap.put("users/" + myUid, true);

        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
        chatInfoDb.updateChildren(newChatMap);
        System.out.println("Initialized chat map");
        InitializeChatID(myUid, otherUid, key);
        System.out.println("initialized user chats");

    }

    private void InitializeChatID(String myUid, String otherUid, String key)
    {
        //String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        FirebaseDatabase.getInstance().getReference().child("users").child(myUid).child("chat").child(key).setValue(otherUid);
        FirebaseDatabase.getInstance().getReference().child("users").child(otherUid).child("chat").child(key).setValue(myUid);

    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
