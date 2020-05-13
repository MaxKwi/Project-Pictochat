package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
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
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SavedActivity extends AppCompatActivity implements SavedAdapter.OnItemClickListener{

    private RecyclerView mRecyclerView;
    private SavedAdapter mAdapter;

    private ProgressBar mProgressCircle;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private List<Upload> mUploads;
    private StorageReference userSB;
    private FirebaseUser tempUser;
    private FirebaseAuth mAuth;
    private Uri mImageUri;
    private StorageTask mUploadTask;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_saved);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext()
                                , MainActivity.class));
                        overridePendingTransition(0,0);
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
                        return true;
                }
                return false;
            }
        });

        mProgressCircle = findViewById(R.id.progress_circle);

        mRecyclerView = findViewById(R.id.recycler_view);
//        mRecyclerView.setHasFixedSize(true);
        int columns = 3;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columns));

        mUploads = new ArrayList<>();

        mAdapter = new SavedAdapter(SavedActivity.this, mUploads);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(SavedActivity.this);

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
                    mProgressCircle.setVisibility(View.INVISIBLE);
                    for (DataSnapshot postSnapshot : dataSnapshot.child("saved").getChildren()) {
                        Upload upload = postSnapshot.getValue(Upload.class);
                        upload.setKey(postSnapshot.getKey());

                        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();

                        if (mUploads.isEmpty()) {
                            mUploads.add(upload);
                        } else {
                            mUploads.add(0, upload);
                        }


                        mAdapter = new SavedAdapter(SavedActivity.this, mUploads);

                        mRecyclerView.setAdapter(mAdapter);

                        mAdapter.setOnItemClickListener(SavedActivity.this);

                        mAdapter.notifyDataSetChanged();


                    }



                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }
                @Override
    public void onItemClick(int position) {
        //Toast.makeText(this, "Normal click at position: " + position, Toast.LENGTH_SHORT).show();
        new ImageViewer.Builder(this, Collections.singletonList(mUploads.get(position).getImageUrl()))
                .show();
    }

    @Override
    public void onSaveClick(int position) {
        //Doesn't work, please fix saving selected image to device
        final Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();
        String url = selectedItem.getImageUrl();
        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        final Uri img = Uri.parse(Uri.decode(url));

//        Bitmap currentImage = getContactBitmapFromURI(SavedActivity.this, img);
        Bitmap currentImage = null;
        try {
            currentImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), img);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(url);
        System.out.println(img);
        saveImage(currentImage);
        Toast.makeText(this, "Saved to device", Toast.LENGTH_SHORT).show();

    }

    public Bitmap getContactBitmapFromURI(Context context, Uri uri) {
        try {

            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input == null) {
                return null;
            }
            return BitmapFactory.decodeStream(input);
        }
        catch (FileNotFoundException e)
        {

        }
        return null;
    }

    private void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        System.out.println(mUploads.get(position).getImageUrl()); //must include filepath in order to be downloaded? (name of image)
        System.out.println(mUploads.get(position).getUid());
        System.out.println(mUploads.get(position).getKey());
        System.out.println(mUploads.get(position).getName());

        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
             sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                 Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
// Tell the media scanner about the new file so that it is
// immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    @Override
    public void onDeleteClick(int position) {
        userSB = FirebaseStorage.getInstance().getReference().child("saved").child(tempUser.getUid());
        final Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();
        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        deleteImageInfo(userID, selectedKey);
    }

    private void deleteImageInfo(String userID, String selectedKey){
        FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("saved").child(selectedKey).removeValue();
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
