package com.bignerdranch.android.kennel;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Divya on 11/14/2016.
 */

public class hostDetailsFragment extends Fragment {

    private Button mAddress;
    private Double mLatitude;
    private Double mLongitude;
    private Button mSaveHost;
    private EditText mFirstname;
    private EditText mLastname;
    private IdentityManager identityManager;
    private static String JSON_request ;
    private CheckBox Exp_dog_yes;
    private CheckBox OwnPets_Yes;
    private String Own_pets;
    private EditText details;
    private String Exp;
    private String userId;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        View v =inflater.inflate(R.layout.fragment_hostdetails, container, false);

        AWSMobileClient.initializeMobileClientIfNecessary(getActivity());
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        identityManager = awsMobileClient.getIdentityManager();

            mAddress = (Button)v.findViewById(R.id.button_address);
        mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getActivity(),MapActivity.class);
                startActivityForResult(intent,2);
            }
        });

        mFirstname=(EditText)v.findViewById(R.id.Name);
        mLastname=(EditText)v.findViewById(R.id.lastName);
        Exp_dog_yes=(CheckBox)v.findViewById(R.id.ExpDog_yes);
        OwnPets_Yes=(CheckBox)v.findViewById(R.id.OwnPets_No);
        details =(EditText)v.findViewById(R.id.details);
        userId="test1";

        if(Exp_dog_yes.isChecked()==true)
            Exp ="yes";
        else
            Exp="No";

        if(OwnPets_Yes.isChecked()==true)
            Own_pets = "Yes";
        else
            Own_pets="No";


        mSaveHost = (Button)v.findViewById(R.id.saveHost_btn);
        mSaveHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            JSON_request =  " { \"ExpDog\": \"" + Exp + "\", " +
                        "\"Longitude\": \"" + mLongitude + "\", " +
                        " \"Latitude\": \"" + mLatitude + "\",  " +
                         " \"userId\": \"" + userId + "\",  " +
                         " \"Owns_pet\": \"" + Own_pets + "\",  " +
                        " \"Details\": \"" + details.getText().toString() + "\",  " +
                        " \"First_Name\": \"" + mFirstname.getText().toString() + "\", " +
                        "\"Last_Name\": \"" + mLastname.getText().toString() + "\" }";

                invokeFunction(JSON_request);

            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==2)
        {
                Bundle b= data.getExtras();
                mLatitude = b.getDouble("Lat");
                mLongitude = b.getDouble("Lng");
        }
    }

    private void invokeFunction(String JSON_request) {

        final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
        final CharsetEncoder ENCODER = CHARSET_UTF8.newEncoder();
        final CharsetDecoder DECODER = CHARSET_UTF8.newDecoder();

        final String functionName = "HostDetails-insertHosts-mobilehub-1555515748";
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
        new AlertDialog.Builder(getActivity())
                .setTitle("Error AWS Backend Contact")
                .setMessage(errorMessage)
                .setNegativeButton("Dissmiss", null)
                .create().show();
    }
}
