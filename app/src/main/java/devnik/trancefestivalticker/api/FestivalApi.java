package devnik.trancefestivalticker.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.greenrobot.greendao.query.Query;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.activity.MainActivity;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDao;
import devnik.trancefestivalticker.model.WhatsNew;
import devnik.trancefestivalticker.model.WhatsNewDao;

/**
 * Created by nik on 05.03.2018.
 */

public class FestivalApi extends AsyncTask<Void, Void, Festival[]> {
    public interface FestivalApiCompleted {
        void onFestivalApiCompleted();
    }

        private FestivalApiCompleted onTaskCompleted;
        private FestivalDao festivalDao;
        private Query<Festival> festivalQuery;
        private WhatsNewDao whatsNewDao;
        private Query<WhatsNew> whatsNewQuery;
        private List<Festival> localFestivals;

        public FestivalApi(FestivalApiCompleted onTaskCompleted, DaoSession daoSession){
            this.onTaskCompleted = onTaskCompleted;
            festivalDao = daoSession.getFestivalDao();
            festivalQuery = festivalDao.queryBuilder().orderAsc(FestivalDao.Properties.Datum_start).build();
            localFestivals = festivalQuery.list();
        }
        @Override
        protected Festival[] doInBackground(Void... params) {
            try {
                String url = "https://festivalticker.herokuapp.com/api/v1/festivalsByUnSync";
                if(localFestivals.size()==0){
                    ////Lokale Daten wurden gelöscht oder noch nicht gesynct, bzw. es sind keine Einträge vorhanden.... Hole alle Festivals von remote
                    url = "https://festivalticker.herokuapp.com/api/v1/festivals";
                }
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Festival[] festivals = restTemplate.getForObject(url, Festival[].class);
                if(festivals.length > 0){
                    updateSQLite(festivals);
                }

                return festivals;
            }
            catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }
        public void updateMySQLSyncSts(Festival festival){
            try {
                final String url = "https://festivalticker.herokuapp.com/api/v1/festivals/{id}";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                festival.setSyncStatus("yes");
                restTemplate.put(url,festival,festival.getFestival_id());
            }catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
        }
        //Completion Handler
        @Override
        protected void onPostExecute(Festival[] festivals) {
            onTaskCompleted.onFestivalApiCompleted();
        }
        public void updateSQLite(Festival[] festivals){

            try{
                //If no of array element is not zero
                if(festivals.length != 0){
                    // Loop through each array element, get JSON object which has festival and username
                    for (int i = 0; i < festivals.length; i++) {
                        Festival festival = festivals[i];
                        updateFestival(festival);
                        updateMySQLSyncSts(festival);
                    }
                    //reloadActivity();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    private void updateFestival(Festival unsyncfestival){
        Integer updatedCount = 0;
        Integer insertedCount = 0;
        for (Festival item: localFestivals) {
            //Festival already exist in local sqlite, so it should be updated
            if(item.getFestival_id() == unsyncfestival.getFestival_id()){
                updatedCount++;
                this.festivalDao.update(unsyncfestival);

                Log.d("DaoFestival", "Update festival with ID: " + unsyncfestival.getFestival_id());
                return;
            }
        }
            insertedCount++;
            this.festivalDao.insert(unsyncfestival);
        WhatsNew whatsNew = new WhatsNew();
        whatsNew.setUpdatedFestivals("Es wurde/n "+updatedCount+" Festival/s aktualisiert");
        whatsNew.setInsertedFestivals("Es wurde/n "+insertedCount+" Festival/s hinzugefügt");

        Log.d("DaoFestival", "Inserted new festival, ID: " + unsyncfestival.getFestival_id());
    }






}
