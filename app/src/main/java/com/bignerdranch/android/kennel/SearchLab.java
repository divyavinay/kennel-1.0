package com.bignerdranch.android.kennel;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Divya on 11/25/2016.
 */

public class SearchLab {

    private static SearchLab mSearchLab;
    private List<Search>mSearches;

    public static SearchLab get(Context context)
    {
       if(mSearchLab == null) {
           mSearchLab = new SearchLab(context);
       }
        return mSearchLab;
    }

    private SearchLab(Context context){
        mSearches = new ArrayList<>();

    }

    public void addSearch(Search search)
    {
        mSearches.add(search);
    }

    public List<Search> getSearches(){
        return mSearches;
    }

    public Search getSearch(UUID id){
        for(Search search:mSearches){
            if(search.getId().equals(id)){
                return search;
            }
        }
        return null;
    }
}
