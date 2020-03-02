package com.example.firebaseimagetest.RecyclerViewFollow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

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
    public void onBindViewHolder(@NonNull final RCViewHolders holder, int position) {
        holder.mUsername.setText(usersList.get(position).getUsername());
        //FIX THIS ---> https://youtu.be/z8iAG7LxDgU?t=1202
//        holder.mAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                if(holder.mAdd.getText().equals("Add")){
//                    holder.mAdd.setText("Remove");
//                    FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("friends").child(usersList.get(holder.getLayoutPosition()).getUid()).setValue(true);
//                }
//                else{
//                    holder.mAdd.setText("Add");
//                    FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("friends").child(usersList.get(holder.getLayoutPosition()).getUid()).removeValue();
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
