package devnik.trancefestivalticker.api;

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
import devnik.trancefestivalticker.model.FestivalDetailImages;
import devnik.trancefestivalticker.model.FestivalDetailImagesDao;
import devnik.trancefestivalticker.model.MusicGenre;
import devnik.trancefestivalticker.model.MusicGenreDao;
import devnik.trancefestivalticker.model.MusicGenreFestivals;
import devnik.trancefestivalticker.model.MusicGenreFestivalsDao;
import devnik.trancefestivalticker.model.WhatsNew;

/**
 * Created by niklas on 23.03.18.
 */

public class SyncAllData {
    private DaoSession daoSession;
    private FestivalDao festivalDao;
    private Query<Festival> festivalQuery;
    private List<Festival> localFestivals;

    private List<FestivalDetail> localFestivalDetails;
    private FestivalDetailDao festivalDetailDao;

    private List<FestivalDetailImages> localFestivalDetailImages;
    private FestivalDetailImagesDao festivalDetailImagesDao;

    private MusicGenreDao musicGenreDao;
    private MusicGenreFestivalsDao musicGenreFestivalsDao;

    public SyncAllData(DaoSession daoSession){
        this.daoSession = daoSession;
        festivalDao = daoSession.getFestivalDao();
        festivalQuery = festivalDao.queryBuilder().orderAsc(FestivalDao.Properties.Datum_start).build();
        localFestivals = festivalQuery.list();

        festivalDetailDao = daoSession.getFestivalDetailDao();
        localFestivalDetails = festivalDetailDao.queryBuilder().build().list();

        festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();
        localFestivalDetailImages = festivalDetailImagesDao.queryBuilder().build().list();

        musicGenreDao = daoSession.getMusicGenreDao();
        musicGenreFestivalsDao = daoSession.getMusicGenreFestivalsDao();

        loadFestivals();
        loadFestivalDetails();
        loadFestivalDetailImages();

        //MusicGenre
        loadMusicGenre();
        loadMusicGenreFestivals();
    }
    //***********************************Festivals****************************************//
    public void loadFestivals(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/festivals";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Festival[] festivals = restTemplate.getForObject(url, Festival[].class);
            if(festivals.length > 0){
                updateSQLiteFestivals(festivals);
            }
            //return festivals;
        }
        catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    public void updateSQLiteFestivals(Festival[] festivals){

        try{
            //If no of array element is not zero
            if(festivals.length != 0){
                festivalDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < festivals.length; i++) {
                    Festival remoteFestival = festivals[i];
                    this.festivalDao.insert(remoteFestival);
                    //updateLocalFestival(festival);
                    //updateMySQLSyncSts(festival);
                }
                //reloadActivity();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void updateLocalFestival(Festival unsyncfestival){
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


    //**********************************Festival Details*********************************************//
    public void loadFestivalDetails(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetails";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            FestivalDetail[] festivalDetails = restTemplate.getForObject(url, FestivalDetail[].class);
            if(festivalDetails.length>0){
                updateSQLiteDetails(festivalDetails);
            }

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    public void updateSQLiteDetails(FestivalDetail[] festivalDetails){

        try{
            //If no of array element is not zero
            if(festivalDetails.length != 0){
                festivalDetailDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < festivalDetails.length; i++) {
                    FestivalDetail festivalDetail = festivalDetails[i];
                    this.festivalDetailDao.insert(festivalDetail);
                    //updateLocalFestivalDetails(festivalDetail);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void updateLocalFestivalDetails(FestivalDetail unSyncFestivalDetail){
        Integer updatedCount = 0;
        Integer insertedCount = 0;
        for (FestivalDetail item: localFestivalDetails) {
            //Festival already exist in local sqlite, so it should be updated
            if(item.getFestival_detail_id() == unSyncFestivalDetail.getFestival_detail_id()){
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

    //*******************************FestivalDetailImages***************************************//
    public void loadFestivalDetailImages(){
        try {
             String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetailImages";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            FestivalDetailImages[] festivalDetailImages = restTemplate.getForObject(url, FestivalDetailImages[].class);
            if(festivalDetailImages.length>0){
                updateSQLiteDetailImages(festivalDetailImages);
            }

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    public void updateSQLiteDetailImages(FestivalDetailImages[] festivalDetailImages){

        try{
            //If no of array element is not zero
            if(festivalDetailImages.length != 0){
                festivalDetailImagesDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < festivalDetailImages.length; i++) {
                    FestivalDetailImages festivalDetailImage = festivalDetailImages[i];
                    this.festivalDetailImagesDao.insert(festivalDetailImage);
                    //updateLocalFestivalDetailImage(festivalDetailImage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //@TODO WhatsNew Maybe ?
    private void updateLocalFestivalDetailImage(FestivalDetailImages unSyncFestivalDetailImage){
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

    //*******************************MusicGenre***************************************//
    public void loadMusicGenre(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/musicGenre";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            MusicGenre[] musicGenres = restTemplate.getForObject(url, MusicGenre[].class);
            if(musicGenres.length>0){
                updateSQLiteMusicGenres(musicGenres);
            }

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    public void updateSQLiteMusicGenres(MusicGenre[] musicGenres){

        try{
            //If no of array element is not zero
            if(musicGenres.length != 0){
                musicGenreDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < musicGenres.length; i++) {
                    MusicGenre musicGenre = musicGenres[i];
                    this.musicGenreDao.insert(musicGenre);
                    //updateLocalFestivalDetailImage(festivalDetailImage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void loadMusicGenreFestivals(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/musicGenreFestivals";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            MusicGenreFestivals[] musicGenreFestivals = restTemplate.getForObject(url, MusicGenreFestivals[].class);
            if(musicGenreFestivals.length>0){
                updateSQLiteMusicGenreFestivals(musicGenreFestivals);
            }

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }
    }
    public void updateSQLiteMusicGenreFestivals(MusicGenreFestivals[] musicGenreFestivals){

        try{
            //If no of array element is not zero
            if(musicGenreFestivals.length != 0){
                musicGenreFestivalsDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < musicGenreFestivals.length; i++) {
                    MusicGenreFestivals musicGenreFestival = musicGenreFestivals[i];
                    this.musicGenreFestivalsDao.insert(musicGenreFestival);
                    //updateLocalFestivalDetailImage(festivalDetailImage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
