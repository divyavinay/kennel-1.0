package com.bignerdranch.android.kennel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Divya on 11/26/2016.
 *
 * form location Search
 */

public class HostDetailsActivity extends AppCompatActivity {

    private TextView mTextView_db_firstname;
    private TextView mTextView_db_lastname;
    private TextView mTextView_db_list_details;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        mTextView_db_firstname=(TextView)findViewById(R.id.db_firstname);
        mTextView_db_lastname=(TextView)findViewById(R.id.db_lastname);
        mTextView_db_list_details=(TextView)findViewById(R.id.list_details);

      Bundle host = getIntent().getExtras();
       mTextView_db_firstname.setText(host.get("firstName").toString());
        mTextView_db_lastname.setText(host.get("lastName").toString());
        mTextView_db_list_details.setText(host.get("details").toString());


    }
}
