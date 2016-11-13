package com.bignerdranch.android.kennel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Divya on 10/22/2016.
 */

public class LoginPageActivity extends AppCompatActivity{
    private Button mLoginButton;
    public static final String LOGIN_DETAILS = "loginDetails";
    private TextView mTextPasswod;
    private TextView mTextEmail;
    private static String emial;
    private static String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mTextEmail = (TextView)findViewById(R.id.input_Email);
        mTextPasswod = (TextView)findViewById(R.id.input_password);

        mLoginButton = (Button) findViewById(R.id.LoginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                SharedPreferences settings =getApplicationContext().getSharedPreferences(LOGIN_DETAILS, Context.MODE_PRIVATE);

                if(!settings.contains("email"))
                {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(mTextEmail.getText().toString(), "email");
                    editor.putString(mTextPasswod.getText().toString(), "password");
                    editor.commit();
                    Intent intent = new Intent(LoginPageActivity.this,HomeActivity.class);
                    startActivity(intent);
                }
                else
                {
                    emial= settings.getString("email",null);
                    password = settings.getString("password",null);
                    mTextEmail.setText(emial);
                    mTextPasswod.setText(password);
                }
            }
        });
    }
}
