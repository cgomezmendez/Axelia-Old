package us.axelia.axelia;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;


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
    public static class LocationDetailFragment extends Fragment implements AudiosDownloaderTask.AudioDownloadListener {
        private static final String LOG_TAG = LocationDetailFragment.class.getSimpleName();
        private static AudiosDownloaderTask downloaderTask;
        private ProgressDialog progressDialog;
        private MediaPlayer mMediaPlayer;

        public LocationDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_location_detail, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (savedInstanceState != null) {
                if (downloaderTask != null) {
                    downloaderTask.addAudioDownloadListener(this);
                }
            }
            loadData();

        }

        public void loadData() {
            if (downloaderTask==null) {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(getActivity(),null, "Loading.... Please Wait...");
                }
                downloaderTask = new AudiosDownloaderTask(getActivity());
                downloaderTask.addAudioDownloadListener(this);
                LocationDetailActivity activity = (LocationDetailActivity) getActivity();
                downloaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        activity.mCurrentLocation.getId());
            }
        }

        @Override
        public void onAudioDownloadComplete(List<File> files) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "files download complete");
            }
            downloaderTask = null;
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

        }

        @Override
        public void onDetach() {
            super.onDetach();
            downloaderTask.removeAudioDownloadListener(this);
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            if (mMediaPlayer != null ) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }
}
