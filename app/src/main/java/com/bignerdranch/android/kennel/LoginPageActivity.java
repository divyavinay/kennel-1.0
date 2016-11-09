package com.bignerdranch.android.kennel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Divya on 10/22/2016.
 */

public class LoginPageActivity extends AppCompatActivity{
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mLoginButton = (Button) findViewById(R.id.LoginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginPageActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
