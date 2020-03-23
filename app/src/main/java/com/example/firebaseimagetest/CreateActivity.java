package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.telecom.Call;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import yuku.ambilwarna.AmbilWarnaDialog;

public class CreateActivity extends AppCompatActivity {

    private PaintView paintView;

    private static final String TAG = "MainActivity";
    private RelativeLayout mLayout;
    int mDefaultColor;
    int newColor;

    private ImageView sendButton, drawMode, drawColor, eraser, bgcolor, savebutton, clear;

    private ProgressBar progressBar;

    private static final int PICK_IMAGE_REQUEST = 1;
    //private static final String TAG = "MainActivity";

//    private Button mButtonChooseImage;
//    private Button mButtonUpload;
//    private TextView mTextViewShowUploads;
//    private EditText mEditTextFileName;
//    private ImageView mImageView;
//    private ProgressBar mProgressBar;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    private int drawModeInt = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);


        //progressBar.setVisibility(View.INVISIBLE);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_draw);

        sendButton = (ImageView) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }
        });

        drawMode = (ImageView) findViewById(R.id.drawMode);

        drawMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawModeInt++;

                if(drawModeInt % 3 == 0)
                {
                    paintView.blur();

                    Toast.makeText(CreateActivity.this, "Draw Mode: Blur", Toast.LENGTH_SHORT).show();
                }
                else if(drawModeInt % 2 == 0)
                {
                    paintView.emboss();
                    Toast.makeText(CreateActivity.this, "Draw Mode: Emboss", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    paintView.normal();
                    Toast.makeText(CreateActivity.this, "Draw Mode: Normal", Toast.LENGTH_SHORT).show();
                }

            }
        });

        drawColor = (ImageView) findViewById(R.id.drawColor);

        drawColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallColorPicker();
                paintView.changeColor(newColor);
            }
        });

        eraser = (ImageView) findViewById(R.id.eraser);

        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.changeEraser();
            }
        });

        bgcolor = (ImageView) findViewById(R.id.bgcolor);

        bgcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallColorPicker();
                paintView.changeBGColor(newColor);
            }
        });

        clear = (ImageView) findViewById(R.id.clear);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.clear();
            }
        });

        savebutton = (ImageView) findViewById(R.id.savebutton);

        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveImage();
            }
        });

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

        paintView = (PaintView) findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);


        mLayout = (RelativeLayout) findViewById(R.id.activity_main);
        mDefaultColor = ContextCompat.getColor(CreateActivity.this, R.color.colorPrimary);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads"); //string is the location in the string
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("uploads");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            mImageUri = data.getData();

            //Picasso.with(this).load(mImageUri).into(mImageView);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType((cR.getType(uri)));
    }

    private void uploadFile(){

        if (mImageUri != null){
            //System.out.println("works");
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
            //System.out.println("past storage ref");
            //System.out.println("past storage ref");
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

                            Toast.makeText(CreateActivity.this, "Upload successful", Toast.LENGTH_LONG).show();

//                            Upload upload =  new Upload(mEditTextFileName.getText().toString().trim(), taskSnapshot.getUploadSessionUri().toString());
//                            String uploadId = mDatabaseRef.push().getKey();
//                            mDatabaseRef.child(uploadId).setValue(upload);

                            //System.out.println("b4 task");
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();

                            //Log.d(TAG, "onSuccess: firebase download url: " + downloadUrl.toString()); //use if testing...don't need this line.
                            //Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),downloadUrl.toString());
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Upload upload = new Upload("Doodle", downloadUrl.toString(), uid);

                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);

                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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



        else{
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Doodle", null);
        return Uri.parse(path);
    }

    private void UploadImage()
    {
        if(mUploadTask == null || !mUploadTask.isInProgress())
        {
            paintView.buildDrawingCache();
            Bitmap image = paintView.getmBitmap();
            if(isStoragePermissionGranted())
            {
                mImageUri = getImageUri(this, image);
                uploadFile();

            }
            else
            {
                Toast.makeText(this, "Storage Permissions are not granted, please enable them in the settings.", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void SaveImage()
    {
        paintView.buildDrawingCache();
        Bitmap image = paintView.getmBitmap();
        if(isStoragePermissionGranted()) {
            MediaStore.Images.Media.insertImage(getContentResolver(), image, "Doodle", "Doodle");  // Saves the image.
            Toast.makeText(this, "Saved to gallery.", Toast.LENGTH_SHORT).show();
        }
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



    private void CallColorPicker()
    {

        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                newColor = color;
            }
        });

        dialog.show();

    }
}
