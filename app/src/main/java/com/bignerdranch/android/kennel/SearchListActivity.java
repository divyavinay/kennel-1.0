package com.bignerdranch.android.kennel;

import android.support.v4.app.Fragment;

/**
 * Created by Divya on 11/25/2016.
 */

public class SearchListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new SearchListFragment();
    }
}
