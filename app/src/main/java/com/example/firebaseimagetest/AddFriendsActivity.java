package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebaseimagetest.RecyclerViewFollow.RCAdapter;
import com.example.firebaseimagetest.RecyclerViewPending.RCAdapterPending;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddFriendsActivity extends AppCompatActivity {

    private ImageView backButton;
    private EditText searchBar;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

//    private RecyclerView pRecyclerView;
//    private RecyclerView.Adapter pAdapter;
//    private RecyclerView.LayoutManager pLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RCAdapter(getDataset(), getApplication());
        mRecyclerView.setAdapter(mAdapter);

//        pRecyclerView = findViewById(R.id.recyclerViewPending);
//        pRecyclerView.setNestedScrollingEnabled(false);
//        pRecyclerView.setHasFixedSize(false);
//        pLayoutManager = new LinearLayoutManager(getApplication());
//        pRecyclerView.setLayoutManager(pLayoutManager);
//        pAdapter = new RCAdapterPending(getDataset(), getApplication());
//        pRecyclerView.setAdapter(pAdapter);

        backButton = (ImageView) findViewById(R.id.back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddFriendsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        searchBar = findViewById(R.id.editText);

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    clearDB();
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        //showPending();

    }

    private void showPending()
    {
        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Query query = usersDB.orderByChild("pending");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    getUserInfo(ds.getKey());
                }
                //pAdapter.notifyDataSetChanged();

                System.out.println(pendingFriends.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo(String uid)
    {

        final String tempFinalUid = uid;

        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
        usersDB.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String tempUid = dataSnapshot.child(tempFinalUid).getKey();
                String tempUsername = dataSnapshot.child(tempFinalUid).child("username").getValue().toString();

                if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("pending").child(tempUid).exists())
                {
                    Users obj = new Users(tempUsername, tempUid);
                    pendingFriends.add(obj);
                }




                //pAdapter.notifyItemInserted(pendingFriends.size() - 1);
//                pAdapter.notifyDataSetChanged();
                //mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //System.out.println("finished");

    }

    private void performSearch() { // FIX SEARCHING
        searchBar.clearFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
        //...perform search
        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = usersDB.orderByChild("username").startAt(searchBar.getText().toString().toUpperCase()).endAt(searchBar.getText().toString().toLowerCase() + "\uf8ff");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String username = "";
                String userID = "";
                String uid = dataSnapshot.getRef().getKey();
                if(dataSnapshot.child("username").getValue() != null){
                    username = dataSnapshot.child("username").getValue().toString();
                    userID = dataSnapshot.child("userID").getValue().toString();
                }
//                if(!userID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
//                {
//                    Users obj = new Users(username, uid);
//                    results.add(obj);
//                    mAdapter.notifyDataSetChanged();
//                }

                if(!userID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    Users obj = new Users(username, uid);
                    results.add(obj);
                    mAdapter.notifyDataSetChanged();
                }

                System.out.println(results.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void clearDB(){
        int size = this.results.size();
        this.results.clear();
        mAdapter.notifyItemRangeRemoved(0, size);
    }

    private ArrayList<Users> pendingFriends = new ArrayList<>();
    private ArrayList<Users> results = new ArrayList<>();
    private ArrayList<Users> getDataset(){
        return results;
    }
}
