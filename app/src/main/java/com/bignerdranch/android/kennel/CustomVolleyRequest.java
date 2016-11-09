package com.bignerdranch.android.kennel;

/**
 * Created by Divya on 10/22/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;

public class CustomVolleyRequest
{

    private static CustomVolleyRequest sCustomVolleyRequest;
    private static Context sContext;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private CustomVolleyRequest(Context context)
    {
        this.sContext = context;
        this.mRequestQueue =getRequestQueue();

        mImageLoader =new ImageLoader(mRequestQueue,new ImageLoader.ImageCache()
        {
            private final LruCache<String,Bitmap>
                    cache=new LruCache<String,Bitmap>(20);

            @Override
            public Bitmap getBitmap(String url)
            {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url,Bitmap bitmap) {
                cache.put(url,bitmap);
            }
        });
    }
    public static synchronized CustomVolleyRequest getInstance(Context context)
    {
        if (sCustomVolleyRequest == null)
        {
            sCustomVolleyRequest = new CustomVolleyRequest(context);
        }
        return sCustomVolleyRequest;
    }

    public RequestQueue getRequestQueue()
    {
        if(mRequestQueue == null)
        {
            Cache cache = new DiskBasedCache(sContext.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            mRequestQueue= new RequestQueue(cache, network);
            mRequestQueue.start();
        }
        return  mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
