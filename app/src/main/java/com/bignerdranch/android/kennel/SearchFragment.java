package com.bignerdranch.android.kennel;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;


/**
 * Created by Divya on 10/24/2016.
 */

public class SearchFragment extends android.support.v4.app.ListFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
    {
        return inflater.inflate(R.layout.fragment_search,container,false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);
       //ArrayAdapter adapter= ArrayAdapter.createFromResource(getActivity(),R.array.Search,android.R.layout.simple_list_item_1);
       // setListAdapter(adapter);

    }


}
