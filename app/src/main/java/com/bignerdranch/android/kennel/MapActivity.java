package com.bignerdranch.android.kennel;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity  {

    GoogleMap mGoogleMap;
    private Button mGo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        if (GoogleServiceAvailable())
        {
            Toast.makeText(this,"Connected to play services",Toast.LENGTH_SHORT).show();
           initMap();
        }
        mGo = (Button)findViewById(R.id.getAddress_btn);
        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    geolocate(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    private void initMap() {
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_disp_frag);
        mapFrag.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
            }
        });
    }

    public boolean GoogleServiceAvailable()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int isavailable = apiAvailability.isGooglePlayServicesAvailable(this);
        if(isavailable == ConnectionResult.SUCCESS)
        {
            return true;
        }
        else if(apiAvailability.isUserResolvableError(isavailable))
        {
            Dialog dialog = apiAvailability.getErrorDialog(this,isavailable,0);
            dialog.show();
        }
        else
        {
            Toast.makeText(this,"Cant connect to play services",Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void goToLocation(double lat,double lng)
    {

        LatLng latLng = new LatLng(lat,lng);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(latLng)
                .build();

        MarkerOptions itemMarker = new MarkerOptions()
                .position(latLng);

        mGoogleMap.clear();
        mGoogleMap.addMarker(itemMarker);

        int margin = getResources().getDimensionPixelSize(R.dimen.map_insert_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds,margin);
        mGoogleMap.moveCamera(update);
    }

    public void geolocate(View view) throws IOException {
        EditText address = (EditText)findViewById(R.id.address);
        String location = address.getText().toString();

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        List<Address> list =geocoder.getFromLocationName(location,1);
        Address add = list.get(0);

       double lat = add.getLatitude();
        double lng = add.getLongitude();
        goToLocation(lat,lng);

    }


}

