package devnik.trancefestivalticker.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import devnik.trancefestivalticker.api.SyncTicketPhases;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalTicketPhaseDao;

/**
 * Created by niklas on 23.03.18.
 */

public class GetBitmapFromURLAsync extends AsyncTask<Void, Void, Bitmap> {
    public interface GetBitmapFromURLCompleted {
        void onGetBitmapFromURLCompleted(Bitmap bitmap);
    }
    private GetBitmapFromURLCompleted onGetBitmapFromURLCompleted;
    private String source;

    public GetBitmapFromURLAsync(String source, GetBitmapFromURLCompleted onGetBitmapFromURLCompleted){
        this.source = source;
        this.onGetBitmapFromURLCompleted = onGetBitmapFromURLCompleted;
    }
    @Override
    protected Bitmap doInBackground(Void... params) {
            try {
                URL url = new URL(source);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                return BitmapFactory.decodeStream(input);
            } catch (Exception ex) {
                Log.e("getBitmapFromUrl", "Error: " + ex);
                return null;
            }
    }

    //Completion Handler
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Log.e("getBitmapFromUrl", "finished");
        onGetBitmapFromURLCompleted.onGetBitmapFromURLCompleted(bitmap);
    }
}
