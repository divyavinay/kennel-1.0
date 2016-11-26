package com.bignerdranch.android.kennel;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;

import java.util.UUID;


/**
 * Created by Divya on 10/24/2016.
 */

public class SearchActivity extends SingleFragmentActivity {

    public static final String EXTRA_SEARCH_ID= "com.bignerdranch.android.kennel.search_id";

    public static Intent newIntent(Context packageContext, UUID searchId){
        Intent intent = new Intent(packageContext,SearchActivity.class);
        intent.putExtra(EXTRA_SEARCH_ID,searchId);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new SearchFragment();
    }
}
