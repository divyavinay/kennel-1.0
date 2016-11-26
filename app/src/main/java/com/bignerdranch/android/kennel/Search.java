package com.bignerdranch.android.kennel;

import java.util.UUID;

/**
 * Created by Divya on 11/25/2016.
 */

public class Search {

    private UUID mId;
    private String mLastName;
    private String mFirstname;

    public Search(){
        mId = UUID.randomUUID();

    }

    public UUID getId() {
        return mId;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public void setFirstname(String firstname) {
        mFirstname = firstname;
    }
}
