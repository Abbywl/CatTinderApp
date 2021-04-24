/*
Authors: Ava Derevlany 51581517 & Abby Liu 15764097
We paired program all aspects of the app!
Ava did the arts
 */

package com.example.cattinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private SharedPreferences pref;
    private ChildEventListener childEventListener;
    private ArrayList<Pair<String, UserAccount>> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "OnCreate: started");
        ImageView catLogo = (ImageView) findViewById(R.id.catLogo);
        int imageResource = getResources().getIdentifier("@drawable/cat_tinder_bg",null, this.getPackageName());
        catLogo.setImageResource(imageResource );

        // connect to firebase
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference("Users");

        // if there is a persistent value, auto log in and skip this page
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        if (pref.contains("user_key")) {
            Intent intent = new Intent(this, SwipingActivity.class);
            startActivity(intent);
        }

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
    public void onClickLogin(View view)
    {
        Intent intent = new Intent(this, SwipingActivity.class);
        EditText username =  (EditText) findViewById(R.id.UsernameTextbox);
        EditText password =  (EditText) findViewById(R.id.PasswordTextbox);
        String usernameText = username.getText().toString();
        String passwordText = password.getText().toString();

        // if the login and password are not empty, search that they are in the database
        if (usernameText.length() > 0 && passwordText.length() > 0) {

            // if the username and password are in the database, set persistent key and log in
            for(int i = 0; i< users.size(); i++)
            {Log.d(TAG, "in for loop with i value " + i + " username " + users.get(i).second.getUsername()
                + " password " + users.get(i).second.getPassword());
                Log.d(TAG, "given username " +  usernameText + " given password " + passwordText);
                if( users.get(i).second.getUsername().equalsIgnoreCase(usernameText)
                        && users.get(i).second.getPassword().equals(passwordText ))
                {

                    // set up saving account info on phone
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    // save the account info on phone
                    editor.putString("user_key", users.get(i).first);
                    editor.commit(); // commit change
                    Log.d(TAG, "Logged in as: " + usernameText);
                    startActivity(intent);
                    return;
                }
            }

        }
        Toast.makeText(MainActivity.this,"Username or Password incorrect", Toast.LENGTH_LONG).show();

    }

    public void onClickCreateAccount(View view) {
        Intent intent = new Intent(this, NewAccountActivity.class);
        startActivity(intent);
    }
}
