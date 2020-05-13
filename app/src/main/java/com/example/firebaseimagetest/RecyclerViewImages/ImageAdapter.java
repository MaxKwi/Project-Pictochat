package com.example.firebaseimagetest.RecyclerViewImages;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseimagetest.CircleTransform;
import com.example.firebaseimagetest.R;
import com.example.firebaseimagetest.Upload;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>
{

    private Context mContext;
    private List<Upload> mUploads;
    private OnItemClickListener mListener;
    private String username="";

    public ImageAdapter(Context context, List<Upload> uploads)
    {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, int position) {
        final Upload uploadCurrent = mUploads.get(position);

        DatabaseReference usernameDB = FirebaseDatabase.getInstance().getReference().child("users").child(uploadCurrent.getUid()).child("username");
        usernameDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                System.out.println("username yes");

                username = dataSnapshot.getValue().toString();
                holder.textViewName.setText(username);
                Picasso.with(mContext)
                        .load(uploadCurrent.getImageUrl())
                        .placeholder(R.mipmap.ic_launcher)
                        .fit()
                        //.centerCrop()
                        .into(holder.imageView);

                final DatabaseReference pfpDb = FirebaseDatabase.getInstance().getReference().child("users").child(uploadCurrent.getUid()).child("profileImageUrl");
                pfpDb.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        System.out.println("getting the pfp uid");
                        String pfpUid = dataSnapshot.getValue().toString();
                        if(!pfpUid.equals("default"))
                        {
                            Picasso.with(mContext)
                                    .load(pfpUid)
                                    .fit()
                                    .transform(new CircleTransform())
                                    .into(holder.pfp);
                        }
                        else
                        {
                            holder.pfp.setImageResource(R.drawable.ic_person_black_24dp);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener
    {

        public TextView textViewName;
        public ImageView imageView;
        public ImageView pfp;

        final GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e){
                Log.d("TEST", "DOUBLE TAP");
                if(mListener != null)
                {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                    {
                        mListener.onDoubleTap(position);
                    }
                }
                return true;
            }
        });

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);
            pfp = itemView.findViewById(R.id.pfp);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            itemView.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    return gestureDetector.onTouchEvent(event);
                }
            });

        }

        @Override
        public void onClick(View view) {
            if(mListener != null)
            {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION)
                {
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            //contextMenu.setHeaderTitle("Select Action");
            MenuItem doWhatever = contextMenu.add(Menu.NONE, 1, 1, "Save");
//            MenuItem delete = contextMenu.add(Menu.NONE, 2, 2, "Delete");

            doWhatever.setOnMenuItemClickListener(this);
//            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            if(mListener != null)
            {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION)
                {
                    switch(menuItem.getItemId())
                    {
                        case 1:
                            mListener.onSaveClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteClick(position);
                            return true;
                    }
                }
            }

            return false;
        }
    }

    public interface OnItemClickListener
    {
        void onItemClick(int position);

        void onDoubleTap(int position);

        void onSaveClick(int position);

        void onDeleteClick(int position);


    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener = listener;
    }

}