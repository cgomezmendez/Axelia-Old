package us.axelia.axelia;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 1/11/14.
 */
public class AudiosDownloaderTask extends AsyncTask<Integer, Float, List<Audio>>{
    private static final String BASE_URL = "http://192.168.1.5/axelia/api/Audios.php";
    private static final String LOG_TAG = AudiosDownloaderTask.class.getSimpleName();
    private static final String FILE_PREFIX = "Axelia_AUDIO";
    private static final String FILE_EXTENSION = ".mp3";
    private Context mContext;
    private List<AudioDownloadListener> mAudioDownloadListeners;

    public AudiosDownloaderTask(Context context) {
        mContext = context;
        mAudioDownloadListeners = new ArrayList<AudioDownloadListener>();
    }
    public void addAudioDownloadListener(AudioDownloadListener listener) {
        mAudioDownloadListeners.add(listener);
    }
    public void removeAudioDownloadListener(AudioDownloadListener listener) {
        mAudioDownloadListeners.remove(listener);
    }
    @Override
    protected List<Audio> doInBackground(Integer... integers) {
        String jsonResponse = downloadJsonList(integers[0]);
        List<Audio> mp3List = parseMp3List(jsonResponse);
        for (Audio audio: mp3List) {
            try {
                audio.setTemporaryFile(downloadMp3(audio.getUrl()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mp3List;
    }

    @Override
    protected void onPostExecute(List<Audio> audios) {
        super.onPostExecute(audios);
        for (AudioDownloadListener listener: mAudioDownloadListeners) {
            listener.onAudioDownloadComplete(audios);
        }
    }

    public String downloadJsonList(int id) {
        HttpClient client = new DefaultHttpClient();
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("http");
        uriBuilder.appendPath("api");
        uriBuilder.appendPath("Audios");
        uriBuilder.authority("www.axelia.us");
        uriBuilder.appendQueryParameter("locationId",String.valueOf(id));
        Uri uri = uriBuilder.build();
        HttpGet httpGet = new HttpGet(BASE_URL);
        httpGet.addHeader("Content-type","application/json");
        StringBuilder builder = new StringBuilder();
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) !=null) {
                    builder.append(line);
                }
            }
            else {
                Log.e(LOG_TAG, "failed to download jsonList");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, builder.toString());
        }
        return builder.toString();
    }

    public List<Audio> parseMp3List (String jsonResponse) {
        List<Audio> mp3List = new ArrayList<Audio>();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Audio>>(){}.getType();
        mp3List = gson.fromJson(jsonResponse, listType);
        return mp3List;
    }

    public File downloadMp3 (String url) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        File outputDir = mContext.getCacheDir();
        FileOutputStream outputStream = null;
        File outputFile = null;
        try {
            outputFile = File.createTempFile(FILE_PREFIX, FILE_EXTENSION, outputDir);
            outputStream = new FileOutputStream(outputFile);
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                long totalSize = entity.getContentLength();
                long downloadedSize = 0;
                byte[] buffer = new byte[1024];
                int bufferLength = 0;
                while ((bufferLength = content.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return outputFile;
    }

    public static interface AudioDownloadListener {
        public void onAudioDownloadComplete(List<Audio> audios);
    }
}
