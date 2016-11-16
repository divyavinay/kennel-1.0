package com.bignerdranch.android.kennel;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageLoader;

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
        String photoPath = mGalleryPhoto.getPath();

        if(resultCode == RESULT_OK) {

                mdisplayProfile.setImageURI(uri);
        }
    }
}
