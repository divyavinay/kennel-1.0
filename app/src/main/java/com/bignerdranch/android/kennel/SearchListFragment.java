package com.bignerdranch.android.kennel;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;

/**
 * Created by Divya on 11/25/2016.
 */

public class SearchListFragment extends Fragment {

    private RecyclerView mSearchRecyclerView;
    private SearchAdapter mAdapter;
    private static String JSON_request ;
    private IdentityManager identityManager;
    String firstName ;
    String lastName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_list,container,false);

        AWSMobileClient.initializeMobileClientIfNecessary(getActivity());
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        identityManager = awsMobileClient.getIdentityManager();

        mSearchRecyclerView =(RecyclerView) view.findViewById(R.id.search_recycler_view);
        mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        //UpdateUI();

        JSON_request =  " { \"city\": \"" + "Fremont" + "\" }";
        invokeFunction(JSON_request);

        return view;
    }

    private void UpdateUI() {




        SearchLab searchLab =SearchLab.get(getActivity());
        List<Search> searches = searchLab.getSearches();

        mAdapter = new SearchAdapter(searches);
        mSearchRecyclerView.setAdapter(mAdapter);
    }

    private class SearchHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Search mSearch;

        public TextView mFirstName;
        public TextView mLastName;

        public void bindSearch(Search search)
        {
            mSearch =search;
            mFirstName.setText(mSearch.getFirstname());
            mLastName.setText(mSearch.getLastName());

        }

        public SearchHolder (View itemView){
            super(itemView);
            itemView.setOnClickListener(this);

            mFirstName = (TextView) itemView.findViewById(R.id.list_item_firstname);
            mLastName = (TextView)itemView.findViewById(R.id.list_item_lastname);
        }

        @Override
        public void onClick(View v) {
            Intent intent = SearchActivity.newIntent(getActivity(),mSearch.getId());
            startActivity(intent);
        }
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchHolder>{
        private List<Search> mSearches;

        public SearchAdapter (List<Search> searches){
            mSearches = searches;
        }

        @Override
        public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_search,parent,false);
            return new SearchHolder(view);
        }

        @Override
        public void onBindViewHolder(SearchHolder holder, int position) {
            Search search = mSearches.get(position);
          holder.bindSearch(search);
        }



        @Override
        public int getItemCount() {
            return mSearches.size();
        }
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
                            Search search = new Search();
                            SearchLab.get(getActivity()).addSearch(search);
                            UpdateUI();
//                            search.setFirstname(firstName);
//                            search.setLastName(lastName);
                        }

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


