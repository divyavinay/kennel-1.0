package com.bignerdranch.android.kennel;

import android.content.Intent;
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
    private TextView mTextViewName;
    private TextView mTextViewEmail;
    private NetworkImageView mProfileImage;

    //Image Loader
    private ImageLoader mImageLoader;

    //Textview for close
    private TextView mTextViewClose;

    //TextView for Login
    private TextView mLogin;

    //
    private Button mSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //Initializing views
        mTextViewName = (TextView) findViewById(R.id.textViewName);
        mTextViewEmail = (TextView) findViewById(R.id.textViewEmail);
        mProfileImage = (NetworkImageView) findViewById(R.id.profileImage);

        //Initializing google sign in
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //Initializing signbutton
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

    //setting onclick listener to signing intent
    private void signIn()
    {
        //Creating an intent
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

        //starting intent for result
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

    //after signing in we are calling this function
    private void handleSignInResult(GoogleSignInResult result)
    {
        //If the login succed
        if(result.isSuccess()) {
            // getting account details
            GoogleSignInAccount acct = result.getSignInAccount();

            //display name and email
            mTextViewName.setText(acct.getDisplayName());
            mTextViewEmail.setText(acct.getEmail());

            //Initializing image loader
            mImageLoader = CustomVolleyRequest.getInstance(this.getApplicationContext())
                    .getImageLoader();

            if (acct.getPhotoUrl()!=null) {
                mImageLoader.get(acct.getPhotoUrl().toString(), ImageLoader.getImageListener(mProfileImage,
                        R.mipmap.ic_launcher,
                        R.mipmap.ic_launcher));

                //Loading image
                mProfileImage.setImageUrl(acct.getPhotoUrl().toString(), mImageLoader);
            }

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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){}
}

