package com.bignerdranch.android.kennel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.UUID;

/**
 * Created by Divya on 11/26/2016.
 *
 * form location Search
 */

public class HostDetailsActivity extends AppCompatActivity {

    private TextView mTextView_db_firstname;
    private TextView mTextView_db_lastname;
    private TextView mTextView_db_list_details;
    private String booking_from_date;
    private String booking_to_date;
    public static final String BOOKING_DATES = "booking_dates";
    private TextView mTextView_from_date;
    private TextView mTextView_to_date;
    private String hostUser_Id;
    private String UUID;
    private Button mConfrim_booking;
    private static String JSON_request ;
    private String host_first_name;
    private String host_last_name;
    private String userId;
    public static final String USER_ID = "userId";
    private IdentityManager identityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search);

        AWSMobileClient.initializeMobileClientIfNecessary(this);
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        identityManager = awsMobileClient.getIdentityManager();

        mTextView_db_firstname=(TextView)findViewById(R.id.db_firstname);
        mTextView_db_lastname=(TextView)findViewById(R.id.db_lastname);
        mTextView_db_list_details=(TextView)findViewById(R.id.list_details);
        mTextView_from_date=(TextView)findViewById(R.id.bookin_from_textView);
        mTextView_to_date=(TextView)findViewById(R.id.bookin_to_textView);
        mConfrim_booking=(Button)findViewById(R.id.confirm_btn);

      Bundle host = getIntent().getExtras();
       mTextView_db_firstname.setText(host.get("firstName").toString());
        mTextView_db_lastname.setText(host.get("lastName").toString());
        mTextView_db_list_details.setText(host.get("details").toString());
        hostUser_Id = host.get("host_userId").toString();

        host_first_name =host.get("firstName").toString();
        host_last_name=host.get("lastName").toString();

        SharedPreferences settings =getApplicationContext().getSharedPreferences(BOOKING_DATES, Context.MODE_PRIVATE);
        if(settings.contains("to_date"))
        {
            booking_from_date = settings.getString("from_date",null);
            booking_to_date = settings.getString("to_date",null);

        }

        SharedPreferences settings_login =getApplicationContext().getSharedPreferences(USER_ID, Context.MODE_PRIVATE);
        if(settings_login.contains("userId"))
        {
            userId = settings_login.getString("userId",null);

        }

        mTextView_from_date.setText(booking_from_date);
        mTextView_to_date.setText(booking_to_date);
        generateReservation();



        mConfrim_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSON_request =  " { \"From_date\": \"" + booking_to_date + "\", " +
                        "\"Host_FirstName\": \"" + host_first_name + "\", " +
                        " \"Host_lastName\": \"" + host_last_name + "\",  " +
                        " \"host_userId\": \"" + hostUser_Id + "\",  " +
                        " \"reservationId\": \"" + UUID + "\",  " +
                        " \"To_date\": \"" + booking_to_date + "\",  " +
                        "\"userId\": \"" + userId + "\" }";

                invokeFunction(JSON_request);

                Toast.makeText(getApplicationContext(),"Booking Confirmed",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void invokeFunction(String JSON_request) {

        final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
        final CharsetEncoder ENCODER = CHARSET_UTF8.newEncoder();
        final CharsetDecoder DECODER = CHARSET_UTF8.newDecoder();

        final String functionName = "insertReservation-itemsHandler-mobilehub-1555515748";
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

                    if (statusCode != 200) {
                        showError(invokeResult.getFunctionError());
                    } else {
                        final ByteBuffer resultPayloadBuffer = invokeResult.getPayload();
                        final String resultPayload = DECODER.decode(resultPayloadBuffer).toString();

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
        new AlertDialog.Builder(this)
                .setTitle("Error AWS Backend Contact")
                .setMessage(errorMessage)
                .setNegativeButton("Dissmiss", null)
                .create().show();
    }

    void generateReservation()
    {
        UUID = java.util.UUID.randomUUID().toString();
    }



}
