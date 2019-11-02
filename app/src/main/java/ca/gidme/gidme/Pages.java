package ca.gidme.gidme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


public class Pages extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "";

    public String currToolbar = null;
    private static ArrayList<HashMap<String, String>> vList;

    // TODO: Rename and change types of parameters
    private String mParam1;

    private ArrayList<HashMap<String, String>> venueList;
    private ListView venueListSet;
    private ListView lv1;
    private ListView lv2;
    private ListView lv3;
    private ListView lv4;
    private ListView lv5;
    private ListView lv6;
    private ListView lv7;

    public ArrayList<HashMap<String, String>> list1;
    public ArrayList<HashMap<String, String>> list2;
    public ArrayList<HashMap<String, String>> list3;
    public ArrayList<HashMap<String, String>> list4;
    public ArrayList<HashMap<String, String>> list5;
    public ArrayList<HashMap<String, String>> list6;
    public ArrayList<HashMap<String, String>> list7;


    private ListView lvCountry;
    private EditText edtSearch;

    private OnFragmentInteractionListener mListener;
    private Object ven;

    public Pages() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Pages newInstance(String param1) {
        Pages fragment = new Pages();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        list1 = new ArrayList<>();
        list2 = new ArrayList<>();
        list3 = new ArrayList<>();
        list4 = new ArrayList<>();
        list5 = new ArrayList<>();
        list6 = new ArrayList<>();
        list7 = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String lay = MainActivity.getTargetLayout();
        View pg = null;
        currToolbar = "normal";

        if(Objects.equals(lay, "settings_vc")){
            pg = inflater.inflate(R.layout.settings_vc, container, false);

            // Set the Text for venue
            TextView t = pg.findViewById(R.id.settingsVenueTitle);
            MainActivity mainAct = new MainActivity();
            String currVen = mainAct.getCurrentVenue();
            t.setText(currVen);

            // Set the switches for app
            SharedPreferences preferences = getContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

            int pref1 = preferences.getInt("pushNotifications", 0);
            int pref2 = preferences.getInt("locationServices", 0);
            int pref3 = preferences.getInt("nearbyOffers", 0);

            Boolean p1 = pref1 > 0 ? true : false ;
            Boolean p2 = pref2 > 0 ? true : false ;
            Boolean p3 = pref3 > 0 ? true : false ;

            Switch push1 = pg.findViewById(R.id.pushSwitch);
            push1.setChecked(p1);

            Switch push2 = pg.findViewById(R.id.locationSwitch);
            push2.setChecked(p2);

            Switch push3 = pg.findViewById(R.id.offersSwitch);
            push3.setChecked(p3);

        }else if(Objects.equals(lay, "venue_vc")){
            pg = inflater.inflate(R.layout.venue_vc, container, false);
        }else if(Objects.equals(lay, "venues_vc")){
            pg = inflater.inflate(R.layout.venues_vc, container, false);
        }else if(Objects.equals(lay, "indoor_vc")){
            pg = inflater.inflate(R.layout.indoor_vc, container, false);
        }else if(Objects.equals(lay, "venue_info")){
            pg = inflater.inflate(R.layout.venue_info, container, false);
        }else if(Objects.equals(lay, "venue_list")){
            pg = inflater.inflate(R.layout.venue_list, container, false);

            currToolbar = "blue";

            lv1 = (ListView) pg.findViewById(R.id.venueList1);
            lv2 = (ListView) pg.findViewById(R.id.venueList2);
            lv3 = (ListView) pg.findViewById(R.id.venueList3);
            lv4 = (ListView) pg.findViewById(R.id.venueList4);
            lv5 = (ListView) pg.findViewById(R.id.venueList5);
            lv6 = (ListView) pg.findViewById(R.id.venueList6);
            lv7 = (ListView) pg.findViewById(R.id.venueList7);
            TextView lv1T = (TextView) pg.findViewById(R.id.venueListTitle1);
            TextView lv2T = (TextView) pg.findViewById(R.id.venueListTitle2);
            TextView lv3T = (TextView) pg.findViewById(R.id.venueListTitle3);
            TextView lv4T = (TextView) pg.findViewById(R.id.venueListTitle4);
            TextView lv5T = (TextView) pg.findViewById(R.id.venueListTitle5);
            TextView lv6T = (TextView) pg.findViewById(R.id.venueListTitle6);
            TextView lv7T = (TextView) pg.findViewById(R.id.venueListTitle7);
            venueList = Splashscreen.getVenueNames();

            Log.d("VENUES", String.valueOf(venueList));

            for(int i = 0; i < 7; i++){

                switch(i){
                    case 0:{
                        lv1T.setText("Mall");
                        break;
                    }
                    case 1:{
                        lv2T.setText("College Campus");
                        break;
                    }
                    case 2:{
                        lv3T.setText("Airport");
                        break;
                    }
                    case 3:{
                        lv4T.setText("Hospital");
                        break;
                    }
                    case 4:{
                        lv5T.setText("Trade Show");
                        break;
                    }
                    case 5:{
                        lv6T.setText("Stadium");
                        break;
                    }
                    case 6: {
                        lv7T.setText("Entertainment");
                        break;
                    }
                }

                for (HashMap<String, String> ven : venueList) {
                    if(ven.get("venueType").equals(String.valueOf(i + 1))) {
                        Log.d("VENUE", String.valueOf(ven.get("venueType")));

                        HashMap<String, String> typeVenueName = new HashMap<>();
                        typeVenueName.put("name", ven.get("name"));


                        switch(i){
                            case 0:{
                                list1.add(typeVenueName);
                                break;
                            }
                            case 1:{
                                list2.add(typeVenueName);
                                break;
                            }
                            case 2:{
                                list3.add(typeVenueName);
                                break;
                            }
                            case 3:{
                                list4.add(typeVenueName);
                                break;
                            }
                            case 4:{
                                list5.add(typeVenueName);
                                break;
                            }
                            case 5:{
                                list6.add(typeVenueName);
                                break;
                            }
                            case 6: {
                                list7.add(typeVenueName);
                                break;
                            }
                        }
                    }
                }
            }

            //list1
            ListAdapter adapterMall = new SimpleAdapter(getContext(), list1,
                    R.layout.list_item, new String[]{ "name"},
                    new int[]{R.id.name});
            lv1.setAdapter(adapterMall);
            if(list1.size()==0){
                lv1.setVisibility(View.GONE);
                lv1T.setVisibility(View.GONE);
            }

            int totalHeight = 0;
            for (int i = 0; i < adapterMall.getCount(); i++) {
                View listItem = adapterMall.getView(i, null, lv1);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = lv1.getLayoutParams();
            params.height = totalHeight + (lv1.getDividerHeight() * (adapterMall.getCount() - 1));
            lv1.setLayoutParams(params);
            lv1.requestLayout();


            //list2
            ListAdapter adapterMall2 = new SimpleAdapter(getContext(), list2,
                    R.layout.list_item, new String[]{ "name"},
                    new int[]{R.id.name});
            lv2.setAdapter(adapterMall2);
            if(list2.size()==0){
                lv2.setVisibility(View.GONE);
                lv2T.setVisibility(View.GONE);
            }
            totalHeight = 0;
            for (int i = 0; i < adapterMall2.getCount(); i++) {
                View listItem = adapterMall2.getView(i, null, lv2);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            params = lv2.getLayoutParams();
            params.height = totalHeight + (lv2.getDividerHeight() * (adapterMall2.getCount() - 1));
            lv2.setLayoutParams(params);
            lv2.requestLayout();


            //list3
            ListAdapter adapterMall3 = new SimpleAdapter(getContext(), list3,
                    R.layout.list_item, new String[]{ "name"},
                    new int[]{R.id.name});
            lv3.setAdapter(adapterMall3);
            if(list3.size()==0){
                lv3.setVisibility(View.GONE);
                lv3T.setVisibility(View.GONE);
            }
            totalHeight = 0;
            for (int i = 0; i < adapterMall3.getCount(); i++) {
                View listItem = adapterMall3.getView(i, null, lv3);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            params = lv3.getLayoutParams();
            params.height = totalHeight + (lv3.getDividerHeight() * (adapterMall3.getCount() - 1));
            lv3.setLayoutParams(params);
            lv3.requestLayout();


            //list4
            ListAdapter adapterMall4 = new SimpleAdapter(getContext(), list4,
                    R.layout.list_item, new String[]{ "name"},
                    new int[]{R.id.name});
            lv4.setAdapter(adapterMall4);
            if(list4.size()==0){
                lv4.setVisibility(View.GONE);
                lv4T.setVisibility(View.GONE);
            }
            totalHeight = 0;
            for (int i = 0; i < adapterMall4.getCount(); i++) {
                View listItem = adapterMall4.getView(i, null, lv4);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            params = lv4.getLayoutParams();
            params.height = totalHeight + (lv4.getDividerHeight() * (adapterMall4.getCount() - 1));
            lv4.setLayoutParams(params);
            lv4.requestLayout();


            //list5
            ListAdapter adapterMall5 = new SimpleAdapter(getContext(), list5,
                    R.layout.list_item, new String[]{ "name"},
                    new int[]{R.id.name});
            lv5.setAdapter(adapterMall5);
            if(list5.size()==0){
                lv5.setVisibility(View.GONE);
                lv5T.setVisibility(View.GONE);
            }
            totalHeight = 0;
            for (int i = 0; i < adapterMall5.getCount(); i++) {
                View listItem = adapterMall5.getView(i, null, lv5);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            params = lv5.getLayoutParams();
            params.height = totalHeight + (lv5.getDividerHeight() * (adapterMall5.getCount() - 1));
            lv5.setLayoutParams(params);
            lv5.requestLayout();


            //list6
            ListAdapter adapterMall6 = new SimpleAdapter(getContext(), list6,
                    R.layout.list_item, new String[]{ "name"},
                    new int[]{R.id.name});
            lv6.setAdapter(adapterMall6);
            if(list6.size()==0){
                lv6.setVisibility(View.GONE);
                lv6T.setVisibility(View.GONE);
            }
            totalHeight = 0;
            for (int i = 0; i < adapterMall6.getCount(); i++) {
                View listItem = adapterMall6.getView(i, null, lv6);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            params = lv6.getLayoutParams();
            params.height = totalHeight + (lv6.getDividerHeight() * (adapterMall6.getCount() - 1));
            lv6.setLayoutParams(params);
            lv6.requestLayout();


            //list7
            ListAdapter adapterMall7 = new SimpleAdapter(getContext(), list7,
                    R.layout.list_item, new String[]{ "name"},
                    new int[]{R.id.name});
            lv7.setAdapter(adapterMall7);
            if(list7.size()==0){
                lv7.setVisibility(View.GONE);
                lv7T.setVisibility(View.GONE);
            }
            totalHeight = 0;
            for (int i = 0; i < adapterMall7.getCount(); i++) {
                View listItem = adapterMall7.getView(i, null, lv7);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            params = lv7.getLayoutParams();
            params.height = totalHeight + (lv7.getDividerHeight() * (adapterMall7.getCount() - 1));
            lv7.setLayoutParams(params);
            lv7.requestLayout();

        }
        return pg;
    }

    /*private ArrayList sortAndAddSections(ArrayList itemList)
    {

        ArrayList tempList = new ArrayList();
        //First we sort the array
        Collections.sort(itemList);

        //Loops thorugh the list and add a section before each sectioncell start
        String header = "";
       /* for(int i = 0; i < itemList.size(); i++)
        {
            //If it is the start of a new section we create a new listcell and add it to our array
            if(header != itemList.get(i).getCategory()){
                ListCell sectionCell = new ListCell(itemList.get(i).getCategory(), null);
                sectionCell.setToSectionHeader();
                tempList.add(sectionCell);
                header = itemList.get(i).getCategory();
            }
            tempList.add(itemList.get(i));
        }

        return tempList;
    }*/

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        Bitmap pdfToBitmap(File pdfFile);
    }

    /**
     * row item
     */
  /*  public interface Item {
        public boolean isSection();
        public String getTitle();
    }
*/
    /**
     * Section Item
     */
  /*  public class SectionItem implements Item {
        private final String title;

        public SectionItem(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public boolean isSection() {
            return true;
        }
    }
*/
    /**
     * Entry Item
     */
  /*  public class EntryItem implements Item {
        public final String title;

        public EntryItem(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public boolean isSection() {
            return false;
        }
    }
*/
    /**
     * Adapter
     */
  /*  public class CountryAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<Item> item;
        private ArrayList<Item> originalItem;

        public CountryAdapter() {
            super();
        }

        public CountryAdapter(Context context, ArrayList<Item> item) {
            this.context = context;
            this.item = item;
            //this.originalItem = item;
        }

        @Override
        public int getCount() {
            return item.size();
        }

        @Override
        public Object getItem(int position) {
            return item.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (item.get(position).isSection()) {
                // if section header
                convertView = inflater.inflate(R.layout.layout_section, parent, false);
                TextView tvSectionTitle = (TextView) convertView.findViewById(R.id.tvSectionTitle);
                tvSectionTitle.setText(((SectionItem) item.get(position)).getTitle());
            }
            else
            {
                // if item
                convertView = inflater.inflate(R.layout.layout_item, parent, false);
                TextView tvItemTitle = (TextView) convertView.findViewById(R.id.tvItemTitle);
                tvItemTitle.setText(((EntryItem) item.get(position)).getTitle());
            }

            return convertView;
        }

        /**
         * Filter
         */
        /*public LayoutInflater.Filter getFilter()
        {
            LayoutInflater.Filter filter = new LayoutInflater.Filter() {

                @Override
                public boolean onLoadClass(Class aClass) {
                    return false;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    item = (ArrayList<Item>) results.values;
                    notifyDataSetChanged();
                }

                @SuppressWarnings("null")
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults results = new FilterResults();
                    ArrayList<Item> filteredArrayList = new ArrayList<Item>();


                    if(originalItem == null || originalItem.size() == 0)
                    {
                        originalItem = new ArrayList<Item>(item);
                    }

                    //
                    // if constraint is null then return original value
                    // else return filtered value

                    if(constraint == null && constraint.length() == 0)
                    {
                        results.count = originalItem.size();
                        results.values = originalItem;
                    }
                    else
                    {
                        constraint = constraint.toString().toLowerCase(Locale.ENGLISH);
                        for (int i = 0; i < originalItem.size(); i++)
                        {
                            String title = originalItem.get(i).getTitle().toLowerCase(Locale.ENGLISH);
                            if(title.startsWith(constraint.toString()))
                            {
                                filteredArrayList.add(originalItem.get(i));
                            }
                        }
                        results.count = filteredArrayList.size();
                        results.values = filteredArrayList;
                    }

                    return results;
                }
            };

            return filter;
        }
    }
*/

}
