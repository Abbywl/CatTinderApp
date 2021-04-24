/*
Authors: Ava Derevlany 51581517 & Abby Liu 15764097
We paired program all aspects of the app!
Ava did the arts
 */

package com.example.cattinder;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.ArrayList;

public class SwipingActivity extends AppCompatActivity {

    private ImageView catImage;
    private TextView usernameText;
    private TextView textBio;

    private int loadingImage;

    private int catCounter = 0;

    private FirebaseDatabase database;
    private DatabaseReference mRef;

    // storage reference
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // event listener
    private ChildEventListener childEventListener;
    // contains all database elements
    private ArrayList<Pair<String, UserAccount>> users;
    private ArrayList<Image> userImages;

    private static final String TAG = "SwippingActivity";

    private boolean firstElement = true;

    private ImageButton hissButton;
    private ImageButton purrButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swiping);

        // set up hiss button
        hissButton = (ImageButton) findViewById(R.id.hissButton);
        int imageX = getResources().getIdentifier("@drawable/hiss_button",null, this.getPackageName());
        hissButton.setImageResource(imageX );

        // set up purr button
        purrButton = (ImageButton) findViewById(R.id.purrButton);
        int imagePaw = getResources().getIdentifier("@drawable/paw_button2",null, this.getPackageName());
        purrButton.setImageResource(imagePaw );

        // set up filler cat image
        catImage = (ImageView) findViewById(R.id.CatImage);
        int imageResource = getResources().getIdentifier("@drawable/tongue_lick_boi",null, this.getPackageName());
        catImage.setImageResource(imageResource );

        usernameText = (TextView) findViewById(R.id.UsernameTextView);

        textBio = (TextView) findViewById(R.id.catBioText);

        loadingImage = getResources().getIdentifier("@drawable/loading_image",null, this.getPackageName());


        // connect to firebase
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference("Users");

        // making cloud storage for saving image
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("profileImage");

        // initialize arrayList
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

                if (firstElement) {
                    usernameText.setText(users.get(catCounter).second.getUsername().toString());
                    textBio.setText(users.get(catCounter).second.getBio().toString());

                    String url = users.get(catCounter).second.getURL();
                    loadImage(url);

                    firstElement = false;

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // if key matches, change the saved data in the array
                Log.d(TAG, "CHild changed, time to update");
                for (int i = 0; i < users.size(); ++i) {
                    if (users.get(i).first.equals(dataSnapshot.getKey().toString())) {
                        UserAccount a = dataSnapshot.getValue(UserAccount.class);
                        users.remove(i);
                        Pair temp = new Pair(dataSnapshot.getKey().toString(), a);
                        users.add(i, temp);

                        //Log.d(TAG, "editing: " + users.get(i).second.getUsername());
                        //Log.d(TAG, "existing: " + usernameText.getText().toString());

                        if (usernameText.getText().toString().equals(users.get(i).second.getUsername())) {
                            textBio.setText(users.get(i).second.getBio());
                        }
                    }
                }
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

        //usernameText.setText("Salutaions");

        // add listener to database
        mRef.addChildEventListener((childEventListener));

    }

    private void loadImage(String url) {
        //download photo to change using glide, recycler view and card view
        // get the image name
        Log.d(TAG, url);

        storageRef.child(url).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Log.d(TAG, "downloaded uri: " + uri);
                Picasso.with(SwipingActivity.this).load(uri).placeholder(loadingImage)
                        .error(loadingImage).fit().into(catImage);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "things went wrong downloading a users uri...");
            }
        });
    }

    public void onClickPurr(View view) {

        if(catCounter < users.size()-1) {
            users.get(catCounter).second.increaseRank();
            String key = users.get(catCounter).first;
            mRef.child(key).child("rank").setValue(users.get(catCounter).second.getRank());
            catCounter++;

            usernameText.setText(users.get(catCounter).second.getUsername().toString());
            textBio.setText(users.get(catCounter).second.getBio().toString());

            String url = users.get(catCounter).second.getURL();
            loadImage(url);

        }
        else {
            Toast.makeText(SwipingActivity.this,"No more cats to judge D:", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickHiss(View view) {

        if(catCounter < users.size()-1) {
            catCounter++;
            usernameText.setText(users.get(catCounter).second.getUsername().toString());
            textBio.setText(users.get(catCounter).second.getBio().toString());

            String url = users.get(catCounter).second.getURL();
            loadImage(url);

        }
        else {
            Toast.makeText(SwipingActivity.this,"No more cats to judge D:", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickHAMBURGEReditMenu(View view) {
        Intent intent = new Intent(this, EditAccountActivity.class);
        startActivity(intent);
    }
}
