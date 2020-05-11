package com.example.firebaseimagetest;

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

public class ProfileActivity extends AppCompatActivity {

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
                //MenuItem drawPicture = contextMenu.add(Menu.NONE, 1, 1, "Draw Picture");
                MenuItem pickPicture = contextMenu.add(Menu.NONE, 2, 2, "Pick Picture");
                MenuItem defaultPicture = contextMenu.add(Menu.NONE, 2, 2, "Default Picture");

//                drawPicture.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem menuItem) {
//
//                        return false;
//                    }
//                });

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

}