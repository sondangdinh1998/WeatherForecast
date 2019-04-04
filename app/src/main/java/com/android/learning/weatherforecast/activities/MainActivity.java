package com.android.learning.weatherforecast.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.learning.weatherforecast.R;
import com.android.learning.weatherforecast.models.Weather;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private Weather todayWeather = new Weather();

    private TextView todayTemperature;
    private TextView todayDescription;
    private TextView todayWind;
    private TextView todayPressure;
    private TextView todayHumidity;
    private TextView todaySunrise;
    private TextView todaySunset;
    private TextView lastUpdate;
    private ImageView todayIcon;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View appView;

    private List<Weather> longTermTodayWeahter = new ArrayList<>();
    private List<Weather> longTermTomorrowWeahter = new ArrayList<>();
    private List<Weather> longTermLaterWeahter = new ArrayList<>();

    public String recentCityId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Khởi tạo các textview
        todayTemperature = findViewById(R.id.todayTemperature);
        todayDescription = findViewById(R.id.todayDescription);
        todayWind = findViewById(R.id.todayWind);
        todayPressure = findViewById(R.id.todayPressure);
        todayHumidity = findViewById(R.id.todayHumidity);
        todaySunrise = findViewById(R.id.todaySunrise);
        todaySunset = findViewById(R.id.todaySunset);
        todayIcon = findViewById(R.id.todayIcon);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);

        // Load trước dữ liệu

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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            Log.d("myLog", "action refresh pressed");
            return true;
        } else if (id == R.id.action_search) {
            Log.d("myLog", "action search pressed");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String lastToday = sp.getString("lastToday", "");
    }

    public static long saveLastUpdateTime(SharedPreferences sp) {
        Calendar now = Calendar.getInstance();
        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).commit();
        return now.getTimeInMillis();
    }
}
