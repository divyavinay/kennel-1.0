package com.bignerdranch.android.kennel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.com.google.gson.Gson;
import com.amazonaws.http.HttpClient;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.mobile.util.StringFormatUtils;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.models.LoginResult;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Created by Divya on 10/22/2016.
 */

public class LoginPageActivity extends AppCompatActivity{
    private Button mLoginButton;
    public static final String LOGIN_DETAILS = "loginDetails";
    private TextView mTextPasswod;
    private TextView mTextEmail;
    private static String JSON_request;
    private static String emial;
    private static String password;
    private IdentityManager identityManager;
    public static final String USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        AWSMobileClient.initializeMobileClientIfNecessary(this);
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        identityManager = awsMobileClient.getIdentityManager();

        mTextEmail = (TextView)findViewById(R.id.input_Email);
        mTextPasswod = (TextView)findViewById(R.id.input_password);

        mLoginButton = (Button) findViewById(R.id.LoginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){


                JSON_request =  " { \"userId\": \"" + mTextEmail.getText().toString() + "\",  " +
                        " \"Password\": \"" + mTextPasswod.getText().toString() + "\" }";
                invokeFunction(JSON_request);
            }
        });

     
    }

    private void invokeFunction(String JSON_request) {

        final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
        final CharsetEncoder ENCODER = CHARSET_UTF8.newEncoder();
        final CharsetDecoder DECODER = CHARSET_UTF8.newDecoder();

        final String functionName = "authUser";
        final String requestPayLoad = JSON_request;

        AsyncTask<Void, Void, InvokeResult> myTask = new AsyncTask<Void, Void, InvokeResult>() {
            @Override
            protected InvokeResult doInBackground(Void... params) {
                try {
                    final ByteBuffer payload =
                            ENCODER.encode(CharBuffer.wrap(requestPayLoad));

                    final InvokeRequest invokeRequest =
                            new InvokeRequest()
                                    .withFunctionName(functionName)
                                    .withInvocationType(InvocationType.RequestResponse)
                                    .withPayload(payload);

                    final InvokeResult invokeResult =
                            AWSMobileClient
                                    .defaultMobileClient()
                                    .getCloudFunctionClient()
                                    .invoke(invokeRequest);

                    return invokeResult;
                } catch (final Exception e) {
                    Log.e("AWSLAMBDA:", "AWS Lambda invocation failed : " + e.getMessage(), e);
                    final InvokeResult result = new InvokeResult();
                    result.setStatusCode(500);
                    result.setFunctionError(e.getMessage());
                    return result;
                }
            }

            @Override
            protected void onPostExecute(final InvokeResult invokeResult) {
                try {
                    final int statusCode = invokeResult.getStatusCode();
                    final String functionError = invokeResult.getFunctionError();
                    final String logResult = invokeResult.getLogResult();

                    String verify;


                    if (statusCode != 200) {
                        showError(invokeResult.getFunctionError());
                    } else {
                        final ByteBuffer resultPayloadBuffer = invokeResult.getPayload();
                        final String resultPayload = DECODER.decode(resultPayloadBuffer).toString();

                        JSONObject reader = new JSONObject(resultPayload);

                        JSONObject body = reader.getJSONObject("body");
                        String userId = body.getString("userId");
                        verify = body.getString("verificationPassed");



                        if(verify.equals("Verified"))
                        {

                            SharedPreferences settings =getApplicationContext().getSharedPreferences(USER_ID, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor= settings.edit();
                            editor.putString("userId",userId);
                            editor.commit();
                            Intent intent = new Intent(LoginPageActivity.this,HomeActivity.class);
                           startActivity(intent);
                            finish();
                        }
                        else
                            Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_SHORT).show();

                    }

                    if (functionError != null) {
                        Log.e("AWSLAMBDA", "AWS Lambda Function Error: " + functionError);
                    }

                    if (logResult != null) {
                        Log.d("AWSLAMBDA", "AWS Lambda Log Result: " + logResult);
                    }
                } catch (final Exception e) {
                    Log.e("AWSLAMBDA", "Unable to decode results. " + e.getMessage(), e);
                    showError(e.getMessage());
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            myTask.execute();
    }

    public void showError(final String errorMessage) {
        new AlertDialog.Builder(getApplication())
                .setTitle("Error AWS Backend Contact")
                .setMessage(errorMessage)
                .setNegativeButton("Dissmiss", null)
                .create().show();
    }

}
