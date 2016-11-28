package com.bignerdranch.android.kennel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Divya on 11/27/2016.
 */

public class Hosts_list extends Fragment {

    private ListView mListView_hosts;
    private IdentityManager identityManager;
    ArrayList<HashMap<String, String>> personList;
    private String JSON_request;
    String firstName ;
    String lastName;
    String Details;
    private String host_userId;
    private static final String TAG_ID = "id";
    private static final String TAG_FIRSTNAME = "firstName";
    private static final String TAG_LASTNAME="lastName";
    private static final String TAG_DETAILS="details";
    private static final String TAG_HOST_USERID="host_userId";
    private static final String TAG_ImageURL="ImageURL";
    private ImageView Host_Image;
    private String ImageUrl;
    private Bitmap bitmap;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //mListView = (ListView) findViewById(R.id.host_list_view);
        View v = inflater.inflate(R.layout.home_hosts, container, false);

        mListView_hosts=(ListView)v.findViewById(R.id.listView_hosts);
        Host_Image=(ImageView)v.findViewById(R.id.host_image);

        AWSMobileClient.initializeMobileClientIfNecessary(getActivity());
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        identityManager = awsMobileClient.getIdentityManager();

        personList = new ArrayList<HashMap<String, String>>();

        JSON_request = " { \"city\": \"" + "Fremont" + "\" }";
        invokeFunction(JSON_request);


        return v;
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
                        JSONObject bodyObj = new JSONObject(body);
                        JSONObject hosts = bodyObj.getJSONObject("hosts");
                        JSONArray items = hosts.getJSONArray("Items");


                        for (int i = 0; i < items.length(); i++) {
                            JSONObject jsonObject = items.getJSONObject(i);
                            firstName = jsonObject.getString("First_Name");
                            lastName = jsonObject.getString("Last_Name");
                            Details = jsonObject.getString("Details");
                            host_userId = jsonObject.getString("userId");
                            //ImageUrl = jsonObject.getString("ImageURL");
                            //bitmap = getBitmapFromURL(ImageUrl);


                            HashMap<String, String> persons = new HashMap<String, String>();

                            persons.put(TAG_FIRSTNAME, firstName);
                            persons.put(TAG_LASTNAME, lastName);
                            persons.put(TAG_DETAILS, Details);
                            persons.put(TAG_HOST_USERID, host_userId);


                            personList.add(persons);
                        }
                        ListAdapter adapter = new SimpleAdapter(
                                getActivity(), personList, R.layout.home_host_list_view,
                                new String[]{TAG_FIRSTNAME, TAG_LASTNAME, TAG_DETAILS, TAG_HOST_USERID},
                                new int[]{R.id.list_item_firstname, R.id.list_item_lastname, R.id.list_details, R.id.host_userid}
                        );

                        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder(){

                            @Override
                            public boolean setViewValue(View view, Object data, String textRepresentation) {

                                if (view.getId() == R.id.list_item_firstname) {
                                    ((TextView) view).setText((String) data);
                                    return true;
                                } else if (view.getId() == R.id.list_item_lastname) {
                                    ((TextView) view).setText((String) data);
                                } else if (view.getId() == R.id.list_item_lastname) {
                                    ((TextView) view).setText((String) data);
                                } else if (view.getId() == R.id.host_image) {
                                   // ((ImageView) view).setImageBitmap(bitmap);
                                }
                                return false;
                            }
                        };
                        ((SimpleAdapter) adapter).setViewBinder(viewBinder);
                        mListView_hosts.setAdapter(adapter);
                        mListView_hosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Bundle bundle = new Bundle();
                                Object o = parent.getAdapter().getItem(position);
                                HashMap<String, Object> obj = (HashMap<String, Object>) parent.getAdapter().getItem(position);
                                String firstName_obj = (String) obj.get("firstName");
                                String Lastname_obj = (String) obj.get("lastName");
                                String Details_obj = (String) obj.get("details");
                                String host_userID_obj=(String)obj.get("host_userId");

                                bundle.putString("firstName",firstName_obj);
                                bundle.putString("lastName",Lastname_obj);
                                bundle.putString("details",Details_obj);
                                bundle.putString("host_userId",host_userID_obj);

                                Intent i = new Intent(getActivity(),HostDetailsActivity.class);
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
        new AlertDialog.Builder(getActivity())
                .setTitle("Error AWS Backend Contact")
                .setMessage(errorMessage)
                .setNegativeButton("Dissmiss", null)
                .create().show();
    }

    public static Bitmap getBitmapFromURL(String src) {
        ResponseHeaderOverrides override = new ResponseHeaderOverrides();
        override.setContentType( "image/jpeg" );

        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}
