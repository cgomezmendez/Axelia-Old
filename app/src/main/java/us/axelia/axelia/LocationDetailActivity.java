package us.axelia.axelia;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


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
        if (id==R.id.action_refresh) {
            LocationDetailFragment citiesListFragment = (LocationDetailFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            citiesListFragment.loadData();
            citiesListFragment.destroyMediaPlayer();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LocationDetailFragment extends Fragment implements AudiosDownloaderTask.AudioDownloadListener {
        private static final String LOG_TAG = LocationDetailFragment.class.getSimpleName();
        private static AudiosDownloaderTask downloaderTask;
        private ProgressDialog progressDialog;
        private static MediaPlayer mMediaPlayer;
        private static List<Audio> mAudios;
        @InjectView(R.id.alert_message_view)
        TextView mAlertMessageView;
        @InjectView(R.id.alert_message_view_container)
        RelativeLayout mAlertMessageViewcContainer;
        private static final String FILE_PREFIX = "AXELIA_AUDIO";
        private static final String FILE_EXTENSION = ".mp3";
        private int mCurrentAudio = 0;
        private boolean mMediaPlayerPaused;
        private int mCurrentPosition = 0;
        @InjectView(R.id.play_or_pause_view)
        Button mPlayOrPauseButton;
        @InjectView(R.id.city_name)
        TextView cityNameView;
        @InjectView(R.id.description_view)
        TextView mDescriptionView;

        public LocationDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_location_detail, container, false);
            ButterKnife.inject(this, rootView);
            LocationDetailActivity locationDetailActivity =  (LocationDetailActivity) getActivity();
            String cityName = locationDetailActivity.mCurrentLocation.getName();
            cityNameView.setText(cityName);
            setMessage();
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (savedInstanceState != null) {
                if (downloaderTask != null) {
                    downloaderTask.addAudioDownloadListener(this);
                }
                mMediaPlayerPaused = savedInstanceState.getBoolean("paused",false);
                mCurrentPosition = savedInstanceState.getInt("currentPosition");
                if (mMediaPlayer==null) {
                    mMediaPlayer = new MediaPlayer();
                    try {
                        prepareMediaPlayer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                attachMediaPlayerToListeners();
                if (mMediaPlayerPaused) {
                    mPlayOrPauseButton.setText("Play");
                }
            }
            else{
                loadData();
            }
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
        public void onAudioDownloadComplete(List<Audio> audios) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "files download complete");
            }
            downloaderTask = null;
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            mAudios = audios;
            try {
                File audioFile = getAudio();
                if (audioFile != null) {
                    Audio audioN = new Audio();
                    audioN.setTemporaryFile(audioFile);
                    audios.add(audioN);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                prepareMediaPlayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public File getAudio () throws IOException {
            File file = null;
            if (mAudios.isEmpty()) {
                LocationDetailActivity locationDetailActivity = (LocationDetailActivity) getActivity();
                String message = locationDetailActivity.mCurrentLocation.getAlertMessage();
                Random random = new Random();
                int randomInt = random.nextInt(4);
                if (message.equals("No Hay Alertas")) {
                    File outputDir = getActivity().getCacheDir();
                    File outputFile = File.createTempFile(FILE_PREFIX, FILE_EXTENSION, outputDir);
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    InputStream inputStream = null;
                    switch (randomInt) {
                        case 0:
                            inputStream = getResources().openRawResource(R.raw.noalerts_1);
                            break;
                        case 1:
                            inputStream = getResources().openRawResource(R.raw.noalerts_2);
                            break;
                        case 2:
                            inputStream = getResources().openRawResource(R.raw.noalerts_3);
                            break;
                        case 3:
                            inputStream = getResources().openRawResource(R.raw.noalerts_4);
                            break;
                    }
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes);
                    }
                    outputStream.close();
                    file = outputFile;
                } else if (message.equals("Próximamente")) {
                    File outputDir = getActivity().getCacheDir();
                    File outputFile = File.createTempFile(FILE_PREFIX, FILE_EXTENSION, outputDir);
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    InputStream inputStream = null;
                    switch (randomInt) {
                        case 0:
                            inputStream = getResources().openRawResource(R.raw.comingsoon_1);
                            break;
                        case 1:
                            inputStream = getResources().openRawResource(R.raw.comingsoon_2);
                            break;
                        case 2:
                            inputStream = getResources().openRawResource(R.raw.comingsoon_3);
                            break;
                        case 3:
                            inputStream = getResources().openRawResource(R.raw.comingsoon_4);
                            break;
                    }
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes);
                    }
                    outputStream.close();
                    file = outputFile;
                }
            }
            return file;
        }

        @Override
        public void onPause() {
            super.onPause();
            pauseMediaPlayer();
        }

        @Override
        public void onDetach() {
            super.onDetach();
            if (downloaderTask != null) {
                downloaderTask.removeAudioDownloadListener(this);
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("currentPosition",mCurrentPosition);
            outState.putBoolean("paused",mMediaPlayerPaused);
        }


        public void prepareMediaPlayer() throws IOException {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, String.valueOf(mAudios.size()));
                if (mAudios.size()-1>0) {
                    Log.d(LOG_TAG, mAudios.get(0).getTemporaryFile().getAbsolutePath());
                }
            }
            mMediaPlayer = new MediaPlayer();
            if (!mAudios.isEmpty()) {
                mMediaPlayer.setDataSource(mAudios.get(mCurrentAudio).getTemporaryFile().getAbsolutePath());
                attachMediaPlayerToListeners();
            }

        }

        public void attachMediaPlayerToListeners() {
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mDescriptionView.setText("");
                    nextAudio();
                    try {
                        mMediaPlayerPaused = false;
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(mAudios.get(mCurrentAudio).getTemporaryFile().getAbsolutePath());
                        if (mCurrentAudio!=0) {
                            mediaPlayer.prepareAsync();
                        }
                        else{
                            mPlayOrPauseButton.setText("Play");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    mDescriptionView.setText(mAudios.get(mCurrentAudio).getDescription());
                }
            });
        }

        public void nextAudio () {
            if (mCurrentAudio < mAudios.size()-1) {
                mCurrentAudio += 1;
            }
            else {
                mCurrentAudio = 0;
            }
        }

        @OnClick(R.id.play_or_pause_view)
        public void playOrPause () {
            if (!mAudios.isEmpty()) {
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        pauseMediaPlayer();
                    } else if (mMediaPlayerPaused) {
                        resumeMediaPlayer();
                    } else {
                        mPlayOrPauseButton.setText("Pausa");
                        mMediaPlayer.prepareAsync();
                    }
                }
            }
        }

        public void resumeMediaPlayer() {
            mMediaPlayer.seekTo(mCurrentPosition);
            mCurrentPosition = 0;
            mMediaPlayerPaused = false;
            mMediaPlayer.start();
            mPlayOrPauseButton.setText("Pausa");
        }

        public void pauseMediaPlayer() {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
                mCurrentPosition = mMediaPlayer.getCurrentPosition();
                mMediaPlayerPaused = true;
                mPlayOrPauseButton.setText("Play");
            }
        }
        public void playMediaPlayer() {
            mMediaPlayer.prepareAsync();
            mPlayOrPauseButton.setText("Pause");
        }

        public void setMessage () {
            LocationDetailActivity activity = (LocationDetailActivity) getActivity();
            Location currentLocation = activity.mCurrentLocation;
            String message = currentLocation.getAlertMessage();
            if (!message.equals("No Hay Alertas") &&  !message.equals("Próximamente")) {
                mAlertMessageViewcContainer.setBackgroundColor(Color.rgb(235,0,0));
                mAlertMessageView.setSingleLine(true);
                mAlertMessageView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                mAlertMessageView.setMarqueeRepeatLimit(-1);
                mAlertMessageView.setHorizontallyScrolling(true);
                mAlertMessageView.setFocusable(true);
                mAlertMessageView.setFocusableInTouchMode(true);
            }
            else{
                mAlertMessageView.setGravity(Gravity.CENTER);
                if (message.equalsIgnoreCase("Próximamente")) {
                    mAlertMessageViewcContainer.setBackgroundColor(Color.rgb(235,222,0));
                }
                else if (message.equalsIgnoreCase("No Hay Alertas")) {
                    mAlertMessageViewcContainer.setBackgroundColor(Color.rgb(0,214, 33));
                }
            }
            mAlertMessageView.setText(message);
        }



        public void destroyMediaPlayer() {
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
                mMediaPlayerPaused = false;
                mPlayOrPauseButton.setText("Play");
                mDescriptionView.setText("");
            }
        }

    }
}
