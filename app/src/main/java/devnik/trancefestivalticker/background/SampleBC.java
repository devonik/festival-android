package devnik.trancefestivalticker.background;

/**
 * Created by nik on 02.03.2018.
 */
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import devnik.trancefestivalticker.api.FestivalApi;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailImages;

public class SampleBC extends BroadcastReceiver {
    static int noOfTimes = 0;
    private Context context;
    private FestivalApi festivalApi;
    // Method gets called when Broad Case is issued from MainActivity for every 10 seconds
    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub
        noOfTimes++;
        this.context = context;
        //Toast.makeText(context, "BC Service Running for " + noOfTimes + " times", Toast.LENGTH_SHORT).show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        new HttpRequestTask().execute();


    }
    private class HttpRequestTask extends AsyncTask<Void, Void, Festival[]> {
        private FestivalDetail[] festivalDetails;
        private FestivalDetailImages[] festivalDetailImages;
        @Override
        protected Festival[] doInBackground(Void... params) {
            try {
                final String url = "https://festivalticker.herokuapp.com/api/v1/festivalsByUnSync";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Festival[] festivals = restTemplate.getForObject(url, Festival[].class);
                checkFestivalDetails();
                checkFestivalDetailImages();
                return festivals;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }
        protected void checkFestivalDetails(){
            try {
                final String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetailsByUnSync";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                festivalDetails = restTemplate.getForObject(url, FestivalDetail[].class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
        }
        protected void checkFestivalDetailImages(){
            try {
                final String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetailImagesByUnSync";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                festivalDetailImages = restTemplate.getForObject(url, FestivalDetailImages[].class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
        }
        @Override
        protected void onPostExecute(Festival[] result) {
            super.onPostExecute(result);
            if(result.length>0 || festivalDetails.length>0||festivalDetailImages.length>0) {
                final Intent intnt = new Intent(context, MyService.class);
                // Set unsynced count in intent data
                intnt.putExtra("intntdata", "Anzahl der Änderungen: " + (result.length+festivalDetails.length+festivalDetailImages.length));
                //intnt.putExtra("updatedFestivals",festivalApi.updatedFestivals);
                // Call MyService
                //if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                 //   context.startForegroundService(intnt);
                //} else {
                    context.startService(intnt);
                //}

            }else{
                //Toast.makeText(context, "Sync not needed", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
