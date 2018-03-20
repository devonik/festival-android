package devnik.trancefestivalticker.api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.greenrobot.greendao.query.Query;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import devnik.trancefestivalticker.model.FestivalDetailImages;
import devnik.trancefestivalticker.model.FestivalDetailImagesDao;
import devnik.trancefestivalticker.model.WhatsNew;

/**
 * Created by nik on 07.03.2018.
 */

public class FestivalDetailImagesApi extends AsyncTask<Void,Void,FestivalDetailImages[]> {
    public interface FestivalDetailImagesApiCompleted {
        void onFestivalDetailImagesApiCompleted();
    }
    private FestivalDetailImagesApi.FestivalDetailImagesApiCompleted onTaskCompleted;
    private List<FestivalDetailImages> localFestivalDetailImages;
    private FestivalDetailImagesDao festivalDetailImagesDao;
    //Comes from MainActivity
    public FestivalDetailImagesApi(FestivalDetailImagesApiCompleted onTaskCompleted, DaoSession daoSession){
        this.onTaskCompleted = onTaskCompleted;
        festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();
        localFestivalDetailImages = festivalDetailImagesDao.queryBuilder().build().list();
    }
    @Override
    protected FestivalDetailImages[] doInBackground(Void... params) {
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetailImagesByUnSync";
            if(localFestivalDetailImages.size()==0){
                //Lokale Daten wurden gelöscht oder noch nicht gesynct, bzw. es sind keine Einträge vorhanden.... Hole alle Festival Detail Images von remote
                url = "https://festivalticker.herokuapp.com/api/v1/festivalDetailImages";
            }
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            FestivalDetailImages[] festivalDetailImages = restTemplate.getForObject(url, FestivalDetailImages[].class);
            if(festivalDetailImages.length>0){
                updateSQLite(festivalDetailImages);
            }

            return festivalDetailImages;
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }

        return null;
    }

    public void updateMySQLSyncSts(FestivalDetailImages festivalDetailImage){
        try {
            final String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetailImages/";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            festivalDetailImage.setSyncStatus("yes");
            restTemplate.postForObject(url,festivalDetailImage,FestivalDetailImages.class);
        }catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    //Completion Handler
    @Override
    protected void onPostExecute(FestivalDetailImages[] festivalDetailImages) {
        //If the call comes from automatic Broadcast, there is no ProgressDialog
        /*if(progressDialog != null) {
            progressDialog.hide();
        }*/
        onTaskCompleted.onFestivalDetailImagesApiCompleted();
    }
    public void updateSQLite(FestivalDetailImages[] festivalDetailImages){

        try{
            //If no of array element is not zero
            if(festivalDetailImages.length != 0){
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < festivalDetailImages.length; i++) {
                    FestivalDetailImages festivalDetailImage = festivalDetailImages[i];
                    updateFestival(festivalDetailImage);
                    updateMySQLSyncSts(festivalDetailImage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void updateFestival(FestivalDetailImages unSyncFestivalDetailImage){
        Integer updatedCount = 0;
        Integer insertedCount = 0;
        for (FestivalDetailImages item: localFestivalDetailImages) {
            //Festival already exist in local sqlite, so it should be updated
            if(item.getFestival_detail_images_id() == unSyncFestivalDetailImage.getFestival_detail_images_id()){
                updatedCount++;
                this.festivalDetailImagesDao.update(unSyncFestivalDetailImage);

                Log.d("DaoFestival", "Update festival detail images with ID: " + unSyncFestivalDetailImage.getFestival_detail_images_id());
                return;
            }
        }
        insertedCount++;
        this.festivalDetailImagesDao.insert(unSyncFestivalDetailImage);
        WhatsNew whatsNew = new WhatsNew();
        whatsNew.setUpdatedFestivals("Es wurde/n "+updatedCount+" Festival/s aktualisiert");
        whatsNew.setInsertedFestivals("Es wurde/n "+insertedCount+" Festival/s hinzugefügt");

        Log.d("DaoFestival", "Inserted new festival detail image, ID: " + unSyncFestivalDetailImage.getFestival_detail_images_id());
    }
}
