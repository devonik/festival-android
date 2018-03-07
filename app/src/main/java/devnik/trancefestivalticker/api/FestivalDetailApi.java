package devnik.trancefestivalticker.api;

import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.greenrobot.greendao.query.Query;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDao;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import devnik.trancefestivalticker.model.WhatsNew;

/**
 * Created by nik on 07.03.2018.
 */

public class FestivalDetailApi extends AsyncTask<Void, Void, FestivalDetail[]> {
    private List<FestivalDetail> localFestivalDetails;
    private FestivalDetailDao festivalDetailDao;
    private Query<FestivalDetail> festivalDetailQuery;
    private Context context;
    @Override
    protected FestivalDetail[] doInBackground(Void... params) {
        try {
            final String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetailsByUnSync";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            FestivalDetail[] festivalDetails = restTemplate.getForObject(url, FestivalDetail[].class);
            updateSQLite(festivalDetails);
            return festivalDetails;
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }

        return null;
    }
    //Comes from background broadcast
    public FestivalDetailApi(Context context){
        this.context = context;
        //get the festival DAO
        DaoSession daoSession = ((App)this.context).getDaoSession();
        festivalDetailDao = daoSession.getFestivalDetailDao();
        //whatsNewDao = daoSession.getWhatsNewDao();
        // query all festivals, sorted a-z by their text
        festivalDetailQuery = festivalDetailDao.queryBuilder().build();
        localFestivalDetails = festivalDetailQuery.list();
    }
    public FestivalDetailApi(Context context, List<FestivalDetail> localFestivalDetailList){
        this.localFestivalDetails = localFestivalDetailList;
        DaoSession daoSession = ((App)context).getDaoSession();
        festivalDetailDao = daoSession.getFestivalDetailDao();
    }
    public void updateMySQLSyncSts(FestivalDetail festivalDetail){
        try {
            final String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetails/{id}";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            festivalDetail.setSyncStatus("yes");
            restTemplate.put(url,festivalDetail,festivalDetail.getFestival_detail_id());
        }catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    //Completion Handler
    @Override
    protected void onPostExecute(FestivalDetail[] festivalDetails) {
        //If the call comes from automatic Broadcast, there is no ProgressDialog
        /*if(progressDialog != null) {
            progressDialog.hide();
        }*/
    }
    public void updateSQLite(FestivalDetail[] festivalDetails){

        try{
            System.out.println(festivalDetails.length);
            //If no of array element is not zero
            if(festivalDetails.length != 0){
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < festivalDetails.length; i++) {
                    FestivalDetail festivalDetail = festivalDetails[i];
                    updateFestival(festivalDetail);
                    updateMySQLSyncSts(festivalDetail);
                }
                //reloadActivity();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void updateFestival(FestivalDetail unSyncFestivalDetail){
        Integer updatedCount = 0;
        Integer insertedCount = 0;
        for (FestivalDetail item: localFestivalDetails) {
            //Festival already exist in local sqlite, so it should be updated
            if(item.getFestival_id() == unSyncFestivalDetail.getFestival_detail_id()){
                updatedCount++;
                this.festivalDetailDao.update(unSyncFestivalDetail);

                Log.d("DaoFestival", "Update festival detail with ID: " + unSyncFestivalDetail.getFestival_detail_id());
                return;
            }
        }
        insertedCount++;
        this.festivalDetailDao.insert(unSyncFestivalDetail);
        WhatsNew whatsNew = new WhatsNew();
        whatsNew.setUpdatedFestivals("Es wurde/n "+updatedCount+" Festival/s aktualisiert");
        whatsNew.setInsertedFestivals("Es wurde/n "+insertedCount+" Festival/s hinzugefügt");

        Log.d("DaoFestival", "Inserted new festival detail, ID: " + unSyncFestivalDetail.getFestival_id());
    }
}
