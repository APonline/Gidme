package ca.gidme.gidme;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        Pages.OnFragmentInteractionListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        com.google.android.gms.location.LocationListener {


    public static final String FRAGMENT_PDF_RENDERER_BASIC = "pdf_renderer_basic";

    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final String[] WRITE = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String[] READ = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;
    public static final String EXTRA_MESSAGE_title = "";

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //MainActivity vars
    public Menu menuSide;
    public Menu searchMenu;
    public ActionBarDrawerToggle mDrawerToggle;
    public GoogleMap mMap;
    public String ActivePage = "";
    public static String ActiveVenue = "";
    public static String ActiveId = "";
    public String venueInfoViewing = "";
    public String venueIDViewing = "";
    public static String targetLayout = "";
    public String indoorVen = "";
    public static final String WEB_URI = "";
    public String venueFilter = "";

    private ArrayList<HashMap<String, String>> venueList;
    private ArrayList<HashMap<String, String>> venuesList;
    private ArrayList<HashMap<String, String>> mapLevels;

    //venuesListLatLong is used for fixing the zoom size
    private ArrayList<HashMap<String, String>> venuesListLatLong;

    private ArrayList<HashMap<String, String>> venuesListInfo;
    private ArrayList<HashMap<String, String>> levelListInfo;
    private String TAG = MainActivity.class.getSimpleName();
    public String theVen = "";
    public String theLevel = "";
    public Bitmap theMap = null;
    public String theVenId = "";
    public String storeHours = "";
    public int activeMapLvl = 0;
    private ListView lv;

    final Context dialogContext = this;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Google api connect
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            Log.i("Api is", "" + mGoogleApiClient);
        }
        //End Google api connect

        venueList = new ArrayList<>();
        venuesList = new ArrayList<>();
        venuesListLatLong = new ArrayList<>();
        venuesListInfo = new ArrayList<>();
        levelListInfo = new ArrayList<>();
        mapLevels = new ArrayList<>();
        int activeMapLvl = 0;

        ActivePage = "Venues";
        ActiveVenue = "";
        venueFilter = "";

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ImageView backBtn = (ImageView) findViewById(R.id.venueBack);
        backBtn.setVisibility(View.GONE);

        //Toolbar setup
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        TextView textView = (TextView) findViewById(R.id.toolbarTitle);
        textView.setText(ActivePage);

        //Hamburger Menu Btn
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        //navigation
        //nav items
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment MapView = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        MapView.getMapAsync(this);


    }

    /*TOOLBAR*/
    public void toolbarSetup(String currtoolbar) throws InterruptedException {

        if (Objects.equals(currtoolbar, "normal")) {
            Log.d("A", "normal");
            normalToolbar();
        } else if (Objects.equals(currtoolbar, "blue")) {
            Log.d("B", "blue");
            blueToolbar();
        } else {
            Log.d("C", "default");
            normalToolbar();
        }
    }

    public void normalToolbar() {
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.darkGray));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        //Textbox
        Log.d("PG", ActivePage);
        TextView textView = (TextView) findViewById(R.id.toolbarTitle);
        textView.setText(ActivePage);

        Resources res = getResources();
        int color = res.getColor(R.color.darkGray);

        textView.setTextColor(color);

        //restore drawer
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();

        //back button / hamburger
        ImageView burgerBtn = (ImageView) findViewById(R.id.venueBack);
        burgerBtn.setColorFilter(R.color.lightGray, PorterDuff.Mode.SRC_ATOP);
        burgerBtn.setVisibility(View.GONE);

        //search icon
        if(!ActivePage.equals("Settings")) {
            MenuItem item = searchMenu.findItem(R.id.action_settings);
            item.setVisible(true);
        }else {
            MenuItem mItem = searchMenu.findItem(R.id.action_settings);
            mItem.setVisible(false);
        }
    }

    public void blueToolbar() {
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkBlue)));

        //Textbox
        TextView textView = (TextView) findViewById(R.id.toolbarTitle);
        textView.setText("Find Venues");
        textView.setTextColor(Color.WHITE);

        //restore drawer
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.syncState();

        //back button / hamburger
        ImageView backBtn = (ImageView) findViewById(R.id.venueBack);
        backBtn.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        backBtn.setVisibility(View.VISIBLE);

        //search icon
        MenuItem item = menuSide.findItem(R.id.nav_venue);
        item.setVisible(false);
    }
    /*TOOLBAR*/

    /*MAIN NAVIGATION*/
    public static String getTargetLayout() {
        return targetLayout;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        searchMenu = menu;

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menus = navigationView.getMenu();

        menuSide = menus;

        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        String vId = preferences.getString("venueId", "");
        String vName = preferences.getString("venueName", "");
        venueIDViewing = vId;
        venueInfoViewing = vName;
        setNewVenue(new View(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        //Change top title
        int id = item.getItemId();

        //Change fragments
        Fragment fragment = null;
        Class fragmentClass = null;


        FloatingActionButton fablocation = (FloatingActionButton) findViewById(R.id.fabMyLocation);
        fablocation.hide();

        fragmentClass = Pages.class;

        //reset filter
        ConstraintLayout subnavMenu = (ConstraintLayout) findViewById(R.id.subnavMenu);
        subnavMenu.setMaxHeight(0);
        ConstraintLayout subnavMenuLevel = (ConstraintLayout) findViewById(R.id.subnavMenuLevel);
        subnavMenuLevel.setMaxHeight(0);
        venueFilter = "";
        removeFilters(getWindow().getDecorView().getRootView());

        String toolStyle = null;

        if (id == R.id.nav_venue) {
            String ven = getCurrVenueName();
            venueInfo(ven);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_indoor_map) {
            ActivePage = (String) item.getTitle();
            targetLayout = "indoor_vc";
            try {
                onMapIndoor();
            } catch (IOException e) {
                e.printStackTrace();
            }
            subnavMenuLevel.setMaxHeight(150);
            //onMapIndoor();
        } else if (id == R.id.nav_venues) {
            ActivePage = (String) item.getTitle();
            indoorVen = "";
            targetLayout = "venues_vc";
            subnavMenu.setMaxHeight(150);
            //returnToVenues();
            fablocation.show();
            mMap.clear();
            onMapReady(mMap);


            HorizontalScrollView hsv = (HorizontalScrollView) findViewById(R.id.subnav);
            Button button = (Button) findViewById(R.id.filter_1);
            int x, y;
            x = button.getLeft();
            y = button.getTop();
            ObjectAnimator animator=ObjectAnimator.ofInt(hsv, "scrollX",x );
            animator.setDuration(200);
            animator.start();

            //} else if (id == R.id.nav_notifications) {
            // fragmentClass = NotificationsVc.class;
        } else if (id == R.id.nav_settings) {
            ActivePage = (String) item.getTitle();
            targetLayout = "settings_vc";
        }

        //Toolbar style
        try {
            toolbarSetup(toolStyle);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //app title
        TextView textView = (TextView) findViewById(R.id.toolbarTitle);
        textView.setText(ActivePage);

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_in_left).replace(R.id.flContent, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
    /*MAIN NAVIGATION*/


    /*MAP*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
        fixZoom();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void removeFilters(View view) {
        Button f1 = (Button) findViewById(R.id.filter_1);
        f1.setBackgroundColor(getResources().getColor(R.color.white));
        f1.setTextColor(getResources().getColor(R.color.darkGray));

        Button f2 = (Button) findViewById(R.id.filter_2);
        f2.setBackgroundColor(getResources().getColor(R.color.white));
        f2.setTextColor(getResources().getColor(R.color.darkGray));

        Button f3 = (Button) findViewById(R.id.filter_3);
        f3.setBackgroundColor(getResources().getColor(R.color.white));
        f3.setTextColor(getResources().getColor(R.color.darkGray));

        Button f4 = (Button) findViewById(R.id.filter_4);
        f4.setBackgroundColor(getResources().getColor(R.color.white));
        f4.setTextColor(getResources().getColor(R.color.darkGray));

        Button f5 = (Button) findViewById(R.id.filter_5);
        f5.setBackgroundColor(getResources().getColor(R.color.white));
        f5.setTextColor(getResources().getColor(R.color.darkGray));

        Button f6 = (Button) findViewById(R.id.filter_6);
        f6.setBackgroundColor(getResources().getColor(R.color.white));
        f6.setTextColor(getResources().getColor(R.color.darkGray));

        Button f7 = (Button) findViewById(R.id.filter_7);
        f7.setBackgroundColor(getResources().getColor(R.color.white));
        f7.setTextColor(getResources().getColor(R.color.darkGray));
    }

    public boolean goFilter(View view) {
        //reset colors
        removeFilters(view);

        int btn = view.getId();

        HorizontalScrollView hsv = (HorizontalScrollView) findViewById(R.id.subnav);
        Button button = (Button) findViewById(btn);
        int x, y;
        x = button.getLeft();
        y = button.getTop();
        ObjectAnimator animator=ObjectAnimator.ofInt(hsv, "scrollX",x );
        animator.setDuration(200);
        animator.start();

        Button b = (Button) view.findViewById(btn);
        b.setBackgroundColor(getResources().getColor(R.color.blue));
        b.setTextColor(getResources().getColor(R.color.white));
        b.setPressed(true);

        String tag = view.getTag().toString();
        venueFilter = tag;

        //Change fragments
        Fragment fragment = null;
        Class fragmentClass = null;


        FloatingActionButton fablocation = (FloatingActionButton) findViewById(R.id.fabMyLocation);
        fablocation.hide();

        fragmentClass = Pages.class;

        ActivePage = "Venues";
        indoorVen = "";
        targetLayout = "venues_vc";
        //returnToVenues();
        fablocation.show();
        mMap.clear();
        onMapReady(mMap);

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_in_left).replace(R.id.flContent, fragment).commit();

        return true;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings mUiSettings = mMap.getUiSettings();

        mUiSettings.setCompassEnabled(false);
        mUiSettings.setMapToolbarEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(false);


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setIndoorEnabled(false);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                buildGoogleApiClient();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        googleMap.setOnMarkerClickListener(this);

        venuesList = Splashscreen.getVenues();

        //place markers
        for (int i = 0; i < venuesList.size(); i++) {
            String name = venuesList.get(i).get("name");

            float thelat = Float.parseFloat(venuesList.get(i).get("lat"));
            float thelong = Float.parseFloat(venuesList.get(i).get("lon"));

            String image = venuesList.get(i).get("image");
            String id = venuesList.get(i).get("venueId");

            int venueType = Integer.parseInt(venuesList.get(i).get("venueType"));

            if (!venueFilter.equals("")) {
                if (venueFilter.equals(String.valueOf(venueType))) {
                    //use asset for icon
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(thelat, thelong))
                            .title(name));


                    int width = 90;
                    int height = 109;

                    //CUSTOM
                    switch (venueType) {

                        case 1: {
                            @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.mallannotation);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));


                            break;
                        }
                        case 2: {
                            @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.schoolannotation);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            break;
                        }
                        case 3: {
                            @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.airportannotation);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            break;
                        }
                        case 4: {
                            @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.hospitalannotation);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            break;
                        }
                        case 5: {
                            @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.tradeshowannotation);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            break;
                        }
                        case 6: {
                            @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.stadiumannotation);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            break;
                        }

                        case 7: {
                            @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.entertainmentannotation);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            break;
                        }

                        default: {
                            @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.mallannotation);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            break;
                        }
                    }
                }
            } else {

                //use asset for icon
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(thelat, thelong))
                        .title(name));


                int width = 90;
                int height = 109;

                //CUSTOM
                switch (venueType) {

                    case 1: {
                        @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.mallannotation);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));


                        break;
                    }
                    case 2: {
                        @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.schoolannotation);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                        break;
                    }
                    case 3: {
                        @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.airportannotation);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                        break;
                    }
                    case 4: {
                        @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.hospitalannotation);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                        break;
                    }
                    case 5: {
                        @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.tradeshowannotation);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                        break;
                    }
                    case 6: {
                        @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.stadiumannotation);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                        break;
                    }

                    case 7: {
                        @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.entertainmentannotation);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                        break;
                    }

                    default: {
                        @SuppressLint("ResourceType") BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.mallannotation);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                        break;
                    }
                }
            }
        }


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Location mLastLocation = location;

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        //stop location updates
        if (mGoogleApiClient != null && indoorVen == "") {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public void fixZoom() {
        Splashscreen splash = new Splashscreen();
        venuesListLatLong = splash.getVenuesLatLong();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < venuesListLatLong.size(); i++) {
            String lat = venuesListLatLong.get(i).get("lat");
            String lon = venuesListLatLong.get(i).get("lon");

            double la = Double.parseDouble(lat);
            double lo = Double.parseDouble(lon);
            LatLng latLng = new LatLng(la, lo);
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));
    }

    public void centerMap(View view) {
        Splashscreen splash = new Splashscreen();
        venuesListLatLong = splash.getVenuesLatLong();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < venuesListLatLong.size(); i++) {
            String lat = venuesListLatLong.get(i).get("lat");
            String lon = venuesListLatLong.get(i).get("lon");

            double la = Double.parseDouble(lat);
            double lo = Double.parseDouble(lon);
            LatLng latLng = new LatLng(la, lo);
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60));

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }
    @Override
    protected void onPause() {
        super.onPause();

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    public boolean onMarkerClick(final Marker marker) {
        //Intent intent = new Intent(this, VenueInfo.class);
        String title = marker.getTitle();

        venueInfo(title);
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    /*MAP*/


    /*INDOOR*/
    public void onMapIndoor() throws IOException {
        levelListInfo.removeAll(levelListInfo);
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String vName = preferences.getString("venueName", "");

        mMap.clear();
        indoorVen = vName;

        activeMapLvl=0;
        mapLevels.removeAll(mapLevels);


        onMapIndoorLayover(indoorVen);
    }
    public void levelClicked(View view){
        levelListInfo.removeAll(levelListInfo);
        String t = (String) view.getTag();
        int tag = Integer.parseInt(t);
        activeMapLvl=tag;

        int btn = view.getId();
        Log.d("ID", String.valueOf(btn));

        HorizontalScrollView hsv = (HorizontalScrollView) findViewById(R.id.subnav_levels);
        Button button = (Button) findViewById(btn);
        int x, y;
        x = button.getLeft();
        y = button.getTop();
        ObjectAnimator animator=ObjectAnimator.ofInt(hsv, "scrollX",x );
        animator.setDuration(200);
        animator.start();

        button.setBackgroundColor(getResources().getColor(R.color.blue));
        button.setTextColor(getResources().getColor(R.color.white));
        button.setPressed(true);

        try {
            onMapIndoorLayover(indoorVen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void onMapIndoorLayover(String venueName) throws IOException {

        // Get the Intent that started this activity and extract the string
        String title = venueName;

        BufferedReader input = null;
        File file = null;
        JSONArray vens = null;
        JSONArray levelz = null;
        JSONArray anchs = null;

        try {
            file = new File(getFilesDir(), "venues.json"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();

            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            try {
                JSONObject obj = new JSONObject(String.valueOf(buffer));
                JSONArray venuesSet = obj.getJSONArray("venues");
                vens = venuesSet;

            } catch (Throwable t) {
                //nothing
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // looping through All venues
        for (int i = 0; i < vens.length(); i++) {
            JSONObject row = null;
            try {
                row = vens.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                //Where row == Venue name
                if (title.equals(row.getString("name"))) {

                    JSONArray maps = row.getJSONArray("map");
                    JSONArray levelsSet = maps;
                    levelz = levelsSet;

                    if(mapLevels.size()<1){
                        LinearLayout myContainer1 = findViewById(R.id.levelFilt);
                        if(((LinearLayout) myContainer1).getChildCount() > 0)
                            ((LinearLayout) myContainer1).removeAllViews();

                    //set listener
                    android.view.View.OnClickListener buttonListener = new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            LinearLayout myContainer = findViewById(R.id.levelFilt);
                            if (((LinearLayout) myContainer).getChildCount() > 0) {

                                for (int j = 0; j < myContainer.getChildCount(); j++) {

                                    int btn = myContainer.getChildAt(j).getId();

                                    Button button1 = (Button) findViewById(btn);
                                    button1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                    button1.setTextColor(getResources().getColor(R.color.darkGray));
                                }
                            }

                            levelClicked(v);
                        }
                    };

                    // create patameter
                    final LinearLayout myContainer = findViewById(R.id.levelFilt);

                    final LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);

                    //MAKES LEVEL BUTTON ARRAY
                    for (int j = 0; j < levelz.length(); j++) {
                        JSONObject rowLevel = null;
                        try {
                            rowLevel = levelz.getJSONObject(j);

                            //DYNAMIC HORIZONTAL SCROLL NAV BUTTONS
                            HashMap<String, String> levelzNames = new HashMap<>();
                            levelzNames.put("levelId", String.valueOf(j));
                            levelzNames.put("levelName", rowLevel.getString("levelName"));
                            mapLevels.add(levelzNames);

                            // create new button
                            final Button newbutton = new Button(this);

                            newbutton.setPadding(20, 20, 20, 20);
                            newbutton.setTextSize(15);
                            newbutton.setGravity(Gravity.CENTER);
                            newbutton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            newbutton.setTextColor(getResources().getColor(R.color.darkGray));
                            newbutton.setSingleLine(true);
                            newbutton.setAllCaps(true);
                            newbutton.setTypeface(null, Typeface.BOLD);
                            newbutton.setClickable(true);
                            newbutton.setId(j + 99);

                            // set text
                            newbutton.setText(rowLevel.getString("levelName"));
                            newbutton.setTag(String.valueOf(j));

                            newbutton.setOnClickListener(buttonListener);
                            myContainer.addView(newbutton, p);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


                    for (int j = 0; j < levelz.length(); j++) {
                        JSONObject rowLevel = null;
                        try {
                            rowLevel = levelz.getJSONObject(j);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        //Get info for desired level
                        if (j == activeMapLvl) {
                            try {

                                String level = rowLevel.getString("level");
                                String mapSet = rowLevel.getString("mapSet");
                                String bearing = rowLevel.getString("bear");
                                String levelName = rowLevel.getString("levelName");

                                JSONArray anchors = rowLevel.getJSONArray("anchors");
                                JSONArray anchorsSet = anchors;
                                anchs = anchorsSet;

                                String lat0 = anchs.getJSONObject(0).getString("latitude");
                                String lon0 = anchs.getJSONObject(0).getString("longitude");

                                String lat1 = anchs.getJSONObject(1).getString("latitude");
                                String lon1 = anchs.getJSONObject(1).getString("longitude");

                                String lat2 = anchs.getJSONObject(2).getString("latitude");
                                String lon2 = anchs.getJSONObject(2).getString("longitude");

                                // tmp hash map for single contact
                                HashMap<String, String> levelzz = new HashMap<>();

                                // adding each child node to HashMap key => value
                                levelzz.put("Level", level);
                                levelzz.put("mapSet", "https://gidme.ca/app/media/floorplans/"+mapSet);
                                levelzz.put("bearing", bearing);
                                levelzz.put("levelName", levelName);
                                levelzz.put("anchor0-lat", lat0);
                                levelzz.put("anchor0-lon", lon0);
                                levelzz.put("anchor1-lat", lat1);
                                levelzz.put("anchor1-lon", lon1);
                                levelzz.put("anchor2-lat", lat2);
                                levelzz.put("anchor2-lon", lon2);

                                // adding contact to contact list
                                levelListInfo.add(levelzz);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //sets image to get
        for(int i = 0; i < levelListInfo.size(); i++) {
            String image = levelListInfo.get(i).get("mapSet");
            theLevel = image;
        }

        class SendHttpRequestTask extends AsyncTask<String, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    URL url = new URL(theLevel);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();


                    //WRITE FILE
                    String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                    File fileNEW = new File("map.png");

                    if (fileNEW.exists()) {
                        fileNEW.delete();
                        fileNEW = new File(extStorageDirectory, "map.png");
                    }
                    try {
                        File newfile = new File(extStorageDirectory, "map.png");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //WRITES INPUTSTREAM TO FILE
                    copyInputStreamToFile(input, extStorageDirectory, fileNEW);

                    //WRITES FILE TO BITMAP
                    File filemap = new File(extStorageDirectory,"map.png");
                    Bitmap map = pdfToBitmap(filemap);

                    theMap = map;
                    return map;
                }catch (Exception e){
                    Log.d(TAG,e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                theMap = result;

                positionIndoor();
            }
        }
        new SendHttpRequestTask().execute();
    }

    private void copyInputStreamToFile(InputStream in, String dir,File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(dir+"/"+file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public Bitmap pdfToBitmap(File pdfFile) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        Bitmap map= null;

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();

            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                bitmaps.add(bitmap);
                map = bitmap;
                // close the page page.close();
            }

            // close the renderer renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return map;
        //return bitmaps;
    }

    public void positionIndoor(){
        mMap.clear();

        Log.d("VV", String.valueOf(levelListInfo.get(0)));

        //DECLARED SW-x SW-y, NE-x NE-y, Bearing
        double bear = Float.valueOf(levelListInfo.get(0).get("bearing"));
        double latF0 = Float.valueOf(levelListInfo.get(0).get("anchor0-lat")); //ne lat
        double lonF0 = Float.valueOf(levelListInfo.get(0).get("anchor0-lon")); //ne lng
        double latF1 = Float.valueOf(levelListInfo.get(0).get("anchor1-lat")); //sw lat
        double lonF1 = Float.valueOf(levelListInfo.get(0).get("anchor1-lon")); //sw lng
        double latF2 = Float.valueOf(levelListInfo.get(0).get("anchor2-lat")); //nw lat
        double lonF2 = Float.valueOf(levelListInfo.get(0).get("anchor2-lon")); //nw lng

        LatLng sw = new LatLng(latF1, lonF1);
        LatLng ne = new LatLng(latF0, lonF0);
        LatLng nw = new LatLng(latF2, lonF2);

        LatLngBounds latLngBounds = new LatLngBounds(sw, ne);


        /*POSITION OVERLAY INFO*/

        float x1 = (float) latF0; //ne x
        float y1 = (float) lonF0; //ne y
        float x2 = (float) latF1; //sw x
        float y2 = (float) lonF1; //sw y

        float xc = (x1 + x2) / 2;
        float yc = (y1 + y2) / 2;    // Center point

        float xd = (x1 - x2) / 2;
        float yd = (y1 - y2) / 2;    // Half-diagonal

        float x3 = xc - yd; //se
        float y3 = yc + xd; // Third corner
        float x4 = (float) latF2; //nw
        float y4 = (float) lonF2; // Fourth corner*/

            float w = (float) distance(x1, x4, y1, y4, 0, 0);
            float h = (float) distance(x2, x4, y2, y4, 0, 0);
            Log.d("W", String.valueOf(w));
            Log.d("H", String.valueOf(h));




            //CENTER
            LatLng indoorMAPCenter = new LatLng(xc, yc);

            GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(theMap);
            groundOverlayOptions.image(bitmapDescriptor)
                .position(indoorMAPCenter, w)
                .bearing((float) bear);//the value is clockwise and rotation is about anchor point (which should be by default 0.5,0.5 of your image
            mMap.addGroundOverlay(groundOverlayOptions);
        /*POSITION OVERLAY INFO*/

        /*CAMERA*/
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 230));
        /*CAMERA*/
    }

    public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
    /*INDOOR*/


    /*VENUE*/
    public static String getCurrentVenue(){
        return ActiveVenue;
    }
    public String getCurrVenueName(){
        String title = "";

        MenuItem item = menuSide.findItem(R.id.nav_venue);
        title = (String) item.getTitleCondensed();

        return title;
    }
    public boolean venueInfo(String venueTitle){

        Fragment fragment = null;
        Class fragmentClass = null;

        fragmentClass = Pages.class;
        indoorVen = "";
        targetLayout = "venue_info";

        FloatingActionButton fablocation = (FloatingActionButton) findViewById(R.id.fabMyLocation);
        fablocation.hide();

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down).replace(R.id.flContent, fragment).commit();

        //normalToolbar();
        venueInfoVC(venueTitle);
        return false;
    }
    public void venueInfoVC(String venueTitle){

        TextView textView = (TextView) findViewById(R.id.toolbarTitle);
        textView.setText(venueTitle);
        textView.setTextSize(19);

        //restore drawer
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.syncState();


        // Get the Intent that started this activity and extract the string
        String title = venueTitle;
        venueInfoViewing = venueTitle;

        BufferedReader input = null;
        File file = null;
        JSONArray vens = null;

        try {
            file = new File(getFilesDir(), "venues.json"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();

            TextView textV = (TextView) findViewById(R.id.textView);
            textV.setSingleLine(false);

            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            try {
                JSONObject obj = new JSONObject(String.valueOf(buffer));
                JSONArray venuesSet = obj.getJSONArray("venues");
                vens = venuesSet;

            } catch (Throwable t) {
                textView.setText(Html.fromHtml(String.valueOf(buffer)));
            }

            TextView outputText = (TextView) findViewById(R.id.textView);
            outputText.setMovementMethod(new ScrollingMovementMethod());

        } catch (IOException e) {
            e.printStackTrace();
        }


        // looping through All Contacts
        for (int i = 0; i < vens.length(); i++) {
            JSONObject row = null;
            try {
                row = vens.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (title.equals(row.getString("name"))) {

                    String id = row.getString("venueId");
                    String name = row.getString("name");
                    String venueType = row.getString("venueType");
                    String location = row.getString("location");

                    String lat = row.getString("latitude");
                    String lon = row.getString("longitude");

                    String image = row.getString("image");
                    String descript = row.getString("descript");
                    String phone = row.getString("phone");
                    String weburl = row.getString("web_site");
                    String hours = row.getString("hours");

                    // tmp hash map for single contact
                    HashMap<String, String> venue = new HashMap<>();

                    // adding each child node to HashMap key => value
                    venue.put("id", id);
                    venue.put("name", name);
                    venue.put("venueType", venueType);
                    venue.put("location", location);
                    venue.put("lat", lat);
                    venue.put("lon", lon);
                    venue.put("image", "https://gidme.ca/app/services/venues/" + image);
                    venue.put("descript", descript);
                    venue.put("phone", phone);
                    venue.put("weburl", weburl);
                    venue.put("hours", hours);

                    // adding contact to contact list
                    venuesListInfo.add(venue);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //sets image to get
        for(int i = 0; i < venuesListInfo.size(); i++) {

            String image = venuesListInfo.get(i).get("image");

            theVen = image;
        }

            class SendHttpRequestTask extends AsyncTask<String, Void, Bitmap> {
                @Override
                protected Bitmap doInBackground(String... params) {
                    try {
                        URL url = new URL(theVen);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        return myBitmap;
                    }catch (Exception e){
                        Log.d(TAG,e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap result) {
                    ImageView imageView = (ImageView) findViewById(R.id.venueDisplay);
                    imageView.setImageBitmap(result);

                        String id = venuesListInfo.get(0).get("id");
                        String name = venuesListInfo.get(0).get("name");
                        String venueType = venuesListInfo.get(0).get("venueType");
                        String location = venuesListInfo.get(0).get("location");

                        String image = venuesListInfo.get(0).get("image");
                        String descript = venuesListInfo.get(0).get("descript");
                        String phone = venuesListInfo.get(0).get("phone");
                        String web = venuesListInfo.get(0).get("weburl");
                        String hours = venuesListInfo.get(0).get("hours");

                        storeHours = hours;

                        theVen = image;
                        theVenId = id;
                        venueIDViewing = theVenId;

                        // Capture the layout's TextView and set the string as its text
                        TextView NLtext = (TextView) findViewById(R.id.nameLocation);
                        NLtext.setSingleLine(false);
                        NLtext.setText(Html.fromHtml("<h2>" + name + "</h2>" +
                                "<h2>" + location + "</h2>"));

                        TextView textViewPhone = (TextView) findViewById(R.id.textViewPhone);
                        textViewPhone.setSingleLine(false);
                        textViewPhone.setText(Html.fromHtml("<h2>" + phone + "</h2>"));


                        TextView textView2 = (TextView) findViewById(R.id.textView2);
                        textView2.setSingleLine(false);
                        textView2.setText(Html.fromHtml("<h2><a href='http://" + web + "'>" + web + "</a></h2>" +
                                "<h2>" + name + "</h2>" +
                                "<p>" + descript + "</p>" +
                                ""));

                        TextView textUrl = (TextView) findViewById(R.id.textView2);
                        textUrl.setMovementMethod(LinkMovementMethod.getInstance());

                        //set hours open or closed
                        TextView storeHours = (TextView) findViewById(R.id.openStoreIcon);
                        storeHours.setSingleLine(false);

                        Boolean isOpen = null;

                        try {
                            isOpen = isVenueOpen(hours);
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }

                        //Boolean isOpen = false;
                        if(isOpen) {
                            storeHours.setText("Now Open");
                        }else{
                            storeHours.setText("Closed");
                        }
                }
            }
            new SendHttpRequestTask().execute();

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView titleT = (TextView) findViewById(R.id.toolbarTitle);
        titleT.setVisibility(View.GONE);

        ImageView backBtn = (ImageView) findViewById(R.id.venueBack);
        backBtn.setVisibility(View.VISIBLE);

        MenuItem item = searchMenu.findItem(R.id.action_settings);
        item.setVisible(false);

        ConstraintLayout subnavMenu = (ConstraintLayout) findViewById(R.id.subnavMenu);
        subnavMenu.setMaxHeight(0);
        ConstraintLayout subnavMenuLevel = (ConstraintLayout) findViewById(R.id.subnavMenuLevel);
        subnavMenuLevel.setMaxHeight(0);
    }
    public void setNewVenue(View view){
        ActiveVenue = venueInfoViewing;
        ActiveId = venueIDViewing;

        //sets Side nav
        MenuItem item = menuSide.findItem(R.id.nav_venue);
        item.setTitle("Venue: "+venueInfoViewing);
        item.setTitleCondensed(venueInfoViewing);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Splashscreen usee = new Splashscreen();
        usee.updateAPref(this,"venueId", ActiveId);
        usee.updateAPref(this,"venueName", ActiveVenue);

        //sets sidebar nav image
        String title = ActiveVenue;
        BufferedReader input = null;
        File file = null;
        JSONArray vens = null;

        try {
            file = new File(getFilesDir(), "venues.json"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();


            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            try {
                JSONObject obj = new JSONObject(String.valueOf(buffer));
                JSONArray venuesSet = obj.getJSONArray("venues");
                vens = venuesSet;

            } catch (Throwable t) {
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        // looping through All Contacts
        for (int i = 0; i < vens.length(); i++) {
            JSONObject row = null;
            try {
                row = vens.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (title.equals(row.getString("name"))) {

                    String id = row.getString("venueId");
                    String image = row.getString("image");
                    String hours = row.getString("hours");

                    // tmp hash map for single contact
                    HashMap<String, String> venue = new HashMap<>();

                    // adding each child node to HashMap key => value
                    venue.put("id", id);
                    venue.put("image", "https://gidme.ca/app/services/venues/" + image);
                    venue.put("hours", hours);

                    // adding contact to contact list
                    venuesListInfo.add(venue);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //sets image to get
        for(int i = 0; i < venuesListInfo.size(); i++) {
            String image = venuesListInfo.get(i).get("image");
            String hours = venuesListInfo.get(i).get("hours");


            TextView textHour = findViewById(R.id.sideOpenStoreIcon);

            Boolean isOpen = null;

            try {
                isOpen = isVenueOpen(hours);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            //Boolean isOpen = false;
            if (isOpen) {
                textHour.setText("Now Open");
            } else {
                textHour.setText("Closed");
            }

            theVen = image;

        }

        class SendHttpRequestTask extends AsyncTask<String, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    URL url = new URL(theVen);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                }catch (Exception e){
                    Log.d(TAG,e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                ImageView imageView =  findViewById(R.id.sideMenuImg);
                imageView.setImageBitmap(result);
            }
        }
        new SendHttpRequestTask().execute();


        closeVenueInfo(view);
    }

    public void closeBack(View view){

        String targetLay = targetLayout;
        switch(targetLay) {
            case "venue_info" :
                closeVenueInfo(view);
                break;

            case "venue_list" :
                MenuItem item = menuSide.findItem(R.id.nav_settings);
                onNavigationItemSelected(item);
                break;

            case "find_venues" :
                SettingsChangeVenue(view);
                break;

            // You can have any number of case statements.
            default :
                // Statements
        }
        MenuItem item = searchMenu.findItem(R.id.action_settings);
        item.setVisible(true);
    }
    public void closeVenueInfo(View view){
        venueInfoViewing = "";
        venuesListInfo.removeAll(venuesListInfo);

        //restore drawer
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setVisibility(View.VISIBLE);

        ImageView backBtn = (ImageView) findViewById(R.id.venueBack);
        backBtn.setVisibility(View.GONE);

        MenuItem item = menuSide.findItem(R.id.nav_venue);
        item.setVisible(true);


        //Change fragments
        Fragment fragment = null;
        Class fragmentClass = null;

        fragmentClass = Pages.class;
        indoorVen = "";

        //clean
        if(Objects.equals(ActivePage, "Settings")){
            targetLayout = "settings_vc";
        }else if(Objects.equals(ActivePage, "Find Venues")){
            targetLayout = "venues_vc";
            ConstraintLayout subnavMenu = (ConstraintLayout) findViewById(R.id.subnavMenu);
            subnavMenu.setMaxHeight(150);
        }else if(Objects.equals(ActivePage, "Venues")){
            targetLayout = "venues_vc";
            ConstraintLayout subnavMenu = (ConstraintLayout) findViewById(R.id.subnavMenu);
            subnavMenu.setMaxHeight(150);
        }


        TextView textView = findViewById(R.id.toolbarTitle);
        textView.setText(ActivePage);
        textView.setTextSize(26);

        FloatingActionButton fablocation = (FloatingActionButton) findViewById(R.id.fabMyLocation);
        fablocation.hide();

        fablocation.show();

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down).replace(R.id.flContent, fragment).commit();

    }

    public void hoursClick(View view){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                dialogContext);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<h4><b>Hours of Operation</b></h4>"));

        // set dialog message
        alertDialogBuilder
                .setMessage(storeHours)
                .setCancelable(false)
                .setPositiveButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        dialog.cancel();
                    }
                });
                /*.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        VenueInfo.this.finish();
                    }
                });*/

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    public static boolean isVenueOpen(String hours) throws ParseException {
        if (hours.length() == 0) {
            return false;
        }

        boolean venueOpen = false;

        ArrayList hoursOo = new ArrayList(Arrays.asList(hours.split("\n\n", -1)));

        /**
         * Get the current day of the week in an int format and use it to grab the correct element from our hoursOfOperation array.
         */
        Calendar calendar = Calendar.getInstance();
        int dayName = calendar.get(Calendar.DAY_OF_WEEK);
        int weekday = 0;

        switch (dayName) {
            case Calendar.SUNDAY:
                // Current day is Sunday
                weekday = 0;
                break;
            case Calendar.MONDAY:
                // Current day is Monday
                weekday = 1;
                break;
            case Calendar.TUESDAY:
                // etc.
                weekday = 2;
                break;
            case Calendar.WEDNESDAY:
                // etc.
                weekday = 3;
                break;
            case Calendar.THURSDAY:
                // etc.
                weekday = 4;
                break;
            case Calendar.FRIDAY:
                // etc.
                weekday = 5;
                break;
            case Calendar.SATURDAY:
                // etc.
                weekday = 6;
                break;
        }

        String day = hoursOo.get((weekday - 1)).toString();

        /**
         * Parse the element selected from the hoursOfOperation array to break down into the opening hour and the closing hour.
         */
        String[] parts = day.split("day:");
        String times = parts[1];

        String[] time = times.split("-");

        String openingHour = time[0];
        String closingHour = time[1];

        /**
         * If the opening hour and closing hour are the same, it must mean that our venue is open 24 hours.
         */
        if(openingHour.equals(closingHour)){
            venueOpen = true;
        }else {

            //if current time is equal to opening . venueOpen true
            //If current time is equal to closing . venueOpen = false;
            //If current time is between opening or closing . venueOpen = true
            //Else . venueOpen = false.

            Date currentTime = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat();
            dateFormatter.applyPattern("h:a");
            String resultStringNoMinutes = dateFormatter.format(currentTime);

            String[] AorP = resultStringNoMinutes.split(":");

            String actualTimeValue = AorP[0];
            String currSide = AorP[1];

            //Opening Time
            String[] open = openingHour.split(":");
            String openingTimeValue = open[0];

            //Closing Time
            String[] close = closingHour.split(":");
            String closingTimeValue = close[0];
            openingTimeValue = openingTimeValue.replaceAll("\\s+", "");
            closingTimeValue = closingTimeValue.replaceAll("\\s+", "");

            actualTimeValue = actualTimeValue+":00";
            openingTimeValue = openingTimeValue+":00";
            closingTimeValue = closingTimeValue+":00";

            actualTimeValue = getGMT(actualTimeValue,currSide);

            String openSide = openingHour.substring(Math.max(openingHour.length() - 3, 0));
            openingTimeValue = getGMT(openingTimeValue,openSide);

            String closeSide = closingHour.substring(Math.max(closingHour.length() - 3, 0));
            closingTimeValue = getGMT(closingTimeValue,closeSide);

            String[] currT = actualTimeValue.split(":");
            String currTime = currT[0];
            actualTimeValue = currTime;

            String[] openT = openingTimeValue.split(":");
            String openTime = openT[0];
            openingTimeValue = openTime;

            String[] closeT = closingTimeValue.split(":");
            String closeTime = closeT[0];
            closingTimeValue = closeTime;

            if(Integer.parseInt(actualTimeValue) >= Integer.parseInt(openingTimeValue) && Integer.parseInt(actualTimeValue) < Integer.parseInt(closingTimeValue)){
                venueOpen = true;
            }else if(Integer.parseInt(actualTimeValue) >= Integer.parseInt(closingTimeValue)){
                venueOpen = false;
            }else{
                venueOpen = false;
            }
        }

        return venueOpen;
    }
    public static String getGMT(String Time, String Side) throws ParseException {
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
        Date date = parseFormat.parse(Time+" "+Side);
        System.out.println(parseFormat.format(date) + " = " + displayFormat.format(date));

        return displayFormat.format(date);
    }
    /*VENUE*/


    /*SETTINGS*/
    public void SettingsVcClicked(View view) {

        int IdAsString = view.getId();

        if (IdAsString == R.id.settingsWebInfo1) {
            Uri uri = Uri.parse("http://gidme.ca/support/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (IdAsString == R.id.settingsWebInfo2) {
            Uri uri = Uri.parse("http://gidme.ca/privacy/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (IdAsString == R.id.settingsWebInfo3) {
            Uri uri = Uri.parse("http://gidme.ca/support/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }
    public void SettingsSwitched(View view){
        int IdAsString = view.getId();

        Splashscreen usee = new Splashscreen();

        if (IdAsString == R.id.pushSwitch) {
            // initiate a Switch
            Switch simpleSwitch = findViewById(R.id.pushSwitch);
            Boolean switchState = simpleSwitch.isChecked();
            String myVal = (switchState) ? "1" : "0";

            usee.updateAPref(this,"pushNotifications", myVal);
        } else if (IdAsString == R.id.locationSwitch){
            // initiate a Switch
            Switch simpleSwitch = findViewById(R.id.locationSwitch);
            Boolean switchState = simpleSwitch.isChecked();
            String myVal = (switchState) ? "1" : "0";

            usee.updateAPref(this,"locationServices", myVal);
        } else if (IdAsString == R.id.offersSwitch){
            // initiate a Switch
            Switch simpleSwitch = findViewById(R.id.offersSwitch);
            Boolean switchState = simpleSwitch.isChecked();
            String myVal = (switchState) ? "1" : "0";

            usee.updateAPref(this,"nearbyOffers", myVal);
        }
    }
    public void SettingsVenueInfo(View view){
        String ven = getCurrVenueName();
        venueInfo(ven);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
    public boolean SettingsChangeVenue(View view){

        Fragment fragment = null;
        Class fragmentClass = null;

        fragmentClass = Pages.class;
        indoorVen = "";
        targetLayout = "venue_list";

        FloatingActionButton fablocation = (FloatingActionButton) findViewById(R.id.fabMyLocation);
        fablocation.hide();

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down).replace(R.id.flContent, fragment).commit();
        blueToolbar();
        return false;

    }
    public void listVenueInfo(View view){
        targetLayout = "find_venues";

        TextView t = (TextView) view;
        String input = t.getText().toString();
        venueInfo(input);
    }
    /*SETTINGS*/
}
