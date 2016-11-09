package com.bignerdranch.android.kennel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Divya on 10/22/2016.
 */

public class SignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }
    public void OnNext(View v)
    {
        Intent intent = new Intent(SignUpActivity.this,Register_Activity.class);
        startActivity(intent);
    }
}
