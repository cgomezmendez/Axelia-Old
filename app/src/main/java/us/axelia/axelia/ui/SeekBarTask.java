package us.axelia.axelia.ui;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.axelia.axelia.BuildConfig;
import us.axelia.axelia.R;

/**
 * Created by mac on 3/11/14.
 */
public class SeekBarTask extends TimerTask {
    private static final String LOG_TAG = SeekBarTask.class.getSimpleName();
    Fragment mContext;
    @InjectView(R.id.audio_seek_bar)
    SeekBar mSeekBar;

    public SeekBarTask(Fragment context, View view) {
        mContext = context;
        ButterKnife.inject(context, view);
    }

    @Override
    public void run() {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "updating seekbar...");
        }
    }
}
