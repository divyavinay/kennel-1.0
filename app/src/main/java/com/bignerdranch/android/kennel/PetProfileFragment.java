package com.bignerdranch.android.kennel;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class PetProfileFragment extends Fragment {

    private static String JSON_request;
    private ImageView ivUpload;
    private ImageView mdisplayProfile;
    private ProgressDialog pd;
    private GalleryPhoto mGalleryPhoto;
    public static final String USER_ID = "userId";
    public static final String BUCKET_NAME = "";
    public static final String ACCESS_KEY = "";
    public static final String SECRET_KEY = "";
    private final int GALLERY_REQUEST = 0;
    private IdentityManager identityManager;
    private String ImageURL;
    private EditText mPetName;
    private EditText mDetails;
    private Button mSave;
    private CheckBox mSmall;
    private CheckBox mMedium;
    private CheckBox mLarge;
    private int mpetType;
    private String mpetOwner;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        AWSMobileClient.initializeMobileClientIfNecessary(getActivity());
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        identityManager = awsMobileClient.getIdentityManager();

        // Inflate the layout for this fragment
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Uploading");
        View v = inflater.inflate(R.layout.fragment_pet_profile, container, false);
        final SharedPreferences settings = getContext().getSharedPreferences(USER_ID, Context.MODE_PRIVATE);
       userId= settings.getString("userId",null);

        ivUpload = (ImageView) v.findViewById(R.id.profileImage);


        mdisplayProfile = (ImageView) v.findViewById(R.id.displayProfile);
        mGalleryPhoto = new GalleryPhoto(getActivity());
        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = mGalleryPhoto.openGalleryIntent();
                startActivityForResult(in, GALLERY_REQUEST);
            }
        });

        mPetName = (EditText)v.findViewById(R.id.PetName);
        mDetails =(EditText)v.findViewById(R.id.About);
        mLarge=(CheckBox)v.findViewById(R.id.DogSize3);
        mMedium=(CheckBox)v.findViewById(R.id.DogSize2);
        mSmall = (CheckBox)v.findViewById(R.id.DogSize1);

        if(mSmall.isChecked()==true)
        {
            mpetType=1;
        }
        else if(mMedium.isChecked()==true)
        {
            mpetType = 2;
        }
        else if(mLarge.isChecked()==true)
        {
            mpetType=3;
        }

        mpetOwner = settings.getString("display_name",null);


        mSave=(Button)v.findViewById(R.id.SaveProfile);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSON_request =  " { \"userId\": \"" + userId + "\", " +
                        "\"OwnerName\": \"" + mpetOwner + "\", " +
                        " \"PetName\": \"" + mPetName.getText().toString() + "\",  " +
                        " \"PetSize\": \"" + mpetType + "\",  " +
                        " \"Details\": \"" + mDetails.getText().toString() + "\",  " +
                        "\"ImageURL\": \"" + ImageURL + "\" }";

                invokeFunction(JSON_request);

            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri = data.getData();
        mGalleryPhoto.setPhotoUri(uri);

        pd.show();

        Bitmap thumbnail = (BitmapFactory.decodeFile(mGalleryPhoto.getPath()));
        mdisplayProfile.setImageBitmap(thumbnail);
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

    private void invokeFunction(String JSON_request) {

        final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
        final CharsetEncoder ENCODER = CHARSET_UTF8.newEncoder();
        final CharsetDecoder DECODER = CHARSET_UTF8.newDecoder();

        final String functionName = "insertPetDetails";
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
