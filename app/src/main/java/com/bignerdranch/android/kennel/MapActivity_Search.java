package com.bignerdranch.android.kennel;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapActivity_Search extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    GoogleMap mGoogleMap;
    private Button mGo;
    private GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "main-activity";
    private Button mFrom_date;
    private Button mTo_date;
    private Location mCurrentLocation;
    private double lat;
    private double lng;
    private static final int REQUEST_LOCATION = 0;
    String city;
    public static final String CITY_EXTRA="com.bignerdranch.android.kennel.city";
    private int REQUEST_FROM_DATE=0;
    private int REQUEST_TO_DATE= 1;
    private String fromDate;
    private String toDate;
    private DatePicker datePicker;
    private Calendar calendar;
    private int year_x, month_x, day_x;
    private static final int DIALOG_ID_FROM = 0;
    private static final int DIALOG_ID_TO=1;
    public static final String BOOKING_DATES = "booking_dates";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);

        if (GoogleServiceAvailable()) {
            initMap();
        }

        Calendar c =Calendar.getInstance();
        year_x = c.get(Calendar.YEAR);
        month_x = c.get(Calendar.MONTH) + 1;
        day_x=c.get(Calendar.DAY_OF_MONTH);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mGo = (Button) findViewById(R.id.getAddress_btn_search);
        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    geolocate(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(),SearchListActivity.class);
                intent.putExtra(CITY_EXTRA,city);
                startActivity(intent);
                finish();
            }
        });

        mFrom_date=(Button)findViewById(R.id.rev_fromDate);
        mFrom_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialogOnButtonClick_from_date();

            }
        });



        mTo_date=(Button)findViewById(R.id.rev_ToDate);
        mTo_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialogOnButtonClick_to();

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
        city = add.getLocality();

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

    public void showDialogOnButtonClick_from_date()
    {
        showDialog(DIALOG_ID_FROM);
    }

    public void showDialogOnButtonClick_to()
    {
        showDialog(DIALOG_ID_TO);
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        if(id == DIALOG_ID_FROM)
           return new DatePickerDialog(this,dpickerListner,year_x,month_x,day_x);

        else if (id == DIALOG_ID_TO)
            return new DatePickerDialog(this,dpickerListner_to,year_x,month_x,day_x);
        else
            return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListner
            = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    year_x = year;
                    month_x = month;
                    day_x = dayOfMonth;
            fromDate = day_x +"/" + month_x +"/" + year_x;
            SharedPreferences settings =getApplicationContext().getSharedPreferences(BOOKING_DATES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor= settings.edit();
            editor.putString("from_date",fromDate);
            editor.commit();

        }
    };

    private DatePickerDialog.OnDateSetListener dpickerListner_to
            = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            year_x = year;
            month_x = month;
            day_x = dayOfMonth;
            toDate = day_x +"/"+month_x+"/"+year_x;
            SharedPreferences settings =getApplicationContext().getSharedPreferences(BOOKING_DATES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor= settings.edit();
            editor.putString("to_date",toDate);
            editor.commit();
        }
    };
}


