/*
Authors: Ava Derevlany 51581517 & Abby Liu 15764097
We paired program all aspects of the app!
Ava did the arts
 */

package com.example.cattinder;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class NewAccountActivity extends AppCompatActivity {

    private static final String TAG = "NewAccountActivity";

    // database reference
    private DatabaseReference mRef;
    private FirebaseDatabase firebaseRef;
    // storage reference
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;

    private ImageView lilDemoImage;
    private int defaultImage;
    private Uri defaultUri;
    private Bitmap lilDemoBitmap;
    private Uri targetUri;
    private String imageName;
    private ChildEventListener childEventListener;
    private ArrayList<Pair<String, UserAccount>> users;

    private Button createNewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);
        Log.d(TAG, "OnCreate: started");

        // initializing database for saving user info
        FirebaseApp.initializeApp(this);
        firebaseRef = FirebaseDatabase.getInstance();
        mRef = firebaseRef.getReference("Users");

        // making cloud storage for saving image
        storage = FirebaseStorage.getInstance();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }

        // get reference to image for later use
        lilDemoImage = (ImageView) findViewById(R.id.lilImageView);
        defaultImage = getResources().getIdentifier("@drawable/tongue_lick_boi",null, this.getPackageName());
        lilDemoImage.setImageResource(defaultImage);

        defaultUri = Uri.parse("android.resource://com.example.cattinder/drawable/tongue_lick_boi");
        targetUri = defaultUri;
        imageName = "tongue_lick_boi";

        //checkFilePermissions();

        //getting the user list
        users = new ArrayList<Pair<String, UserAccount>>();

        // set up event listener
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                UserAccount a = dataSnapshot.getValue(UserAccount.class);
                Pair temp = new Pair(dataSnapshot.getKey().toString(), a);
                users.add(temp);
                //users.add(dataSnapshot.getValue(UserAccount.class));
                Log.d(TAG, "Child added " + a.getUsername());
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
        };

        // add listener to database
        mRef.addChildEventListener((childEventListener));

    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
                    }
                });
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
            Log.d(TAG, imageName);
            //Bitmap bitmap;
            try {
                lilDemoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                lilDemoImage.setImageBitmap(lilDemoBitmap);
                lilDemoImage.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "The image failed us, whyyyyyyyyyy");
            }
        } else {
            Toast.makeText(NewAccountActivity.this, "You haven't picked an Image", Toast.LENGTH_LONG).show();
            // fails to get image, set default one
            targetUri = defaultUri;
            imageName = "tongue_lick_boi";
            lilDemoImage.setImageResource(defaultImage);
        }

    }

    public void onClickCreateNewAccount(View view) {
        // DONT ALLOW NULL STUFFS
        EditText username = (EditText) findViewById(R.id.enterUsernameText);
        EditText password = (EditText) findViewById(R.id.enterPasswordText);
        EditText bio = (EditText) findViewById(R.id.EditBioTextBox);

        String usernameText = username.getText().toString();
        String passwordText = password.getText().toString();
        String bioText = bio.getText().toString();

        // if everything is not empty, search that they are in the database
        if (usernameText.length() > 0 && passwordText.length() > 0 &&
                bioText.length() > 0) {

            // make sure username not in database
            for (int i = 0; i < users.size(); i++) {
                //{Log.d(TAG, "in for loop with i value " + i + " username " + users.get(i).second.getUsername()
                //      + " password " + users.get(i).second.getPassword());
                //Log.d(TAG, "given username " +  usernameText + " given password " + passwordText);
                if (users.get(i).second.getUsername().equalsIgnoreCase(usernameText)) {
                    Toast.makeText(NewAccountActivity.this, "Username taken", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // set up saving account info on phone
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = pref.edit();

            // create new user acount and assign to database
            String key = mRef.push().getKey(); // generates unique rand key

            // save the account info on phone
            editor.putString("user_key", key);
            editor.commit(); // commit change

            // save photo, upload it to storage
            storageRef = storage.getReference().child("profileImage/" + key + "/" + imageName + ".jpg");
            Log.d(TAG, storageRef.toString());
            storageRef.putFile(targetUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Image successfully uploaded! " + targetUri);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Failed to upload image D:");
                }
            });

            String imageLocation = key + "/" + imageName + ".jpg";

            UserAccount newUser = new UserAccount(key, usernameText, passwordText, bioText, imageLocation);

            mRef.child(key).setValue(newUser);

            Intent intent = new Intent(this, SwipingActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

