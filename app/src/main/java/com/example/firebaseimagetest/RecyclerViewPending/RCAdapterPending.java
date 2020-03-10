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
import com.google.firebase.database.FirebaseDatabase;

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
    public void onBindViewHolder(@NonNull final RCViewPending holder, int position) {
        holder.mUsername.setText(usersList.get(position).getUsername());

        holder.pAdd.setText("Accept");
        holder.pRevoke.setText("Revoke");



        holder.pAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String uid = usersList.get(holder.getLayoutPosition()).getUid();
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("pending").child(uid).removeValue();
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").child(uid).setValue(true);
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
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("pending").child(uid).removeValue();
                FirebaseDatabase.getInstance().getReference("users").child(uid).child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                usersList.remove(holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
                notifyItemRangeChanged(holder.getLayoutPosition(), usersList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
