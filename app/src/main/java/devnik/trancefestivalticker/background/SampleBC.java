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
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import devnik.trancefestivalticker.model.Festival;

public class SampleBC extends BroadcastReceiver {
    static int noOfTimes = 0;
    private Context context;
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
        @Override
        protected Festival[] doInBackground(Void... params) {
            try {
                final String url = "https://festivalticker.herokuapp.com/api/v1/festivalsByUnSync";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Festival[] festivals = restTemplate.getForObject(url, Festival[].class);
                return festivals;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }
        @Override
        protected void onPostExecute(Festival[] result) {
            super.onPostExecute(result);
            if(result.length>0) {
                final Intent intnt = new Intent(context, MyService.class);
                // Set unsynced count in intent data
                intnt.putExtra("intntdata", "Anzahl der Änderungen: " + result.length);
                // Call MyService
                context.startService(intnt);

            }else{
                //Toast.makeText(context, "Sync not needed", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
