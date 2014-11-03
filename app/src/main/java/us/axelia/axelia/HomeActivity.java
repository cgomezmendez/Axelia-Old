package us.axelia.axelia;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.commonsware.cwac.merge.MergeAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class HomeActivity extends ActionBarActivity {
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    public boolean isConnectedToNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return networkInfo.isConnected();
        }
        return false;
    }

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            CitiesListFragment citiesListFragment = (CitiesListFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            citiesListFragment.checkInternetConnection();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class CitiesListFragment extends Fragment implements Response.ErrorListener, Response.Listener<JSONArray> {
        private static final String LOG_TAG = CitiesListFragment.class.getSimpleName();
        private static final String LOCATION_URL = "http://axelia.us/api/Locations";
        @InjectView(R.id.location_list)
        ListView locationListView;
        private ProgressDialog progressDialog;
        private List<Location> mLocations;
        private MergeAdapter mAdapter;
        private AdView mAdView;

        public CitiesListFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            checkInternetConnection();
        }

        public void displayNoInternetDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("No hay conexión a internet");
            builder.setMessage("Para poder usar esta aplicación, " +
                    "debe tener conexión a internet");
            builder.setIcon(R.drawable.ic_launcher);
            builder.setCancelable(false);
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    getActivity().finish();
                }
            });
            AlertDialog dialog = builder.show();
        }

        public void checkInternetConnection() {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "checking internet connection");
            }
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (getActivity() != null) {
                        if (msg.what == InternetCheckThread.IS_INTERNET_CONNECTION) {
                            loadData();
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.d(LOG_TAG, "No hay conexion a internet");
                            }
                            displayNoInternetDialog();
                        }
                    }
                }
            };
            Thread internetCheckThread = new Thread(new InternetCheckThread(handler));
            internetCheckThread.start();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            ButterKnife.inject(this, rootView);
            loadAd(rootView);
            track();
            return rootView;
        }

        public void track() {
            Axelia axelia = Axelia.getInstance();
            Tracker tracker = axelia.getTracker();
            tracker.setScreenName("Locations Screen");
            tracker.send(new HitBuilders.AppViewBuilder().build());
        }

        public void loadAd(View view) {
            LinearLayout placeholder = (LinearLayout) view.findViewById(R.id.adView);
            mAdView = new AdView(getActivity());
            mAdView.setAdSize(AdSize.SMART_BANNER);
            Random random = new Random();
            int randomInt = random.nextInt(101);
            String adCode = "";
            if (randomInt <= 30) {
                adCode = "ca-app-pub-7038667452523799/7558607661";
            } else {
                adCode = "ca-app-pub-7038667452523799/7558607661";
            }
            mAdView.setAdUnitId(adCode);
            placeholder.addView(mAdView);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mAdView.destroy();
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, error.getMessage());
            }
        }

        @Override
        public void onResponse(JSONArray response) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            mLocations = new ArrayList<Location>();
            Type listType = new TypeToken<List<Location>>() {
            }.getType();
            Gson gson = new Gson();
            mLocations = gson.fromJson(response.toString(), listType);
            List<Location> availableLocations = new ArrayList<Location>();
            List<Location> comingSoonLocations = new ArrayList<Location>();
            for (Location location : mLocations) {
                if (location.getAlertMessage().equals("Próximamente")) {
                    comingSoonLocations.add(location);
                } else {
                    availableLocations.add(location);
                }
            }
            CitiesListAdapter availableLocationsAdapter = new CitiesListAdapter(availableLocations, getActivity());
            CitiesListAdapter comingSoonLocationsAdapter = new CitiesListAdapter(comingSoonLocations, getActivity());
            mAdapter = new MergeAdapter();
            mAdapter.addView(header("Ciudades"));
            mAdapter.addAdapter(availableLocationsAdapter);
            mAdapter.addView(header("Próximamente"));
            mAdapter.addAdapter(comingSoonLocationsAdapter);
            locationListView.setAdapter(mAdapter);
        }

        private View header(String headerText) {
            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                View view = inflater.inflate(R.layout.header_ciudades, null);
                TextView cityNameHeader = (TextView) view.findViewById(R.id.city_name_header);
                cityNameHeader.setText(headerText);
                return view;
            } else {
                return null;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        private void loadData() {
            if (getActivity() != null) {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(getActivity(), null, "Loading.... Please Wait...");
                }
                VolleyQueue queue = VolleyQueue.getInstance(getActivity().getApplicationContext());
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(LOCATION_URL, this, this);
                queue.getRequestQueue().add(jsonArrayRequest);
            }
        }

        @Override
        public void onDetach() {
            super.onDetach();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            VolleyQueue.getInstance(getActivity()).getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            getActivity().getMenuInflater().inflate(R.menu.home, menu);
        }
    }

    public static class AboutFragment extends Fragment {
        @InjectView(R.id.about_list)
        ListView listView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);
            ButterKnife.inject(this, rootView);
            populateInfo();
            loadAd(rootView);
            track();
            return rootView;
        }

        public void track() {
            Axelia axelia = Axelia.getInstance();
            Tracker tracker = axelia.getTracker();
            tracker.setScreenName("About Screen");
            tracker.send(new HitBuilders.AppViewBuilder().build());
        }

        public void populateInfo() {
            AboutListAdapter mAdapter = new AboutListAdapter(getActivity());
            listView.setAdapter(mAdapter);
            mAdapter.addSeparatorItem("Axelia");
            mAdapter.addItem("Version 1.0.4 (build 674)\n" +
                    "Copyright(C) 2014 Axelia, Inc. All rights reserved");
            mAdapter.addSeparatorItem("About the App");
            mAdapter.addItem("Axelia is a well respected and trusted app that brings accurate real time traffic information and the details about the conditions of the roads and highways in your city and community. Our expert and well trained team of reporters combine decades of traffic reporting experience on many of the most influential Spanish radio stations in America.");
            mAdapter.addSeparatorItem("Contact Us");
            mAdapter.addItem("axeliatransito@gmail.com \n");
            mAdapter.addHtmlItem("Twitter: <a href=\"https://twitter.com/AxeliaTransito\">@AxeliaTransito</a>");
            mAdapter.addSeparatorItem("Advertise With Us");
            mAdapter.addItem("Please contact us for information on advertising and promotional opportunities on this app.Email us at axeliatransito@gmail.com.");
        }

        public void loadAd(View view) {
            LinearLayout placeholder = (LinearLayout) view.findViewById(R.id.adView);
            AdView adView = new AdView(getActivity());
            adView.setAdSize(AdSize.SMART_BANNER);
            Random random = new Random();
            int randomInt = random.nextInt(101);
            String adCode = "";
            if (randomInt <= 30) {
                adCode = "ca-app-pub-7038667452523799/7558607661";
            } else {
                adCode = "ca-app-pub-7038667452523799/7558607661";
            }
            adView.setAdUnitId(adCode);
            placeholder.addView(adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
        }

    }

}
