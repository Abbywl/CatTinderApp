/*
Authors: Ava Derevlany 51581517 & Abby Liu 15764097
We paired program all aspects of the app!
Ava did the arts
 */

package com.example.cattinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;

public class EditAccountActivity extends AppCompatActivity {

    private static final String TAG = "EditAccountActivity";

    private FirebaseDatabase firebaseRef;
    private DatabaseReference mDatabase;

    // storage reference
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String imageName;

    private TextView usernameBox;
    private TextView rankBox;
    private EditText bio;
    private ImageView lilDemoImage;
    private Bitmap lilDemoBitmap;
    private Uri targetUri;

    private SharedPreferences pref;
    private String key;

    private UserAccount user;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        // initializing database for saving user info
        Log.d(TAG, "OnCreate: started");
        FirebaseApp.initializeApp(this);
        firebaseRef = FirebaseDatabase.getInstance();
        mDatabase = firebaseRef.getReference("Users");

        // making cloud storage for saving image
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("profileImage");

        // get reference to image fro later use
        lilDemoImage = (ImageView) findViewById(R.id.lilImageView);
        lilDemoImage.setVisibility(View.INVISIBLE);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        key = pref.getString("user_key", null);


        // get components of activity to change
        bio         =  (EditText) findViewById(R.id.EditBioTextBox);
        rankBox     = (TextView) findViewById(R.id.currentRankText);
        usernameBox = (TextView) findViewById(R.id.usernameTextBox);

        // set up valueEventListener to get users bio info
        DatabaseReference mPostReference = mDatabase.child(key);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange entered");
                // Get Post object and use the values to update the UI
                //Post post = dataSnapshot.getValue(Post.class);
                // set Bio and rank
                user = dataSnapshot.getValue(UserAccount.class);
                usernameBox.setText("Username: " + user.getUsername().toString());
                rankBox.setText(String.format("Current Rank: %d", user.getRank()));
                bio.setText(user.getBio().toString());

                 //download photo to change using glide, recycler view and card view
                // get the image name
                url = user.getURL().toString();

                // https://firebasestorage.googleapis.com/v0/b/cat-tinder-database.appspot.com/o/profileImage%2F-L_njUJnUcNQObXEMtCR%2F1323000481.jpg?alt=media&token=e0f49632-9914-4e4c-b029-7f33389e7ca6
                Log.d(TAG, url);

                storageRef.child(url).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        Log.d(TAG, "downloaded uri: " + uri);
                        Picasso.with(EditAccountActivity.this).load(uri).fit().into(lilDemoImage);
                        lilDemoImage.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mPostReference.addValueEventListener(postListener);


    }

    public void onClickGetImage(View view) {

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 0);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            targetUri = data.getData();
            imageName = targetUri.getLastPathSegment();
            //Bitmap bitmap;
            try {
                lilDemoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                lilDemoImage.setImageBitmap(lilDemoBitmap);
                lilDemoImage.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "The image failed us, whyyyyyyyyyy");
            }
        }

    }

    public void onClickCreateEditAccount(View view)
    {
        String bioText      = bio.getText().toString();

        mDatabase.child(key).child("bio").setValue(bioText);

        finish();
    }

    public void onClickDelete(View view) {
        SharedPreferences.Editor editor = pref.edit();

        try {
            // delete account and send to home screen
            mDatabase.child(key).removeValue();
        } catch(Exception e) {
            Log.d(TAG, "Something went wrong, could not delete");
        }

        editor.remove("user_key"); // remove data change
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClickLogOut(View view) {
        SharedPreferences.Editor editor = pref.edit();

        editor.remove("user_key"); // remove account from phone
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClickGoBack(View view) {
        finish();
    }
}
