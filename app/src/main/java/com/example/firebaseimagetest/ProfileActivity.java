package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity implements ProfileAdapter.OnItemClickListener{

    FirebaseAuth mAuth;
    ImageView profileIcon, saveButton;
    EditText usernameEdit;
    ProgressBar progressBar;

    DatabaseReference currentUserDb;
    Uri mImageUri;
    public static final int PICK_IMAGE = 1;

    FirebaseStorage mStorageRef;
    DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    FirebaseUser tempUser;

    boolean pictureChanged = false;
    String userID;

    private RecyclerView mRecyclerView;
    private ProfileAdapter mAdapter;

    private ProgressBar mProgressCircle;

    private FirebaseStorage mStorage;
    private ValueEventListener mDBListener;
    private List<Upload> mUploads;
    private StorageReference userSB;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final boolean dbInit = getIntent().getBooleanExtra("db_initialized", false);

        progressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        profileIcon = (ImageView) findViewById(R.id.imageView);
        saveButton = (ImageView) findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        tempUser = mAuth.getCurrentUser();

        currentUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        mStorageRef = FirebaseStorage.getInstance();

        Picasso.with(ProfileActivity.this)
                .load(user.getPhotoUrl())
                .transform(new CircleTransform())
                .into(profileIcon);

        usernameEdit = (EditText) findViewById(R.id.usernameEdit);

        currentUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                usernameEdit.setText(dataSnapshot.child("username").getValue().toString());
                userID = dataSnapshot.child("userID").getValue().toString();

                if(!dbInit)
                {
                    if(!dataSnapshot.child("profileImageUrl").getValue().toString().equals("default"))
                    {
                        Picasso.with(ProfileActivity.this).load(dataSnapshot.child("profileImageUrl").getValue().toString()).transform(new CircleTransform()).into(profileIcon);
                    }
                    else
                    {
                        Picasso.with(ProfileActivity.this)
                                .load(tempUser.getPhotoUrl())
                                .transform(new CircleTransform())
                                .into(profileIcon);
                    }
                }
                else
                {
                    Picasso.with(ProfileActivity.this)
                            .load(tempUser.getPhotoUrl())
                            .transform(new CircleTransform())
                            .into(profileIcon);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //usernameEdit.setText(null);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveProfileData();
            }
        });

        profileIcon.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                MenuItem pickPicture = contextMenu.add(Menu.NONE, 1, 1, "Pick picture");
                MenuItem defaultPicture = contextMenu.add(Menu.NONE, 2, 2, "Set default picture");

                pickPicture.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                        return false;
                    }
                });

                defaultPicture.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
                        mDatabaseRef.child("profileImageUrl").setValue("default");
                        return false;
                    }
                });
            }
        });

        mProgressCircle = findViewById(R.id.progress_circle);

        mRecyclerView = findViewById(R.id.recycler_view);
//        mRecyclerView.setHasFixedSize(true);
        int columns = 3;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columns));

        mUploads = new ArrayList<>();

        mAdapter = new ProfileAdapter(ProfileActivity.this, mUploads);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(ProfileActivity.this);

        mRecyclerView.setAdapter(mAdapter);


        mAuth = FirebaseAuth.getInstance();
        tempUser = mAuth.getCurrentUser();

        listenForData();

    }

    private void listenForData(){
        mStorage = FirebaseStorage.getInstance();
        String userID = mAuth.getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.child("uploads").getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());

                    FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

                    if (mUploads.isEmpty()) {
                        mUploads.add(upload);
                    } else {
                        mUploads.add(0, upload);
                    }


                    mAdapter = new ProfileAdapter(ProfileActivity.this, mUploads);

                    mRecyclerView.setAdapter(mAdapter);

                    mAdapter.setOnItemClickListener(ProfileActivity.this);

                    mAdapter.notifyDataSetChanged();

                    mProgressCircle.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK)
        {
            if(data != null && data.getData() != null)
            {
                mImageUri = data.getData();
                Picasso.with(this).load(mImageUri).transform(new CircleTransform()).into(profileIcon);
                pictureChanged = true;
            }
        }

    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType((cR.getType(uri)));
    }

    private void UploadPFP()
    {
        if(mImageUri != null)
        {
            StorageReference fileReference = mStorageRef.getReference().child("profile_pictures").child(userID).child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(userID);

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //mProgressBar.setProgress(0);
                                }
                            }, 5000);

                            Toast.makeText(ProfileActivity.this, "Picture Upload successful", Toast.LENGTH_LONG).show();

//                            Upload upload =  new Upload(mEditTextFileName.getText().toString().trim(), taskSnapshot.getUploadSessionUri().toString());
//                            String uploadId = mDatabaseRef.push().getKey();
//                            mDatabaseRef.child(uploadId).setValue(upload);

                            //System.out.println("b4 task");
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();

                            //Log.d(TAG, "onSuccess: firebase download url: " + downloadUrl.toString()); //use if testing...don't need this line.
                            //Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),downloadUrl.toString());

                            progressBar.setVisibility(View.INVISIBLE);

                            mDatabaseRef.child("profileImageUrl").setValue(downloadUrl.toString());


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            //mProgressBar.setProgress((int)progress);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });

        }
    }

    private void SaveProfileData()
    {
        String newUserName = usernameEdit.getText().toString();
        currentUserDb.child("username").setValue(newUserName);

        if(pictureChanged)
        {
            UploadPFP();
        }

        Toast.makeText(this, "Profile Data Updated!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onSaveClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {
        userSB = FirebaseStorage.getInstance().getReference().child("uploads").child(tempUser.getUid());
        final Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();
        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        deleteImageInfo(userID, selectedKey);
    }

    private void deleteImageInfo(String userID, String selectedKey){
        FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("uploads").child(selectedKey).removeValue();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}