package ca.gidme.gidme;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by apanemia on 2017-11-14.
 */

public class WorldMap extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        com.google.android.gms.location.LocationListener{

    public static final String EXTRA_MESSAGE_title = "";


    //MainActivity vars
    public GoogleMap mMap;
    public String ActivePage = "";
    public String indoorVen = "";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private ArrayList<HashMap<String, String>> userList;
    private ArrayList<HashMap<String, String>> venuesList;

    //venuesListLatLong is used for fixing the zoom size
    private ArrayList<HashMap<String, String>> venuesListLatLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("WTF","here");
        //Google api connect
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            Log.i("Api is",""+mGoogleApiClient);
        }
        //End Google api connect

        venuesList = new ArrayList<>();
        venuesListLatLong = new ArrayList<>();
        ActivePage = "Venues";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.world_map);

        SupportMapFragment MapView = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        MapView.getMapAsync(this);
        Log.d("WTF1","test");

    }


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
            if(mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public void onMapReady(GoogleMap googleMap) {
        Log.d("WTF","test");
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
            else{
                buildGoogleApiClient();
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        googleMap.setOnMarkerClickListener(this);

        venuesList = Splashscreen.getVenues();

        //place markers
        for(int i = 0; i < venuesList.size(); i++) {
            String name = venuesList.get(i).get("name");

            float thelat = Float.parseFloat(venuesList.get(i).get("lat"));
            float thelong = Float.parseFloat(venuesList.get(i).get("lon"));

            String image = venuesList.get(i).get("image");
            String id = venuesList.get(i).get("venueId");


            //use asset for icon
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(thelat, thelong))
                    .title(name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        fixZoom();

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
    public void fixZoom(){
        venuesListLatLong = Splashscreen.getVenuesLatLong();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i =0; i<venuesListLatLong.size(); i++) {
            String lat = venuesListLatLong.get(i).get("lat");
            String lon = venuesListLatLong.get(i).get("lon");

            double la=Double.parseDouble(lat);
            double lo=Double.parseDouble(lon);
            LatLng latLng = new LatLng(la, lo);
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
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
        Intent intent = new Intent(this, VenueInfo.class);
        String title = marker.getTitle();

        intent.putExtra(EXTRA_MESSAGE_title, title);

        startActivity(intent);
        overridePendingTransition( R.anim.slide_in_up, R.anim.stay );
        return false;
    }
    /*MAP*/
}
