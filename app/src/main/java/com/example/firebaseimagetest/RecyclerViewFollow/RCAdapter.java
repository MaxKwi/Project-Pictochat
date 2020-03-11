package com.example.firebaseimagetest.RecyclerViewFollow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;
import com.example.firebaseimagetest.UserInformation;
import com.example.firebaseimagetest.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RCAdapter extends RecyclerView.Adapter<RCViewHolders>{

    private List<Users> usersList;
    private Context context;

    public RCAdapter(List<Users> usersList, Context context){
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public RCViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_followers_item, null);
        RCViewHolders rcv = new RCViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final RCViewHolders holder, final int position) {
        holder.mUsername.setText(usersList.get(position).getUsername());

        if(UserInformation.friendsList.contains(usersList.get(holder.getLayoutPosition()).getUid())){
            holder.mAdd.setText("Remove");
        }
        else{
            holder.mAdd.setText("Add");
        }

        holder.mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(!UserInformation.friendsList.contains(usersList.get(holder.getLayoutPosition()).getUid())){
                    holder.mAdd.setText("Remove");
                    FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("friends").child(usersList.get(holder.getLayoutPosition()).getUid()).setValue(true);
                    //AddUserToPending(1, usersList.get(holder.getLayoutPosition()).getUid());

                    String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
                    FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat").child(key).setValue(true);
                    FirebaseDatabase.getInstance().getReference("users").child(usersList.get(position).getUid()).child("chat").child(key).setValue(true);

                }
                else{
                    holder.mAdd.setText("Add");
                    FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("friends").child(usersList.get(holder.getLayoutPosition()).getUid()).removeValue();
                    //AddUserToPending(-1, usersList.get(holder.getLayoutPosition()).getUid());
                }
            }
        });
    }

    private void AddUserToPending(int choice, String uid)
    {
        if(choice == 1)
        {
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("pending").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
        }
        else if(choice == -1)
        {
            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("pending").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        }

    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
