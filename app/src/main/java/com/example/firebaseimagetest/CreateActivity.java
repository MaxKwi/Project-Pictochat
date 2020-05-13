package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
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

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;

public class CreateActivity extends AppCompatActivity {

    private PaintView paintView;

    private static final String TAG = "MainActivity";
    private RelativeLayout mLayout;
    int mDefaultColor;
    int mCurrentColor;
    int newColor;
    boolean backgroundColor;
    int savedProgress = 20;
    int savedItem = 0;

    private ImageView sendButton, drawMode, drawColor, eraser, bgcolor, savebutton, clear, undo, redo;

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

    private int drawModeInt = 1;

    ArrayList<String> currentFriends;
    ArrayList<String> currentUidFriends;
    String[] listItems;
    boolean[] checkedItems;
    ArrayList<Integer> mUserItems;
    ArrayList<String> targetUidDestination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        currentFriends  = new ArrayList<>();
        currentUidFriends = new ArrayList<>();
        mUserItems = new ArrayList<>();
        targetUidDestination = new ArrayList<>();
        getFriendItems();
//        friendsArrayToList();

        //progressBar.setVisibility(View.INVISIBLE);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_draw);

        sendButton = (ImageView) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //UploadImage();
                targetUidDestination.clear();
                friendsArrayToList();
                mUserItems.clear();

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(CreateActivity.this);
                mBuilder.setTitle("Select Where To Send");
                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked)
                    {

                        if(isChecked)
                        {
                            if(!mUserItems.contains(position))
                            {
                                mUserItems.add(position);
                            }
                        }
                        else if(mUserItems.contains(position))
                        {
                            mUserItems.remove((Integer) position);
                        }

                    }
                }); //FEED as a first option? uploading? conversion from usernames to uids?
                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which)
                    {
                        String item = "";
                        String corresUids = "";
                        for(int i = 0; i < mUserItems.size(); i++)
                        {
                            //System.out.println("indexes: " + mUserItems.get(i));
                            item = item + listItems[mUserItems.get(i)];
                            if(i != mUserItems.size() - 1)
                            {
                                item = item + ", ";
                            }

                            targetUidDestination.add(currentUidFriends.get(mUserItems.get(i)));

                        }
                        System.out.println(item);
                        System.out.println(targetUidDestination);

                        if(mUserItems.contains(0)) //feed was selected as one of the options
                        {
                            System.out.println("Feed included");
                            UploadImage();
                        }
                        else
                        {
                            System.out.println("No feed included");
                            BeginSendingToTargets();
                        }

                    }
                });
                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for(int i = 0; i < checkedItems.length; i++)
                        {
                            checkedItems[i] = false;
                            mUserItems.clear();
                        }
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
                //System.out.println("after");

            }
        });

        drawMode = (ImageView) findViewById(R.id.drawMode);

        drawMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrokeAlertDialog();
            }
        });

        drawColor = (ImageView) findViewById(R.id.drawColor);

        drawColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallColorPicker();
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
                backgroundColor=true;
                CallColorPicker();
            }
        });

        undo = (ImageView) findViewById(R.id.undo);

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.undo();
            }
        });

        redo = (ImageView) findViewById(R.id.redo);

        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.redo();
            }
        });

        clear = (ImageView) findViewById(R.id.clear);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteDialogMethod();
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
        //mDefaultColor = ContextCompat.getColor(CreateActivity.this, R.color.colorPrimary);
        mDefaultColor = Color.RED;
        mCurrentColor = mDefaultColor;
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads"); //string is the location in the string
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("uploads");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

    }

    private void getFriendItems()
    {
        currentFriends.clear();
        currentUidFriends.clear();
        currentFriends.add("Your Feed");
        currentUidFriends.add("feed_uid_placeholder");
//        for(String friendUid : UserInformation.friendsList)
//        {
//            currentUidFriends.add(friendUid);
//            DatabaseReference userNameDb = FirebaseDatabase.getInstance().getReference().child("users").child(friendUid).child("username");
//            userNameDb.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    String username = dataSnapshot.getValue().toString();
//                    currentFriends.add(username);
//                    System.out.println(username);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//        }
        for(ChatInfo chatUid : UserInformation.chatList)
        {
            if(!currentFriends.contains(chatUid.displayName) && !currentUidFriends.contains(chatUid.chatId))
            {
                currentUidFriends.add(chatUid.chatId);
                currentFriends.add(chatUid.displayName);
            }

            //System.out.println("SIZE OF TEMP FRIENDS IN CHAT INFO: " + chatUid.tempUidFriendsInChat.size());
        }

        //System.out.println("first");

    }

    private void friendsArrayToList()
    {
        //System.out.println(currentFriends.size());
        listItems = new String[currentFriends.size()];
        for(int i = 0; i < currentFriends.size(); i++)
        {
            listItems[i] = currentFriends.get(i);
            //System.out.println("item:" + listItems[i]);
        }
        //System.out.println("size: " + listItems.length);
        checkedItems = new boolean[listItems.length];

        //System.out.println("seconds");
    }

    private void DeleteDialogMethod(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Clear canvas?");
//        alertDialog.setMessage("Message");
        alertDialog.setCancelable(false);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        paintView.clear();
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    private void StrokeAlertDialog(){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        popDialog.setCancelable(false);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seek.setMin(1);
        }
        seek.setProgress(savedProgress);
        seek.setKeyProgressIncrement(1);

        popDialog.setTitle("Change style and size");
        popDialog.setView(seek);

        final TextView textView = new TextView(this);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                int val = (progress * ((seekBar.getWidth()+150) - 10 * seekBar.getThumbOffset())) / seekBar.getMax();
                textView.setText("" + progress);
                textView.setX(seekBar.getX() + val + seekBar.getThumbOffset() / 2);
                savedProgress = progress;
                paintView.changeSize(progress);
            }

            public void onStartTrackingTouch(SeekBar arg0) {
                //do something

                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                //do something

            }
        });

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.addView(seek);
        linearLayout.addView(textView);
        popDialog.setView(linearLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams sb = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, 20, 0, 0);
        seek.setLayoutParams(sb);
        linearLayout.setPadding(0,60,0,0);
        seek.setPadding(60, 0, 60, 0);
        linearLayout.setLayoutParams(lp);

        String[] items = {"Normal","Emboss","Blur"};
        final int checkedItem = savedItem;
        popDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        savedItem=0;
                        paintView.normal();
                        break;
                    case 1:
                        savedItem=1;
                        paintView.emboss();
                        break;
                    case 2:
                        savedItem=2;
                        paintView.blur();
                        break;
                }
            }
        });

        // Button OK
        popDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        popDialog.create();
        popDialog.show();
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

            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

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

                            if(targetUidDestination.size() > 0)
                            {
                                System.out.println("feed + extra targets");
                                UploadImageToTargets();
                            }
                            else
                            {
                                progressBar.setVisibility(View.INVISIBLE);
                            }



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

    private void BeginSendingToTargets()
    {
        paintView.buildDrawingCache();
        Bitmap image = paintView.getmBitmap();
        if(isStoragePermissionGranted())
        {
            mImageUri = getImageUri(this, image);
            UploadImageToTargets();
        }
        else
        {
            Toast.makeText(this, "Storage Permissions are not granted, please enable them in the settings.", Toast.LENGTH_SHORT).show();
        }
    }

    private void UploadImageToTargets()
    {
        if(mImageUri != null)
        {
            System.out.println("SENDING TO TARGETS " + targetUidDestination.size() + ", " + targetUidDestination);
            for(int i = 0; i < targetUidDestination.size(); i++)
            {
                System.out.println("CURRENT CHAT UID TARGET: " + targetUidDestination.get(i));
                DatabaseReference mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(targetUidDestination.get(i)).child("messages");
                String messageId = mChatDb.push().getKey();
                final DatabaseReference newMessageDb = mChatDb.child(messageId);

                final Map newMessageMap = new HashMap<>();
                newMessageMap.put("creator", FirebaseAuth.getInstance().getCurrentUser().getUid());

                String mediaId = newMessageDb.child("media").push().getKey();
                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(targetUidDestination.get(i)).child(messageId).child(mediaId);

                UploadTask uploadTask = filePath.putFile(mImageUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                newMessageMap.put("/media/" + System.currentTimeMillis() + "/", uri.toString());
                                newMessageDb.updateChildren(newMessageMap);
                            }
                        });
                    }
                });

                if(i == targetUidDestination.size() - 1)
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Successfully sent to " + targetUidDestination.size() + " friends", Toast.LENGTH_SHORT).show();
                }

            }
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

        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, mCurrentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                backgroundColor=false;
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                newColor = color;
                mCurrentColor = newColor;
                if(backgroundColor){
                    paintView.changeBGColor(newColor);
                }
                else{
                    paintView.changeColor(newColor);
                }
                backgroundColor=false;
            }
        });

        dialog.show();

    }

}
