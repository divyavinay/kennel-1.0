package com.bignerdranch.android.kennel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Divya on 10/22/2016.
 *
 * Contains Google sign in and Login
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    //Sign in button
    private SignInButton mSignInButton;

    //signing in option
    private GoogleSignInOptions mGoogleSignInOptions;

    //google api client
    private GoogleApiClient mGoogleApiClient;

    //signing in constant to check activity result
    private int RC_Signin = 100;

    //TextViews
    private String mTextViewName;
    private String mTextViewEmail;
    private NetworkImageView mProfileImage;

    //
    private Button mSignin;

    //Shared prefernce file name
    public static final String LOGIN_DETAILS = "loginDetails";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_WIDE);
        mSignInButton.setScopes(mGoogleSignInOptions.getScopeArray());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();

        mSignInButton.setOnClickListener(this);

        mSignin =(Button)findViewById(R.id.newUserButton);
        mSignin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent i =new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_Signin);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == RC_Signin){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            // call sign in function
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result)
    {
        //If the login succed
        if(result.isSuccess()) {
            // getting account details
            GoogleSignInAccount acct = result.getSignInAccount();
            mTextViewName = acct.getDisplayName();
            mTextViewEmail = acct.getEmail();
            SharedPreferences settings =getApplicationContext().getSharedPreferences(LOGIN_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("display_name",mTextViewName);
            editor.putString("display_email",mTextViewEmail);
            editor.commit();

            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();

        }
        else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onClick(View v)
    {
        if(v == mSignInButton)
        {
            signIn();
        }
    }

        public void onLogin(View v) {
            Intent i =new Intent(LoginActivity.this,LoginPageActivity.class);
            startActivity(i);
            finish();

    }

    // this is to close the app when user clicks close
    public void onClickClose(View v)
    {
        finish();
        onDestroy();
        System.exit(0);
    }

    public void onSignup()
    {
        Intent i = new Intent(LoginActivity.this,Register_Activity.class);
        startActivity(i);
        finish();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){}
}

