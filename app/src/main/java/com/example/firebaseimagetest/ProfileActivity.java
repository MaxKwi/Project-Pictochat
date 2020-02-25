package com.example.firebaseimagetest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ImageView profileIcon;

    EditText usernameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileIcon = (ImageView) findViewById(R.id.imageView);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

        Picasso.with(ProfileActivity.this)
                .load(user.getPhotoUrl())
                .transform(new CircleTransform())
                .into(profileIcon);

        usernameEdit = (EditText) findViewById(R.id.usernameEdit);
        //usernameEdit.setText(null);
        

    }
}
