package com.bignerdranch.android.kennel;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.kosalgeek.android.photoutil.GalleryPhoto;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
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
    public static final String BUCKET_NAME = "kennel-deployments-mobilehub-1555515748/HostImage";
    public static final String ACCESS_KEY = "AKIAINKUY6FRGRCGVC3Q";
    public static final String SECRET_KEY = "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";
    private ProgressDialog pd;
    private ImageView IprofileImage_host;
    private ImageView mdisplayProfile_host;
    private GalleryPhoto mGalleryPhoto;
    private final int GALLERY_REQUEST = 0;
    private String ImageURL;
    public static final String USER_ID = "userId";
    private String mCity;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        View v =inflater.inflate(R.layout.fragment_hostdetails, container, false);

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Uploading");

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

        final SharedPreferences settings = getContext().getSharedPreferences(USER_ID, Context.MODE_PRIVATE);
        userId = settings.getString("userId",null);

        if(Exp_dog_yes.isChecked()==true)
            Exp ="yes";
        else
            Exp="No";

        if(OwnPets_Yes.isChecked()==true)
            Own_pets = "Yes";
        else
            Own_pets="No";

        IprofileImage_host = (ImageView) v.findViewById(R.id.profileImage_host);
        mdisplayProfile_host = (ImageView) v.findViewById(R.id.displayProfile_host);
        mGalleryPhoto = new GalleryPhoto(getActivity());
        IprofileImage_host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = mGalleryPhoto.openGalleryIntent();
                startActivityForResult(in, GALLERY_REQUEST);
            }
        });

        mSaveHost = (Button)v.findViewById(R.id.saveHost_btn);
        mSaveHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSON_request =  " { \"ExpDog\": \"" + Exp + "\", " +
                        "\"Longitude\": \"" + mLongitude + "\", " +
                        " \"Latitude\": \"" + mLatitude + "\",  " +
                        " \"userId\": \"" + userId + "\",  " +
                        " \"Owns_pet\": \"" + Own_pets + "\",  " +
                        " \"ImageURL\": \"" + ImageURL + "\",  " +
                        " \"Details\": \"" + details.getText().toString() + "\",  " +
                        " \"city\": \"" + mCity + "\",  " +
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
            mCity =b.getString("city");

        }
        else
        {
            Uri uri = data.getData();
            mGalleryPhoto.setPhotoUri(uri);

            pd.show();

            Bitmap thumbnail = (BitmapFactory.decodeFile(mGalleryPhoto.getPath()));
            mdisplayProfile_host.setImageBitmap(thumbnail);
            pd.show();
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        uploadImageToAWS(mGalleryPhoto.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
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

                        Toast.makeText(getActivity(),"Congratulations! you are now a host!",Toast.LENGTH_LONG).show();

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

    private void uploadImageToAWS(String path) {
        try {
            File file = new File(path);
            AmazonS3Client s3Client = new AmazonS3Client( new BasicAWSCredentials(ACCESS_KEY,SECRET_KEY) );
            PutObjectRequest por = new PutObjectRequest(BUCKET_NAME, file.getName() ,file);
            por.setCannedAcl(CannedAccessControlList.PublicReadWrite);
            s3Client.putObject(por);
            URL url = getUrlForDataBaseInsert(s3Client, file);
            ImageURL = url.toString();
        } catch (Exception e) {
            Log.e("ERROR",e.getMessage());
        }
        pd.dismiss();
    }

    private URL getUrlForDataBaseInsert(AmazonS3Client s3Client1, File file) {
        try {
            ResponseHeaderOverrides override = new ResponseHeaderOverrides();
            override.setContentType("image/jpeg" );
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(BUCKET_NAME, file.getName() );
            urlRequest.setExpiration( new Date( System.currentTimeMillis() + 3600000 ) );
            urlRequest.setResponseHeaders( override );
            URL url = s3Client1.generatePresignedUrl( urlRequest );
            return url;
            //startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( url.toURI().toString() ) ) );
        } catch (Exception ex) {
            Log.e("ERROR",ex.getMessage());
        }
        return null;
    }

}
