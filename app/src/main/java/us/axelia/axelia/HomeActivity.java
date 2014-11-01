package us.axelia.axelia;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.commonsware.cwac.merge.MergeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class HomeActivity extends ActionBarActivity {
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (savedInstanceState == null) {
        }
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.Tab citiesTab = getSupportActionBar().newTab().setText(R.string.cities_list_tab_title);
        citiesTab.setTabListener(new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                HomeActivity.this.changeToCitiesListTab();
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            }
        });
        ActionBar.Tab aboutTab = getSupportActionBar().newTab().setText(R.string.about_tab_title);
        aboutTab.setTabListener(new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                changeToAboutTab();
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }
        });
        getSupportActionBar().addTab(citiesTab);
        getSupportActionBar().addTab(aboutTab);
    }

    public void changeToAboutTab() {
        Fragment aboutFragment = new AboutFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, aboutFragment);
        fragmentTransaction.commit();
    }

    public void changeToCitiesListTab() {
        Fragment citiesListFragment = new CitiesListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, citiesListFragment);
        fragmentTransaction.commit();
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
        if (id==R.id.action_refresh) {
            CitiesListFragment citiesListFragment = (CitiesListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            citiesListFragment.loadData();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class CitiesListFragment extends Fragment implements Response.ErrorListener, Response.Listener<JSONArray> {
        private static final String LOG_TAG = CitiesListFragment.class.getSimpleName();
        private static final String LOCATION_URL = "http://www.axelia.us/api/Locations";
        @InjectView(R.id.location_list) ListView locationListView;
        private ProgressDialog progressDialog;

        public CitiesListFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            loadData();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, error.getMessage());
            }
        }

        @Override
        public void onResponse(JSONArray response) {
            if (progressDialog!=null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            List<Location> locations = new ArrayList<Location>();
            Type listType = new TypeToken<List<Location>>(){}.getType();
            Gson gson = new Gson();
            locations = gson.fromJson(response.toString(), listType);
            List<Location> availableLocations = new ArrayList<Location>();
            List<Location> comingSoonLocations = new ArrayList<Location>();
            for (Location location: locations) {
                if (location.getAlertMessage().equals("Próximamente")) {
                    comingSoonLocations.add(location);
                }
                else {
                    availableLocations.add(location);
                }
            }
            CitiesListAdapter availableLocationsAdapter = new CitiesListAdapter(availableLocations, getActivity());
            CitiesListAdapter comingSoonLocationsAdapter = new CitiesListAdapter(comingSoonLocations, getActivity());
            MergeAdapter mergeAdapter = new MergeAdapter();
            mergeAdapter.addView(header("Ciudades"));
            mergeAdapter.addAdapter(availableLocationsAdapter);
            mergeAdapter.addView(header("Próximamente"));
            mergeAdapter.addAdapter(comingSoonLocationsAdapter);
            locationListView.setAdapter(mergeAdapter);
        }

        private View header(String headerText) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.header_ciudades,null);
            TextView cityNameHeader = (TextView) view.findViewById(R.id.city_name_header);
            cityNameHeader.setText(headerText);
            return view;
        }

        private void loadData() {
            if (progressDialog==null) {
                progressDialog = ProgressDialog.show(this.getActivity(), null, "Loading.... Please Wait...");
            }
            VolleyQueue queue = VolleyQueue.getInstance(getActivity().getApplicationContext());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(LOCATION_URL, this, this);
            queue.getRequestQueue().add(jsonArrayRequest);
        }

        @Override
        public void onDetach() {
            if (progressDialog!=null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            super.onDetach();
        }
    }

    public static class AboutFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);
            return rootView;
        }
    }

}
