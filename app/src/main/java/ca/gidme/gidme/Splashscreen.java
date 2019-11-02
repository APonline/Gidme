package ca.gidme.gidme;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;
import static java.lang.Thread.sleep;


public class Splashscreen extends Activity {

    private static final String[] INITIAL_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] LOCATION_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;
    public static final String EXTRA_MESSAGE_title = "";

    private static ArrayList<HashMap<String, String>> userList;
    private static ArrayList<HashMap<String, String>> venueList;
    private static ArrayList<HashMap<String, String>> venuesList;

    //venuesListLatLong is used for fixing the zoom size
    private static ArrayList<HashMap<String, String>> venuesListLatLong;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    // Called when the activity is first created.
    Thread splashThread;

    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        // Perhaps set content view here

        userList = new ArrayList<>();
        venueList = new ArrayList<>();
        venuesList = new ArrayList<>();
        venuesListLatLong = new ArrayList<>();

        prefs = getSharedPreferences("ca.Gidme.Gidme", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (prefs.getBoolean("firstrun", true)) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
            prefs.edit().putBoolean("firstrun", false).apply();

            //--Init
            int myON = 1;
            int myOFF = 0;
            String myVenue = "";
            String myVenueName = "";

            //--SAVE Data
            SharedPreferences preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("pushNotifications", myOFF);
            editor.putInt("locationServices", myOFF);
            editor.putInt("nearbyOffers", myOFF);
            editor.putString("venueId", myVenue);
            editor.putString("venueName", myVenueName);
            editor.apply();

            HashMap<String, String> userPrefs = new HashMap<>();

            // adding each child node to HashMap key => value
            userPrefs.put("pushNotifications", String.valueOf(myOFF));
            userPrefs.put("locationServices", String.valueOf(myOFF));
            userPrefs.put("nearbyOffers", String.valueOf(myOFF));
            userPrefs.put("venueId", myVenue);
            userPrefs.put("venueName", myVenueName);

            // adding contact to contact list
            userList.add(userPrefs);

        }else{
            SharedPreferences preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

            int pref1 = preferences.getInt("pushNotifications", 0);
            int pref2 = preferences.getInt("locationServices", 0);
            int pref3 = preferences.getInt("nearbyOffers", 0);
            String pref4 = preferences.getString("venueId", "");
            String pref5 = preferences.getString("venueName", "");

            HashMap<String, String> userPrefs = new HashMap<>();

            // adding each child node to HashMap key => value
            userPrefs.put("pushNotifications", String.valueOf(pref1));
            userPrefs.put("locationServices", String.valueOf(pref2));
            userPrefs.put("nearbyOffers", String.valueOf(pref3));
            userPrefs.put("venueId", pref4);
            userPrefs.put("venueName", pref5);

            // adding contact to contact list
            userList.add(userPrefs);
        }

        new GetVenues().execute();
    }

    //Get Venues
    private class GetVenues extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(Splashscreen.this,"Loading nearby venues",Toast.LENGTH_LONG).show();
        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "https://gidme.ca/app/services/app.android.1.0.0.php";
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray venueSet = jsonObj.getJSONArray("venues");

                        /*WRITE JSON*/
                    org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
                    obj.put("venues", venueSet);


                    String FILE_NAME = "venues.json";
                    writeToJson(FILE_NAME, obj);
                        /*WRITE JSON END*/

                    // looping through All Contacts
                    for (int i = 0; i < venueSet.length(); i++) {
                        JSONObject row = venueSet.getJSONObject(i);

                        String venueId = row.getString("venueId");
                        String name = row.getString("name");
                        String venueType = row.getString("venueType");

                        String lat = row.getString("latitude");
                        String lon = row.getString("longitude");

                        String image = row.getString("image");
                        String descript = row.getString("descript");
                        String phone = row.getString("phone");
                        String weburl = row.getString("web_site");
                        String hours = row.getString("hours");


                        // tmp hash map for single contact
                        HashMap<String, String> venue = new HashMap<>();
                        HashMap<String, String> venuesListLL = new HashMap<>();


                        // adding each child node to HashMap key => value
                        venue.put("venueId", venueId);
                        venue.put("name", name);
                        venue.put("venueType", venueType);
                        venue.put("lat", lat);
                        venue.put("lon", lon);
                        venue.put("image", "https://gidme.ca/app/services/venues/"+image);
                        venue.put("descript", descript);
                        venue.put("phone", phone);
                        venue.put("weburl", weburl);
                        venue.put("hours", hours);

                        // adding contact to contact list
                        venuesList.add(venue);

                        venuesListLL.put("name",name);
                        venuesListLL.put("lat",lat);
                        venuesListLL.put("lon",lon);
                        venuesListLatLong.add(venuesListLL);

                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("venueId", venueId);
                        contact.put("name", name);
                        contact.put("venueType", venueType);

                        // adding contact to contact list
                        venueList.add(contact);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            StartAnimations();
        }
    }

    //Writes JSON file
    public void writeToJson(String thefile, org.json.simple.JSONObject object){
        try {
            FileOutputStream fos = openFileOutput(thefile, Context.MODE_PRIVATE);
            fos.write(object.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Return lists
    public static ArrayList updateAPref(Context context,String pref, String val){
        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if(Objects.equals(pref, "pushNotifications")){
            editor.putInt("pushNotifications", Integer.parseInt(val));
        }else if(Objects.equals(pref, "locationServices")){
            editor.putInt("locationServices", Integer.parseInt(val));
        }else if(Objects.equals(pref, "nearbyOffers")){
            editor.putInt("nearbyOffers", Integer.parseInt(val));
        }else if(Objects.equals(pref, "venueId")){
            editor.putString("venueId", val);
        }else if(Objects.equals(pref, "venueName")){
            editor.putString("venueName", val);
        }

        editor.apply();

        updatedPrefs(context);
        //Toast.makeText(context,"Preferences Updated.",Toast.LENGTH_SHORT).show();
        Log.d("USER UPDATE", String.valueOf(userList));
        return userList;
    }
    public static ArrayList updatedPrefs(Context context){
        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        int pref1 = preferences.getInt("pushNotifications", 0);
        int pref2 = preferences.getInt("locationServices", 0);
        int pref3 = preferences.getInt("nearbyOffers", 0);
        String pref4 = preferences.getString("venueId", "");
        String pref5 = preferences.getString("venueName", "");

        HashMap<String, String> userPrefs = new HashMap<>();

        // adding each child node to HashMap key => value
        userPrefs.put("pushNotifications", String.valueOf(pref1));
        userPrefs.put("locationServices", String.valueOf(pref2));
        userPrefs.put("nearbyOffers", String.valueOf(pref3));
        userPrefs.put("venueId", pref4);
        userPrefs.put("venueName", pref5);

        // adding contact to contact list
        userList.removeAll(userList);
        userList.add(userPrefs);
        return userList;
    }

    public static ArrayList getSettings(){
        return userList;
    }
    public static ArrayList getVenueNames(){
        return venueList;
    }
    public static ArrayList getVenues(){
        return venuesList;
    }
    public static ArrayList getVenuesLatLong(){
        return venuesListLatLong;
    }

    //Animation
    private void StartAnimations(){
        //load animations
        Animation animIn = AnimationUtils.loadAnimation(this, R.anim.alpha_fast_in);
        animIn.reset();
        final Animation animOut = AnimationUtils.loadAnimation(this, R.anim.alpha_slow_out);
        animOut.reset();

        //run first animation
        LinearLayout l=(LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(animIn);

        animIn = AnimationUtils.loadAnimation(this, R.anim.translate);
        animIn.reset();

        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {

                    //run second animation
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout l=(LinearLayout) findViewById(R.id.lin_lay);
                            l.clearAnimation();
                            l.startAnimation(animOut);
                        }
                    });

                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 1000) {
                        sleep(100);
                        waited += 100;
                    }

                    //trigger intent
                    Intent intent = new Intent(Splashscreen.this,
                            MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    Splashscreen.this.finish();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    Splashscreen.this.finish();
                }

            }
        };
        splashTread.start();
    }

}
