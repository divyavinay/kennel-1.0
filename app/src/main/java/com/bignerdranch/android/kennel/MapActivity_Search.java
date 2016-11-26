package com.bignerdranch.android.kennel;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity_Search extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    GoogleMap mGoogleMap;
    private Button mGo;
    private GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "main-activity";
    private Button mSave;
    private Location mCurrentLocation;
    private double lat;
    private double lng;
    private static final int REQUEST_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);

        if (GoogleServiceAvailable()) {
            Toast.makeText(this, "Connected to play services", Toast.LENGTH_SHORT).show();
            initMap();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mGo = (Button) findViewById(R.id.getAddress_btn_search);
        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle Location_cor = new Bundle();
                Location_cor.putDouble("Latitude",lat);
                Location_cor.putDouble("Longitude",lng);
                Intent intent = new Intent(getApplicationContext(),SearchListActivity.class);
                intent.putExtras(Location_cor);
                startActivity(intent);
                finish();
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

    public boolean GoogleServiceAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int isavailable = apiAvailability.isGooglePlayServicesAvailable(this);
        if (isavailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (apiAvailability.isUserResolvableError(isavailable)) {
            Dialog dialog = apiAvailability.getErrorDialog(this, isavailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void goToLocation(double lat, double lng) {

        LatLng latLng = new LatLng(lat, lng);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(latLng)
                .build();

        MarkerOptions itemMarker = new MarkerOptions()
                .position(latLng);

        mGoogleMap.clear();
        mGoogleMap.addMarker(itemMarker);

        int margin = getResources().getDimensionPixelSize(R.dimen.map_insert_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,14.0f);
        mGoogleMap.moveCamera(update);
    }

    public void geolocate(View view) throws IOException {
        EditText address = (EditText) findViewById(R.id.address);
        String location = address.getText().toString();

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        List<Address> list = geocoder.getFromLocationName(location, 1);

        Address add = list.get(0);

        lat = add.getLatitude();
        lng = add.getLongitude();

        goToLocation(lat, lng);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "Connect", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
            return;
        } else {
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setNumUpdates(1);
            request.setInterval(0);

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
             lat = location.getLatitude();
             lng = location.getLongitude();
            goToLocation(lat,lng);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}


