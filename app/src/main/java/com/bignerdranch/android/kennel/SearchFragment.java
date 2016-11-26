package com.bignerdranch.android.kennel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.UUID;

/**
 * Created by Divya on 11/25/2016.
 */

public class SearchFragment extends Fragment {

    private Search mSearch;
    private TextView mFirstName;
    private TextView mLastName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID searchId =(UUID) getActivity().getIntent().getSerializableExtra(SearchActivity.EXTRA_SEARCH_ID);
        mSearch = SearchLab.get(getActivity()).getSearch(searchId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.fragment_search,container,false);

//        mFirstName.setText(mSearch.getFirstname());
//        mLastName.setText(mSearch.getLastName());
        return v;
    }
}
