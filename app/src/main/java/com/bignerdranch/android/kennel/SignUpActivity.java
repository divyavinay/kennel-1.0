package com.bignerdranch.android.kennel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by Divya on 10/22/2016.
 */

public class SignUpActivity extends AppCompatActivity {

    private TextView mfirst_name;
    private TextView mlast_name;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_sign_up);

    }

    public void onNext(View v)
    {
        mfirst_name = (TextView)findViewById(R.id.input_Firstname);
        mlast_name =(TextView)findViewById(R.id.input_LastName);
        Bundle details = new Bundle();
        details.putString("first_name",mfirst_name.getText().toString());
        details.putString("last_name",mlast_name.getText().toString());
        Intent i= Register_Activity.newIntent(SignUpActivity.this,details);
        startActivity(i);



    }
}
