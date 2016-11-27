package com.bignerdranch.android.kennel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Divya on 11/25/2016.
 */

public class SearchListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{

    private RecyclerView mSearchRecyclerView;
    private static String JSON_request ;
    private IdentityManager identityManager;
    String firstName ;
    String lastName;
    String Details;

    private static final String TAG_ID = "id";
    private static final String TAG_FIRSTNAME = "firstName";
    private static final String TAG_LASTNAME="lastName";
    private static final String TAG_DETAILS="details";
    private static final String ID_EXTRA="com.bignerdranch.android.kennel._ID";

    private GoogleApiClient mGoogleApiClient;
    GoogleMap mGoogleMap;
    private static final int REQUEST_LOCATION = 0;
    private double lat;
    private double lng;
    private String city;


    ListView mListView;
    ArrayList<HashMap<String, String>> personList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search_list);

        AWSMobileClient.initializeMobileClientIfNecessary(this);
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        identityManager = awsMobileClient.getIdentityManager();

        personList = new ArrayList<HashMap<String,String>>();
        mListView = (ListView) findViewById(R.id.listView);

        city = getIntent().getStringExtra(MapActivity_Search.CITY_EXTRA);

        JSON_request =  " { \"city\": \"" + city + "\" }";
        invokeFunction(JSON_request);


        if (GoogleServiceAvailable()) {
            Toast.makeText(this, "Connected to play services", Toast.LENGTH_SHORT).show();
            initMap();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    private void initMap() {

        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_disp_location_frag);
        mapFrag.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;

            }
        });

    }



    private void invokeFunction(String JSON_request) {

        final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
        final CharsetEncoder ENCODER = CHARSET_UTF8.newEncoder();
        final CharsetDecoder DECODER = CHARSET_UTF8.newDecoder();

        final String functionName = "getSearchLocations-itemsHandler-mobilehub-1555515748";
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

                        JSONObject reader = new JSONObject(resultPayload);
                        String body = reader.getString("body");
                        JSONObject bodyObj= new JSONObject(body);
                        JSONObject hosts = bodyObj.getJSONObject("hosts");
                        JSONArray items = hosts.getJSONArray("Items");

                        for(int i=0;i<items.length();i++)
                        {
                            JSONObject jsonObject = items.getJSONObject(i);
                            firstName = jsonObject.getString("First_Name");
                            lastName = jsonObject.getString("Last_Name");
                            Details = jsonObject.getString("Details");

                            lat = jsonObject.getDouble("Latitude");
                            lng = jsonObject.getDouble("Longitude");
                            goToLocation(lat,lng);

                            HashMap<String,String> persons = new HashMap<String,String>();

                            persons.put(TAG_FIRSTNAME,firstName);
                            persons.put(TAG_LASTNAME,lastName);
                            persons.put(TAG_DETAILS,Details);

                            personList.add(persons);
                        }
                        ListAdapter adapter = new SimpleAdapter(
                                getApplicationContext(), personList, R.layout.list_item_search,
                                new String[] {TAG_FIRSTNAME,TAG_LASTNAME,TAG_DETAILS},
                                new int[]{ R.id.list_item_firstname, R.id.list_item_lastname,R.id.list_details}
                        );

                     mListView.setAdapter(adapter);
                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Bundle bundle = new Bundle();
                                Object o = parent.getAdapter().getItem(position);
                                HashMap<String, Object> obj = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                                String firstName_obj = (String) obj.get("firstName");
                                String Lastname_obj = (String) obj.get("lastName");
                               String Details_obj = (String) obj.get("details");

                                bundle.putString("firstName",firstName_obj);
                                bundle.putString("lastName",Lastname_obj);
                                bundle.putString("details",Details_obj);

                                Intent i = new Intent(SearchListActivity.this,HostDetailsActivity.class);
                                i.putExtras(bundle);
                                startActivity(i);
                            }
                        });
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
        new AlertDialog.Builder(this)
                .setTitle("Error AWS Backend Contact")
                .setMessage(errorMessage)
                .setNegativeButton("Dissmiss", null)
                .create().show();
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

        //mGoogleMap.clear();
        mGoogleMap.addMarker(itemMarker);

        int margin = getResources().getDimensionPixelSize(R.dimen.map_insert_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,14.0f);
        mGoogleMap.moveCamera(update);
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
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

            LatLng latLng_current = new LatLng(lat,lng);

            mGoogleMap.setMyLocationEnabled(true);
           // goToLocation(lat,lng);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}

