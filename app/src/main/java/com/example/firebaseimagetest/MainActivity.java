package com.example.firebaseimagetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "MainActivity";

    //private Button mButtonChooseImage;
    //private Button mButtonUpload;
    //private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    //private ImageView mImageView;
    //private ProgressBar mProgressBar;

    private ImageView profileIcon;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Removing notification bar
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_main);

        //mButtonChooseImage = findViewById(R.id.button_choose_image);
        //mButtonUpload = findViewById(R.id.button_upload);
        //mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        //mImageView = findViewById(R.id.image_view);
        //mProgressBar = findViewById(R.id.progress_bar);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads"); //String is the location in the string
        //mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fir-image-test-fb7e4.appspot.com/");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        profileIcon = (ImageView) findViewById(R.id.profile);
        Uri userUri = user.getPhotoUrl();

        if(userUri != null)
        {
            profileIcon.setImageURI(userUri);
        }

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view profile
            }
        });

        profileIcon.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.setHeaderTitle("Select Action");
                MenuItem viewProfile = contextMenu.add(Menu.NONE, 1, 1, "View Profile");
                MenuItem signOut = contextMenu.add(Menu.NONE, 2, 2, "Sign Out");

                viewProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        return false;
                    }
                });

                signOut.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        mAuth.signOut();
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
    }


}
