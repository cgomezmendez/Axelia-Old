package us.axelia.axelia;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class LocationDetailActivity extends ActionBarActivity {
    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LocationDetailFragment())
                    .commit();
        }
        int id = getIntent().getExtras().getInt("id");
        String name = getIntent().getExtras().getString("name");
        String alertMessage = getIntent().getExtras().getString("alertMessage");
        mCurrentLocation = new Location();
        mCurrentLocation.setId(id);
        mCurrentLocation.setName(name);
        mCurrentLocation.setAlertMessage(alertMessage);
        getSupportActionBar().setSubtitle(mCurrentLocation.getName());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LocationDetailFragment extends Fragment {

        public LocationDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_location_detail, container, false);
            return rootView;
        }
    }
}
