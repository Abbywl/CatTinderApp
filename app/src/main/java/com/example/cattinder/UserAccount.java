/*
Authors: Ava Derevlany 51581517 & Abby Liu 15764097
We paired program all aspects of the app!
Ava did the arts
 */

package com.example.cattinder;

import android.widget.ImageView;

public class UserAccount {

    private String id;
    private String username;
    private String password;
    private String bio;
    private int rank;
    private String url;
    //private ImageView picture;

    public UserAccount() {
        id = "N/A";
        username = "N/A";
        password = "N/A";
        bio = "N/A";
        rank = 0;
        url = "N/A";
    }

    public UserAccount(String d, String u, String p, String b, String l) {
        id = d;
        username = u;
        password = p;
        bio = b;
        rank = 0;
        url = l;
        //picture = i;
    }

    public UserAccount(UserAccount account) {
        id = account.id;
        username = account.username;
        password = account.password;
        bio = account.bio;
        rank = account.rank;
        url = account.url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBio() {
        return bio;
    }

    public int getRank() {
        return rank;
    }

    public String getURL() {return url;}

    public void increaseRank() { rank++; }
}
