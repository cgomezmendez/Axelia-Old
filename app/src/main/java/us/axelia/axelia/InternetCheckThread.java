package us.axelia.axelia;

import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mac on 3/11/14.
 */
public class InternetCheckThread implements Runnable {
    private static final String URL_TO_PING = "http://m.google.com";
    private boolean isConnectedToInternet;
    private Handler mHandler;
    public static final int IS_NOT_INTERNET_CONNECTION = 0;
    public static final int IS_INTERNET_CONNECTION = 1;
    private static final String LOG_TAG = InternetCheckThread.class.getSimpleName();

    public InternetCheckThread (Handler handler) {
        mHandler = handler;
    }

    @Override
    public void run() {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "checking internet connection");
        }
        HttpParams httpParams = new BasicHttpParams();
        int timeOut = 3000;
        HttpConnectionParams.setConnectionTimeout(httpParams, timeOut);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpGet httpGet = new HttpGet(URL_TO_PING);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                isConnectedToInternet = true;
            }
            else {

            }
            InputStream responseStream = response.getEntity().getContent();
            responseStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isConnectedToInternet) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "hay conexion a internet");
            }
            mHandler.sendEmptyMessage(IS_INTERNET_CONNECTION);
        }
        else {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "no hay conexion a internet");
            }
            mHandler.sendEmptyMessage(IS_NOT_INTERNET_CONNECTION);
        }
    }
}
