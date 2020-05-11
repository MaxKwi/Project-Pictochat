package com.example.firebaseimagetest.RecyclerViewFollow;

import android.content.Context;
import android.provider.ContactsContract;
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
import com.google.firebase.database.Query;
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

        if(UserInformation.friendsList.contains(usersList.get(holder.getLayoutPosition()).getUid()) || UserInformation.pendingList.contains(usersList.get(holder.getLayoutPosition()).getUid())){
            holder.mAdd.setText("Remove");
        }
        else{
            holder.mAdd.setText("Add");
        }

        holder.mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String otherUserID = usersList.get(holder.getLayoutPosition()).getUid();
                if(UserInformation.pendingList.contains(otherUserID))
                {
                    //System.out.println("FRIEND SWITCH 1");
                    holder.mAdd.setText("Add");
                    FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("pending").child(otherUserID).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("users").child(otherUserID).child("incoming").child(userID).removeValue();
                }
                else if(!UserInformation.friendsList.contains(otherUserID)){
                    //System.out.println("FRIEND SWITCH 2");
                    holder.mAdd.setText("Remove");
                    FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("pending").child(otherUserID).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("users").child(otherUserID).child("incoming").child(userID).setValue(true);
                    //AddUserToPending(1, usersList.get(holder.getLayoutPosition()).getUid());

//                    String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
//                    FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat").child(key).setValue(true);
//                    FirebaseDatabase.getInstance().getReference("users").child(usersList.get(position).getUid()).child("chat").child(key).setValue(true);

                }
                else if(UserInformation.friendsList.contains(otherUserID))
                {
                    //System.out.println("FRIEND SWITCH 3");
                    holder.mAdd.setText("Add");
                    FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("friends").child(otherUserID).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("users").child(otherUserID).child("friends").child(userID).removeValue();

                    RemoveChatChild(otherUserID);

                }
                else{
                    //System.out.println("FRIEND SWITCH 4");
                    holder.mAdd.setText("Add");


                    //FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("friends").child(otherUserID).removeValue();
                    //AddUserToPending(-1, usersList.get(holder.getLayoutPosition()).getUid());
                }
            }
        });
    }

    private void RemoveChatChild(final String otherUID)
    {
        System.out.println("REMOVING CHAT CHILD ");
        DatabaseReference chatIDDB = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat");
//        chatIDDB.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot ds : dataSnapshot.getChildren())
//                {
//                    String linkedUID = ds.getValue().toString();
//                    String linkedPath = ds.getKey();
//                    if(linkedUID.equals(otherUID))
//                    {
//                        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat").child(linkedPath).removeValue();
//                        FirebaseDatabase.getInstance().getReference().child("users").child(otherUID).child("chat").child(linkedPath).removeValue();
//                        FirebaseDatabase.getInstance().getReference().child("chat").child(linkedPath).removeValue();
//                        System.out.println("CHAT CHILD REMOVED");
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
        chatIDDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String linkedUID = ds.getValue().toString();
                    String linkedPath = ds.getKey();
                    if(linkedUID.equals(otherUID))
                    {
                        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat").child(linkedPath).removeValue();
                        FirebaseDatabase.getInstance().getReference().child("users").child(otherUID).child("chat").child(linkedPath).removeValue();
                        FirebaseDatabase.getInstance().getReference().child("chat").child(linkedPath).removeValue();
                        System.out.println("CHAT CHILD REMOVED");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
