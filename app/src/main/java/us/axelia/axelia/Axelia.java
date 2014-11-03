package us.axelia.axelia;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by mac on 3/11/14.
 */
public class Axelia extends Application {
    private static Axelia instance;
    private static Context mContext;

    public Axelia() {

    }

    public static Axelia getInstance() {
        if (instance == null) {
            instance = new Axelia();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public synchronized Tracker getTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        Tracker t = analytics.newTracker(R.xml.global_tracker);
        return t;
    }
}
