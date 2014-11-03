package us.axelia.axelia;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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
        if (id == R.id.action_refresh) {
            LocationDetailFragment citiesListFragment = (LocationDetailFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            citiesListFragment.checkInternetConnection();
            citiesListFragment.destroyMediaPlayer();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LocationDetailFragment extends Fragment implements AudiosDownloaderTask.AudioDownloadListener {
        private static final String LOG_TAG = LocationDetailFragment.class.getSimpleName();
        private static final String FILE_PREFIX = "AXELIA_AUDIO";
        private static final String FILE_EXTENSION = ".mp3";
        private static AudiosDownloaderTask downloaderTask;
        private static MediaPlayer mMediaPlayer;
        private static List<Audio> mAudios;
        @InjectView(R.id.alert_message_view)
        TextView mAlertMessageView;
        @InjectView(R.id.alert_message_view_container)
        RelativeLayout mAlertMessageViewcContainer;
        @InjectView(R.id.play_or_pause_view)
        Button mPlayOrPauseButton;
        @InjectView(R.id.city_name)
        TextView cityNameView;
        @InjectView(R.id.description_view)
        TextView mDescriptionView;
        @InjectView(R.id.audio_seek_bar)
        SeekBar mSeekBar;
        @InjectView(R.id.current_time)
        TextView currentTime;
        @InjectView(R.id.duration_time)
        TextView durationTime;
        Timer timer = new Timer();
        private ProgressDialog progressDialog;
        private int mCurrentAudio = 0;
        private boolean mMediaPlayerPaused;
        private int mCurrentPosition = 0;
        private AdView mAdView;


        public LocationDetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_location_detail, container, false);
            ButterKnife.inject(this, rootView);
            LocationDetailActivity locationDetailActivity = (LocationDetailActivity) getActivity();
            String cityName = locationDetailActivity.mCurrentLocation.getName();
            cityNameView.setText(cityName);
            mSeekBar.setProgress(0);
            mSeekBar.setMax(100);
            durationTime.setText("");
            currentTime.setText("");
            setMessage();
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        if (!mAudios.isEmpty()) {
                            Audio currentAudio = mAudios.get(mCurrentAudio);
                            if (fromUser && currentAudio.getAudioType() != AudioType.COMMERCIAL) {
                                mMediaPlayer.seekTo(progress);
                            }
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            loadAd(rootView);
            track();
            return rootView;
        }

        public void track() {
            Axelia axelia = Axelia.getInstance();
            Tracker tracker = axelia.getTracker();
            LocationDetailActivity activity = (LocationDetailActivity) getActivity();
            String screenName = String.format("Audio Screen: %s",
                    activity.mCurrentLocation.getName());
            tracker.setScreenName(screenName);
            tracker.send(new HitBuilders.AppViewBuilder().build());
        }

        public void setupSeekBar(View view, final int amongToUpdate, final int duration) {
            long period = duration;
            mSeekBar.setMax(duration);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (mMediaPlayer != null) {
                        if (!mMediaPlayerPaused && mMediaPlayer.isPlaying()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int p = mMediaPlayer.getCurrentPosition();
                                    mSeekBar.setProgress(p);
                                    currentTime.setText(getTimeText(mMediaPlayer.getCurrentPosition()));
                                }
                            });
                        }
                    }
                }
            };
            timer.schedule(timerTask, 0, 1000);
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "update among: " + amongToUpdate);
            }
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (savedInstanceState != null) {
                if (downloaderTask != null) {
                    downloaderTask.addAudioDownloadListener(this);
                }
                mMediaPlayerPaused = savedInstanceState.getBoolean("paused", false);
                mCurrentPosition = savedInstanceState.getInt("currentPosition");
                if (mMediaPlayer == null) {
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
            } else {
                loadData();
            }
        }

        public void loadData() {
            mCurrentAudio = 0;
            if (getActivity() != null) {
                if (downloaderTask == null) {
                    if (progressDialog == null) {
                        progressDialog = ProgressDialog.show(getActivity(), null, "Loading.... Please Wait...");
                    }
                    downloaderTask = new AudiosDownloaderTask(getActivity());
                    downloaderTask.addAudioDownloadListener(this);
                    LocationDetailActivity activity = (LocationDetailActivity) getActivity();
                    downloaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            activity.mCurrentLocation.getId());
                }
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

        public File getAudio() throws IOException {
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
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                pauseMediaPlayer();
            }
            mPlayOrPauseButton.setText("Play");
            mDescriptionView.setText("");
            mSeekBar.setProgress(0);
            mSeekBar.setMax(100);
            currentTime.setText("");
            durationTime.setText("");
        }

        @Override
        public void onStop() {
            super.onStop();
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
            outState.putInt("currentPosition", mCurrentPosition);
            outState.putBoolean("paused", mMediaPlayerPaused);
        }


        public void prepareMediaPlayer() throws IOException {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, String.valueOf(mAudios.size()));
                if (mAudios.size() - 1 > 0) {
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
                    durationTime.setText("");
                    currentTime.setText("");
                    mSeekBar.setProgress(0);
                    mSeekBar.setMax(100);
                    nextAudio();
                    try {
                        mMediaPlayerPaused = false;
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(mAudios.get(mCurrentAudio).getTemporaryFile().getAbsolutePath());
                        if (mCurrentAudio != 0) {
                            mediaPlayer.prepareAsync();
                        } else {
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
                    durationTime.setText(getTimeText(mMediaPlayer.getDuration()));
                    mediaPlayer.start();
                    int duration = mMediaPlayer.getDuration();
                    int amongToUpdate = duration / 100;
                    setupSeekBar(getView(), amongToUpdate, duration);
                    mDescriptionView.setText(mAudios.get(mCurrentAudio).getDescription());
                }
            });
        }

        public void nextAudio() {
            if (mCurrentAudio < mAudios.size() - 1) {
                mCurrentAudio += 1;
            } else {
                mCurrentAudio = 0;
            }
        }

        public void trackPausa() {
            Axelia axelia = Axelia.getInstance();
            Tracker tracker = axelia.getTracker();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("UI")
                    .setAction("buttonPress")
                    .setLabel("Pausa")
                    .build());
        }

        public void trackPlay() {
            Axelia axelia = Axelia.getInstance();
            Tracker tracker = axelia.getTracker();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("UI")
                    .setAction("buttonPress")
                    .setLabel("Play")
                    .build());
        }

        @OnClick(R.id.play_or_pause_view)
        public void playOrPause() {

            mAlertMessageView.setSelected(true);
            mAlertMessageView.setEnabled(true);
            mAlertMessageView.setFocusable(true);
            if (!mAudios.isEmpty()) {
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        pauseMediaPlayer();
                        trackPausa();
                    } else if (mMediaPlayerPaused) {
                        resumeMediaPlayer();
                        trackPlay();
                    } else {
                        mPlayOrPauseButton.setText("Pausa");
                        mMediaPlayer.prepareAsync();
                        trackPlay();
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

        public void setMessage() {
            LocationDetailActivity activity = (LocationDetailActivity) getActivity();
            Location currentLocation = activity.mCurrentLocation;
            String message = currentLocation.getAlertMessage();
            if (!message.equals("No Hay Alertas") && !message.equals("Próximamente")) {
                mAlertMessageViewcContainer.setBackgroundColor(Color.rgb(235, 0, 0));
                mAlertMessageView.setSingleLine(true);
                mAlertMessageView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                mAlertMessageView.setMarqueeRepeatLimit(-1);
                mAlertMessageView.setHorizontallyScrolling(true);
                mAlertMessageView.setFocusable(true);
                mAlertMessageView.setFocusableInTouchMode(true);
            } else {
                mAlertMessageView.setGravity(Gravity.CENTER);
                if (message.equalsIgnoreCase("Próximamente")) {
                    mAlertMessageViewcContainer.setBackgroundColor(Color.rgb(235, 222, 0));
                } else if (message.equalsIgnoreCase("No Hay Alertas")) {
                    mAlertMessageViewcContainer.setBackgroundColor(Color.rgb(0, 214, 33));
                }
            }
            mAlertMessageView.setText(message);
        }


        public void destroyMediaPlayer() {
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;

            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            ButterKnife.reset(this);
            timer.cancel();
            destroyMediaPlayer();
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

        private String getTimeText(int time) {
            String timeString = String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(time),
                    TimeUnit.MILLISECONDS.toSeconds(time) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)
                            ));
            if (TimeUnit.MILLISECONDS.toSeconds(time) < 10 && TimeUnit.MILLISECONDS.toMinutes(time) < 10) {
                timeString = String.format("0%d:0%d",
                        TimeUnit.MILLISECONDS.toMinutes(time),
                        TimeUnit.MILLISECONDS.toSeconds(time) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)
                                ));
            } else if (TimeUnit.MILLISECONDS.toSeconds(time) < 10) {
                timeString = String.format("%d:0%d",
                        TimeUnit.MILLISECONDS.toMinutes(time),
                        TimeUnit.MILLISECONDS.toSeconds(time) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)
                                ));
            } else if (TimeUnit.MILLISECONDS.toMinutes(time) < 10) {
                timeString = String.format("0%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(time),
                        TimeUnit.MILLISECONDS.toSeconds(time) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)
                                ));
            }
            return timeString;
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
    }
}
