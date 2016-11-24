package com.bignerdranch.android.kennel;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class PetProfileFragment extends Fragment {

    private CheckBox msmall;
    private CheckBox mmedium;
    private CheckBox mlarge;
    private EditText mpetDetails;
    private EditText mpetName;
    private static String JSON_request;
    private Button mSave;
    private String petName;
    private Integer petType;
    private String Details;
    private String mownerName;
    public static final String TAG = "PetProfileFragment";
    private ImageView ivUpload;
    private String photoPath;

    private ImageView mdisplayProfile;

    public static final String LOGIN_DETAILS = "loginDetails";
    private  final int GALLERY_REQUEST=0;
    GalleryPhoto mGalleryPhoto;


    public PetProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v =inflater.inflate(R.layout.fragment_pet_profile, container, false);

       final SharedPreferences settings =getContext().getSharedPreferences(LOGIN_DETAILS, Context.MODE_PRIVATE);

        mSave = (Button)v.findViewById(R.id.SaveProfile);

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mpetName=(EditText)v.findViewById(R.id.PetName);
                mpetDetails=(EditText)v.findViewById(R.id.About);
                msmall=(CheckBox)v.findViewById(R.id.DogSize1);
                mmedium=(CheckBox)v.findViewById(R.id.DogSize2);
                mlarge=(CheckBox)v.findViewById(R.id.DogSize3);
                if(settings.contains("display_name"))
                {
                   mownerName = settings.getString("display_name",null);
                }

               if (msmall.isChecked())
               {
                   petType = 1;
               }
                else if (mmedium.isChecked())
               {
                   petType =2;
               }
                else
               {
                   petType =3;
               }
            }
        });

        ivUpload = (ImageView)v.findViewById(R.id.profileImage);
        mdisplayProfile = (ImageView)v.findViewById(R.id.displayProfile);
         mGalleryPhoto = new GalleryPhoto(getActivity());
        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in = mGalleryPhoto.openGalleryIntent();
                startActivityForResult(in, GALLERY_REQUEST);

            }
        });
     return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Uri uri = data.getData();
        mGalleryPhoto.setPhotoUri(uri);
         photoPath = mGalleryPhoto.getPath();

        if(resultCode == RESULT_OK) {

                mdisplayProfile.setImageURI(uri);
             uploadImageToAWS();
        }
    }

    private void uploadImageToAWS(){


        AsyncTask<String,String,String>_Task = new AsyncTask<String, String, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {

                if(android.os.Debug.isDebuggerConnected())
                    android.os.Debug.waitForDebugger();
                try{
                    java.util.Date expiration = new java.util.Date();
                    long msec = expiration.getTime();
                    msec += 1000 * 60 * 60; // 1 hour.
                    expiration.setTime(msec);
                    publishProgress(params);

                    String BucketName = "HostImage_dp";////////////
                    String keyName = "image_name";
                    String filePath = photoPath;

                    AmazonS3Client s3Client1 = new AmazonS3Client( new BasicAWSCredentials( "AKIAINKUY6FRGRCGVC3Q","HW2y+pJvFrqU23WUgqlEy9radA0Wb9fMagRnDd5r") );
//                    PutObjectRequest por = new PutObjectRequest(existingBucketName,
//                            keyName + ".png",new File(filePath));//key is  URL

                    s3Client1.createBucket(BucketName);

                    PutObjectRequest por = new PutObjectRequest(BucketName,
                           keyName ,new java.io.File(filePath));//key is  URL
                    //making the object Public
                    por.setCannedAcl(CannedAccessControlList.PublicRead);
                    s3Client1.putObject(por);
                }catch(Exception e)
                {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        };
    }
}
