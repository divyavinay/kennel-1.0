package com.bignerdranch.android.kennel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.common.api.GoogleApiClient;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener

       {

           //TextViews
           private static TextView mTextViewName;
           private static TextView mTextViewEmail;
           private NetworkImageView mProfileImage;

           public static String  name;
           private static String email;

           //Image Loader
           private ImageLoader mImageLoader;

           //Floating Search button
            private Button mSearchButton;

           public static final String LOGIN_DETAILS = "loginDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              SearchFragment searchFragment = new SearchFragment();
               FragmentManager fm = getSupportFragmentManager();
              fm.beginTransaction().replace(R.id.content_home,searchFragment).commit();
            }
        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        SharedPreferences settings =getApplicationContext().getSharedPreferences(LOGIN_DETAILS, Context.MODE_PRIVATE);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hview = navigationView.getHeaderView(0);

        mTextViewEmail = (TextView)hview.findViewById(R.id.EmailView);
        mTextViewName = (TextView)hview.findViewById(R.id.Name);

        if(settings.contains("display_name"))
        {
           name = settings.getString("display_name","test1");
            email = settings.getString("display_email","test2");
        }
       mTextViewName.setText(name);
        mTextViewEmail.setText(email);
        navigationView.setNavigationItemSelectedListener(this);




       // mProfileImage = (NetworkImageView) findViewById(R.id.profileImage);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Home) {

        } else if (id == R.id.nav_Trips) {
            TripsFragment Tripsfragment = new TripsFragment();
            FragmentManager fm= getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_home,Tripsfragment).commit();

        } else if (id == R.id.nav_PetProfile) {
            PetProfileFragment petProfileFragment = new PetProfileFragment();
            FragmentManager fm =getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_home,petProfileFragment).commit();

        } else if (id == R.id.nav_HostProfile){

            hostDetailsFragment hostDetailsFragment = new hostDetailsFragment();
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_home,hostDetailsFragment).commit();

        }
        else if (id == R.id.log_out) {

            SharedPreferences settings =getApplicationContext().getSharedPreferences(LOGIN_DETAILS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.commit();

            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
