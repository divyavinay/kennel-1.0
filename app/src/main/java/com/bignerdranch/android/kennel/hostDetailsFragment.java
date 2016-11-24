package com.bignerdranch.android.kennel;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        View v =inflater.inflate(R.layout.fragment_hostdetails, container, false);

            mAddress = (Button)v.findViewById(R.id.button_address);
        mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getActivity(),MapActivity.class);
                startActivityForResult(intent,2);
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
}
