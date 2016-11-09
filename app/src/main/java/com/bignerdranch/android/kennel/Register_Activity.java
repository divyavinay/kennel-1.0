package com.bignerdranch.android.kennel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Divya on 10/23/2016.
 */

public class Register_Activity extends AppCompatActivity{

    private Button mSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

    }
    public void onLogin(View v)
    {
        Intent i = new Intent(Register_Activity.this,HomeActivity.class);
        startActivity(i);
        finish();
    }
}
