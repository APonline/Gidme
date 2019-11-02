package ca.gidme.gidme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static ca.gidme.gidme.MainActivity.*;


public class VenueInfo extends AppCompatActivity {

    ArrayList<HashMap<String, String>> venuesListInfo;
    ArrayList<HashMap<String, String>> settingsListInfo;
    private String TAG = MainActivity.class.getSimpleName();
    public String theVen ="";
    public String theVenId ="";
    public String storeHours = "";

    final Context dialogContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_info);

        getSupportActionBar().setTitle(null);
        venuesListInfo = new ArrayList<>();
        settingsListInfo = new ArrayList<>();


        BufferedReader input = null;
        File file = null;
        JSONArray vens = null;

        try {
            file = new File(getFilesDir(), "venues.json"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();

            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setSingleLine(false);

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


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_MESSAGE_title);

        // looping through All Contacts
        for (int i = 0; i < vens.length(); i++) {
            JSONObject row = null;
            try {
                row = vens.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if(title.equals(row.getString("name"))) {

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

        for(int i = 0; i < venuesListInfo.size(); i++) {

            String id = venuesListInfo.get(i).get("id");
            String name = venuesListInfo.get(i).get("name");
            String venueType = venuesListInfo.get(i).get("venueType");
            String location = venuesListInfo.get(i).get("location");

            String image = venuesListInfo.get(i).get("image");
            String descript = venuesListInfo.get(i).get("descript");
            String phone = venuesListInfo.get(i).get("phone");
            String web = venuesListInfo.get(i).get("weburl");
            String hours = venuesListInfo.get(i).get("hours");

            storeHours = hours;

            theVen = image;
            theVenId = id;

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
                }
            }
            new SendHttpRequestTask().execute();

            //set hours open or closed
            TextView storeHours = (TextView) findViewById(R.id.openStoreIcon);
            storeHours.setSingleLine(false);

            MainActivity goHour = new MainActivity();
            Boolean isOpen = null;
            try {
                isOpen = goHour.isVenueOpen(hours);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            Log.d("WTF","hello venue");
            //Boolean isOpen = false;
            if(isOpen) {
                storeHours.setText("Now Open");
            }else{
                storeHours.setText("Closed");
            }

            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"darkGray\">" + name + "</font>"));
            final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(getResources().getColor(R.color.darkGray), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);

            // Capture the layout's TextView and set the string as its text
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setSingleLine(false);
            textView.setText(Html.fromHtml("<h2>" + name + "</h2>" +
                    "<h2>" + location + "</h2>"));

            TextView textViewp = (TextView) findViewById(R.id.textViewPhone);
            textViewp.setSingleLine(false);
            textViewp.setText(Html.fromHtml("<h2>" + phone + "</h2>"));

            TextView textView2 = (TextView) findViewById(R.id.textView2);
            textView2.setSingleLine(false);
            textView2.setText(Html.fromHtml("<h2><a href='http://"+web+"'>" + web + "</a></h2>" +
                    "<h2>" + name + "</h2>" +
                    "<p>" + descript + "</p>" +
                    ""));

            TextView textUrl = (TextView) findViewById(R.id.textView2);
            textUrl.setMovementMethod(LinkMovementMethod.getInstance());
        }
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

    public void closeVenueInfo(View view){
        //setContentView(R.layout.activity_main);
    }

    public void onBackPressed(View view) throws JSONException, FileNotFoundException {
        //generateNoteOnSD("venue.txt", theVenId);



        BufferedReader input = null;
        File file = null;
        JSONObject obj = null;
        JSONArray user = null;

        try {
            //Reads user JSON
            file = new File(getFilesDir(), "user.json"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();

            //TextView textView = (TextView) findViewById(R.id.textView);
            //textView.setSingleLine(false);

            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            try {
                obj = new JSONObject(String.valueOf(buffer));
                JSONArray userSet = obj.getJSONArray("settings");
                //user = userSet;
                //Log.e(TAG, "Response from url: " + user.toString());
            } catch (Throwable t) {
                //textView.setText(Html.fromHtml(String.valueOf(buffer)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        file = new File(getFilesDir(), "user.json");
        input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        JSONArray arr = new JSONArray(input);
        for(int i = 0; i < arr.length(); i++){

            JSONObject jsonObj = (JSONObject)arr.get(i); // get the josn object
            if(jsonObj.getString("venueId").equals(".*")){ // compare for the key-value
                ((JSONObject)arr.get(i)).put("venueId", theVenId); // put the new value for the key
            }
            Log.e(TAG, "Response from url: " + arr.toString());
            //textview.setText(arr.toString());// display and verify your Json with updated value
        }


        //read user settings
        /*BufferedReader input = null;
        File file = null;
        JSONObject obj = null;
        JSONArray user = null;

        try {
            //Reads user JSON
            file = new File(getFilesDir(), "user.json"); // Pass getFilesDir() and "MyFile" to read file

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();

            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setSingleLine(false);

            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }

            try {
                obj = new JSONObject(String.valueOf(buffer));
                JSONArray userSet = obj.getJSONArray("settings");
                user = userSet;
                Log.e(TAG, "Response from url: " + user.toString());
            } catch (Throwable t) {
                textView.setText(Html.fromHtml(String.valueOf(buffer)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        //read values from user.json
        // looping through All Contacts
        for (int i = 0; i < obj.length(); i++) {
            JSONObject row = user.getJSONObject(i);
            try {
                String venueId = row.getString("venueId");

                // tmp hash map for single contact
                HashMap<String, String> venue = new HashMap<>();

                // adding each child node to HashMap key => value
                venue.put("venueId", venueId);

                // adding contact to contact list
                settingsListInfo.add(venue);
            } catch (JSONException e) {
                String venueId = "0";
            }
        }

        org.json.simple.JSONObject objSetting = new org.json.simple.JSONObject();
        org.json.simple.JSONObject objS = new org.json.simple.JSONObject();
        for(int i = 0; i < settingsListInfo.size(); i++) {

            String venueId = settingsListInfo.get(i).get("venueId");
            Log.d("var",theVenId);
            objS.put("venueId", theVenId);
        }
        objSetting.put("settings", objS);
        // end read values from user.json

        try {
            FileOutputStream fos = openFileOutput("user.json", Context.MODE_PRIVATE);
            fos.write(objSetting.toString().getBytes());
            fos.close();
            Log.d("test1A","write worked");
            System.out.print(objSetting);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("test2A","didnt write");
        }*/

        //super.onBackPressed();
        //finish();
    }
    /**
     * Write Note
     * @param sFileName this is the local storage file that will be written to.
     * @param sBody this is the string that will be captured and written to the file.
     */
    public void generateNoteOnSD(String sFileName, String sBody) {

        // Create a file in the Internal Storage
        FileOutputStream outputStream = null;

        try {
            outputStream = openFileOutput(sFileName, Context.MODE_APPEND);
            outputStream.write(sBody.getBytes());
            outputStream.close();

            Toast.makeText(getBaseContext(), "Venue Set", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Venue Not Set", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}