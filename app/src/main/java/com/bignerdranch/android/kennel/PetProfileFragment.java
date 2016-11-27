package com.bignerdranch.android.kennel;

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
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
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
import java.util.Date;

import static android.app.Activity.RESULT_OK;

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
    private ImageView ivUpload;
    private String photoPath;
    private ImageView mdisplayProfile;
    private ProgressDialog pd;
    private GalleryPhoto mGalleryPhoto;
    public static final String LOGIN_DETAILS = "loginDetails";
    public static final String BUCKET_NAME = "kennel-deployments-mobilehub-1555515748/HostImage";
    public static final String ACCESS_KEY = "AKIAINKUY6FRGRCGVC3Q";
    public static final String SECRET_KEY = "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";
    private final int GALLERY_REQUEST = 0;

    public PetProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Uploading");
        View v = inflater.inflate(R.layout.fragment_pet_profile, container, false);
        final SharedPreferences settings = getContext().getSharedPreferences(LOGIN_DETAILS, Context.MODE_PRIVATE);
        mSave = (Button) v.findViewById(R.id.SaveProfile);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mpetName = (EditText) v.findViewById(R.id.PetName);
                mpetDetails = (EditText) v.findViewById(R.id.About);
                msmall = (CheckBox) v.findViewById(R.id.DogSize1);
                mmedium = (CheckBox) v.findViewById(R.id.DogSize2);
                mlarge = (CheckBox) v.findViewById(R.id.DogSize3);
                if (settings.contains("display_name")) {
                    mownerName = settings.getString("display_name", null);
                }

                if (msmall.isChecked()) {
                    petType = 1;
                } else if (mmedium.isChecked()) {
                    petType = 2;
                } else {
                    petType = 3;
                }
            }
        });

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
