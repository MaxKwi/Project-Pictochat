package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseimagetest.RecyclerViewFollow.RCAdapter;
import com.example.firebaseimagetest.RecyclerViewMain.ChatObject;
import com.example.firebaseimagetest.RecyclerViewMain.RCAdapterMain;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "MainActivity";

    //private Button mButtonChooseImage;
    //private Button mButtonUpload;
    //private TextView mTextViewShowUploads;
    private EditText searchBar;
    //private ImageView mImageView;
    //private ProgressBar mProgressBar;

    private ImageView profileIcon, addFriends, createChat;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    DatabaseReference currentUserDb;

    private StorageTask mUploadTask;

    FirebaseUser tempUser;

    FirebaseAuth mAuth;

    private ArrayList<String> tempChats;
    private ArrayList<String> tempFriends;

    private GoogleSignInClient mGoogleSignInClient;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserInformation userInformationListener = new UserInformation();
        userInformationListener.startFetching();

        Fresco.initialize(this);

        mRecyclerView = findViewById(R.id.recycler_view_main);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RCAdapterMain(getDataset(), getApplication());
        mRecyclerView.setAdapter(mAdapter);

        Intent intent = getIntent();
        final boolean db_Initialized = intent.getBooleanExtra("initialized_db", false);
        //System.out.println(db_Initialized);


        //Removing notification bar
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_main);

        //mButtonChooseImage = findViewById(R.id.button_choose_image);
        //mButtonUpload = findViewById(R.id.button_upload);
        //mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);
        searchBar = findViewById(R.id.editText);
        addFriends = findViewById(R.id.addFriend);
        createChat = findViewById(R.id.startChatCreate);
        //mImageView = findViewById(R.id.image_view);
        //mProgressBar = findViewById(R.id.progress_bar);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads"); //String is the location in the string
        //mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fir-image-test-fb7e4.appspot.com/");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        tempUser = mAuth.getCurrentUser();

        currentUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

        currentUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if(!db_Initialized)
                {
                    if(!dataSnapshot.child("profileImageUrl").getValue().toString().equals("default"))
                    {
                        Picasso.with(MainActivity.this).load(dataSnapshot.child("profileImageUrl").getValue().toString()).transform(new CircleTransform()).into(profileIcon);
                    }
                    else
                    {
                        Picasso.with(MainActivity.this)
                                .load(tempUser.getPhotoUrl())
                                .transform(new CircleTransform())
                                .into(profileIcon);
                    }
                }
                else if(db_Initialized)
                {
                    Picasso.with(MainActivity.this)
                            .load(tempUser.getPhotoUrl())
                            .transform(new CircleTransform())
                            .into(profileIcon);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        profileIcon = (ImageView) findViewById(R.id.profile);

        //mDatabaseRef.child("users").child(user.getUid()).child("photoUrl").setValue(user.getPhotoUrl());

//        mDatabaseRef.child("users").child(user.getUid()).child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String photoUrl = (String) dataSnapshot.getValue();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        //profileIcon.setImageBitmap(null);

        //profileIcon.setImageURI(null);
        //profileIcon.setImageURI(user.getPhotoUrl());

        Picasso.with(MainActivity.this)
                .load(user.getPhotoUrl())
                .transform(new CircleTransform())
                .into(profileIcon);

        //new DownloadImageTask(profileIcon).execute(user.getPhotoUrl());

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view profile
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddFriendsActivity.class);
                startActivity(intent);
            }
        });

        createChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MultiRoomChatActivity.class);
                startActivity(intent);
            }
        });

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //clearDB();
                    performSearch();
                    return true;
                }
                return false;
            }
        });



        profileIcon.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                //contextMenu.setHeaderTitle("Select Action");
                MenuItem viewProfile = contextMenu.add(Menu.NONE, 1, 1, "View Profile Activity");
                MenuItem signOut = contextMenu.add(Menu.NONE, 2, 2, "Sign Out");

                viewProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        return false;
                    }
                });

                signOut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        OneSignal.setSubscription(false);

                        mAuth.signOut();
                        mGoogleSignInClient.revokeAccess();

                        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "Successfully Signed Out", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

            }
        });


        //NavigationView Listener
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        return true;
                    case R.id.nav_draw:
                        startActivity(new Intent(getApplicationContext()
                                , CreateActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_feed:
                        startActivity(new Intent(getApplicationContext()
                                , ImagesActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_saved:
                        startActivity(new Intent(getApplicationContext()
                                , SavedActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        tempFriends = new ArrayList<>();
        tempChats = new ArrayList<>();

        getUserChatList(); //works as intended
        //getUsersInChat(); //works as intended
        //GetFriends();

    }

    private void performSearch() { // FIX SEARCHING
        searchBar.clearFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
        //...perform search

        String target = searchBar.getText().toString().toLowerCase();

        clearDB();

        if(target.equals(""))
        {
            getUserChatList();
        }
        else
        {
            for(ChatInfo chatObject : UserInformation.chatList)
            {
                if(chatObject.currentFriendsInChat.contains(target))
                {
                    System.out.println("CURRENT FRIEND LIST ARRAY: " + chatObject.currentFriendsInChat);
                    System.out.println("CHAT ID IN SEARCH: " + chatObject.chatId);
                    ChatObject mChat = new ChatObject(chatObject.chatId);
                    results.add(mChat);
                    getChatData(mChat.getChatId());
                }
            }
            mAdapter.notifyDataSetChanged();
        }

        System.out.println("search complete");

    }

    private void getUserChatList()
    {
        clearDB();
        DatabaseReference mUserChatDb = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat");
        mUserChatDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {

                        ChatObject mChat = new ChatObject(ds.getKey());

                        results.add(mChat);
                        getChatData(mChat.getChatId());
                    }

                    mAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        for(ChatInfo currentChatInfo : UserInformation.chatList)
//        {
//            ChatObject mChat = new ChatObject(currentChatInfo.chatId);
//            mChat.displayName = currentChatInfo.displayName;
//            results.add(mChat);
//            getChatData(mChat.getChatId());
//        }
        System.out.println("USER INFO CHAT LIST SIZE: " + UserInformation.chatList.size());
        System.out.println("RESULTS SIZE: " + results.size());

        //mAdapter.notifyDataSetChanged();

    }

//    private void getUsersInChat()
//    {
//        DatabaseReference mChatDb = FirebaseDatabase.getInstance().getReference().child("chat");
//        mChatDb.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists())
//                {
//
//                    //String usersInCurrentChat = "";
//                    tempFriends.clear();
//                    boolean firstValue = true;
//                    for(String childUid : tempChats)
//                    {
//                        String usersInCurrentChat = "";
//                        for(DataSnapshot cs : dataSnapshot.child(childUid).child("info").child("users").getChildren())
//                        {
//                            if(!cs.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
//                            {
//                                if(firstValue)
//                                {
//                                    usersInCurrentChat = usersInCurrentChat + cs.getKey();
//                                    firstValue = false;
//                                }
//                                else
//                                {
//                                    usersInCurrentChat = usersInCurrentChat + "," + cs.getKey();
//                                }
//                            }
//                        }
//
//                        tempFriends.add(usersInCurrentChat);
//                        System.out.println("TEMP FRIENDS: " + usersInCurrentChat);
//
//                    }
//
//                    //completed
//
//                    //GetFriends();
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    private void GetFriends()
//    {
//        DatabaseReference friendsDb = FirebaseDatabase.getInstance().getReference().child("users");
//        friendsDb.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists())
//                {
//                    if(dataSnapshot.getValue() != null)
//                    {
//                        clearDB();
//                        System.out.println("LENGTH OF TEMP FRIENDS: " + tempFriends.size());
//                        for(String currentFriends : tempFriends)
//                        {
//                            System.out.println("CHECK PHASE");
//                            if(currentFriends.indexOf(",") == -1) //single user
//                            {
//                                System.out.println("SINGLE FRIEND CHAT");
//                                for(DataSnapshot ds : dataSnapshot.getChildren())
//                                {
//                                    if(currentFriends.equals(ds.child("userID").getValue().toString()))
//                                    {
//                                        ChatObject mChat = new ChatObject(ds.getKey());
//                                        results.add(mChat);
//                                        getChatData(mChat.getChatId());
//
//                                        System.out.println("CHAT OBJECT: " + ds.getKey());
//                                    }
//                                }
//                            }
//
//                        }
//
//                        //completed
//
//                        mAdapter.notifyDataSetChanged();
//
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void getChatData(String chatId)
    {

        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("info");
        mChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String chatId = "";
                    if(dataSnapshot.child("id").getValue() != null)
                    {
                        chatId = dataSnapshot.child("id").getValue().toString();
                    }

                    for(DataSnapshot userSnapshot : dataSnapshot.child("users").getChildren())
                    {
                        for(ChatObject mChat : results)
                        {
                            if(mChat.getChatId().equals(chatId))
                            {
                                Users mUser = new Users(userSnapshot.getKey());
                                mChat.addUserToArrayList(mUser);
                                getUserData(mUser);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getUserData(Users mUser)
    {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid());
        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users mUser = new Users(dataSnapshot.getKey());

                if(dataSnapshot.child("notificationKey").getValue() != null)
                {
                    mUser.setNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());

                    for(ChatObject mChat : results)
                    {
                        for(Users mUserIt : mChat.getUserObjectArrayList())
                        {
                            if(mUserIt.getUid().equals(mUser.getUid()))
                            {
                                mUserIt.setNotificationKey(mUser.getNotificationKey());
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
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

    private ArrayList<ChatObject> results = new ArrayList<>();
    private ArrayList<ChatObject> getDataset(){
        return results;
    }

}
