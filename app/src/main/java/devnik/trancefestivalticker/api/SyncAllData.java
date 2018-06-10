package devnik.trancefestivalticker.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDao;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import devnik.trancefestivalticker.model.FestivalDetailImages;
import devnik.trancefestivalticker.model.FestivalDetailImagesDao;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalTicketPhaseDao;
import devnik.trancefestivalticker.model.FestivalVrView;
import devnik.trancefestivalticker.model.FestivalVrViewDao;
import devnik.trancefestivalticker.model.MusicGenre;
import devnik.trancefestivalticker.model.MusicGenreDao;
import devnik.trancefestivalticker.model.MusicGenreFestivals;
import devnik.trancefestivalticker.model.MusicGenreFestivalsDao;
import devnik.trancefestivalticker.model.WhatsNew;
import devnik.trancefestivalticker.model.WhatsNewDao;

/**
 * Created by niklas on 23.03.18.
 */

public class SyncAllData {

    private Context context;
    private FestivalDao festivalDao;

    private FestivalDetailDao festivalDetailDao;

    private FestivalDetailImagesDao festivalDetailImagesDao;

    private MusicGenreDao musicGenreDao;
    private MusicGenreFestivalsDao musicGenreFestivalsDao;
    private FestivalTicketPhaseDao festivalTicketPhaseDao;
    private WhatsNewDao whatsNewDao;
    private FestivalVrViewDao festivalVrViewDao;

    public SyncAllData(DaoSession daoSession, Context context){
        this.context = context;
        festivalDao = daoSession.getFestivalDao();

        festivalDetailDao = daoSession.getFestivalDetailDao();

        festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();

        musicGenreDao = daoSession.getMusicGenreDao();
        musicGenreFestivalsDao = daoSession.getMusicGenreFestivalsDao();

        festivalTicketPhaseDao = daoSession.getFestivalTicketPhaseDao();
        whatsNewDao = daoSession.getWhatsNewDao();
        festivalVrViewDao = daoSession.getFestivalVrViewDao();

        loadFestivals();
        loadFestivalDetails();
        loadFestivalDetailImages();

        //MusicGenre
        loadMusicGenre();
        loadMusicGenreFestivals();

        //TicketPhase
        loadFestivalTicketPhases();

        loadWhatsNew();
        loadVrViews();

    }
    //***********************************Festivals****************************************//
    private void loadFestivals(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/festivals";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Festival[] festivals = restTemplate.getForObject(url, Festival[].class);
            if(festivals.length > 0){
                updateSQLiteFestivals(festivals);
            }
        }
        catch (Exception e) {
            Log.e("loadFestivals()", e.getMessage(), e);
        }
    }
    private void updateSQLiteFestivals(final Festival[] festivals){

        try{
            //If no of array element is not zero
            if(festivals.length != 0){

                festivalDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (Festival remoteFestival : festivals) {

                    this.festivalDao.insert(remoteFestival);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //**********************************Festival Details*********************************************//
    private void loadFestivalDetails(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetails";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            FestivalDetail[] festivalDetails = restTemplate.getForObject(url, FestivalDetail[].class);
            if(festivalDetails.length>0){
                updateSQLiteDetails(festivalDetails);
            }

        } catch (Exception e) {
            Log.e("loadFestivalDetails()", e.getMessage(), e);
        }
    }
    private void updateSQLiteDetails(FestivalDetail[] festivalDetails){

        try{
            //If no of array element is not zero
            if(festivalDetails.length != 0){
                festivalDetailDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (FestivalDetail festivalDetail : festivalDetails) {
                    this.festivalDetailDao.insert(festivalDetail);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //*******************************FestivalDetailImages***************************************//
    private void loadFestivalDetailImages(){
        try {
             String url = "https://festivalticker.herokuapp.com/api/v1/festivalDetailImages";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            FestivalDetailImages[] festivalDetailImages = restTemplate.getForObject(url, FestivalDetailImages[].class);
            if(festivalDetailImages.length>0){
                updateSQLiteDetailImages(festivalDetailImages);
            }

        } catch (Exception e) {
            Log.e("loadDetailImages()", e.getMessage(), e);
        }
    }
    private void updateSQLiteDetailImages(FestivalDetailImages[] festivalDetailImages){

        try{
            //If no of array element is not zero
            if(festivalDetailImages.length != 0){
                festivalDetailImagesDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (FestivalDetailImages festivalDetailImage : festivalDetailImages) {
                    this.festivalDetailImagesDao.insert(festivalDetailImage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //*******************************MusicGenre***************************************//
    private void loadMusicGenre(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/musicGenre";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            MusicGenre[] musicGenres = restTemplate.getForObject(url, MusicGenre[].class);
            if(musicGenres.length>0){
                updateSQLiteMusicGenres(musicGenres);
            }

        } catch (Exception e) {
            Log.e("loadMusicGenre()", e.getMessage(), e);
        }
    }
    private void updateSQLiteMusicGenres(MusicGenre[] musicGenres){

        try{
            //If no of array element is not zero
            if(musicGenres.length != 0){
                musicGenreDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (MusicGenre musicGenre : musicGenres) {
                    this.musicGenreDao.insert(musicGenre);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //*******************************MusicGenreFestivals***************************************//
    private void loadMusicGenreFestivals(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/musicGenreFestivals";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            MusicGenreFestivals[] musicGenreFestivals = restTemplate.getForObject(url, MusicGenreFestivals[].class);
            if(musicGenreFestivals.length>0){
                updateSQLiteMusicGenreFestivals(musicGenreFestivals);
            }

        } catch (Exception e) {
            Log.e("loadMusicGenreFe()", e.getMessage(), e);
        }
    }
    private void updateSQLiteMusicGenreFestivals(MusicGenreFestivals[] musicGenreFestivals){

        try{
            //If no of array element is not zero
            if(musicGenreFestivals.length != 0){
                musicGenreFestivalsDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (MusicGenreFestivals musicGenreFestival : musicGenreFestivals) {
                    this.musicGenreFestivalsDao.insert(musicGenreFestival);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //*******************************FestivalTicketPhases***************************************//
    private void loadFestivalTicketPhases(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/ticketPhase";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            FestivalTicketPhase[] festivalTicketPhases = restTemplate.getForObject(url, FestivalTicketPhase[].class);
            if(festivalTicketPhases.length>0){
                updateSQLiteFestivalTicketPhases(festivalTicketPhases);
            }

        } catch (Exception e) {
            Log.e("loadTicketPhases()", e.getMessage(), e);
        }
    }
    private void updateSQLiteFestivalTicketPhases(FestivalTicketPhase[] festivalTicketPhases){

        try{
            //If no of array element is not zero
            if(festivalTicketPhases.length != 0){
                festivalTicketPhaseDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (FestivalTicketPhase festivalTicketPhase : festivalTicketPhases) {
                    this.festivalTicketPhaseDao.insert(festivalTicketPhase);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**************************************WhatsNew****************************************************/
    private void loadWhatsNew(){
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String preferenceLastOnline = sharedPref.getString(context.getString(R.string.devnik_trancefestivalticker_preference_last_sync), DateFormat.format("dd.MM.yyyy HH:mm:ss", new Date()).toString());
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String url = "https://festivalticker.herokuapp.com/api/v1/whatsNewBetweenDates?start="+
                    DateFormat.format("dd.MM.yyyy HH:mm:ss",format.parse(preferenceLastOnline))+
                    "&end="+
                    DateFormat.format("dd.MM.yyyy HH:mm:ss",new Date());
            Log.e("loadWhatsNewURL",url);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            WhatsNew[] whatsNews = restTemplate.getForObject(url, WhatsNew[].class);
            if(whatsNews.length>0){
                updateSQLiteWhatsNew(whatsNews);
            }

        } catch (Exception e) {
            Log.e("loadWhatsNew()", e.getMessage(), e);
        }
    }
    private void updateSQLiteWhatsNew(WhatsNew[] whatsNews){
        try{
            //If no of array element is not zero
            if(whatsNews.length != 0){
                whatsNewDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (WhatsNew whatsNew : whatsNews) {
                    this.whatsNewDao.insert(whatsNew);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**************************************VrView****************************************************/
    private void loadVrViews(){
        try {
            String url = "https://festivalticker.herokuapp.com/api/v1/vrView";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            FestivalVrView[] vrViews = restTemplate.getForObject(url, FestivalVrView[].class);
            if(vrViews.length>0){
                updateSQLiteVrViews(vrViews);
            }

        } catch (Exception e) {
            Log.e("loadVrViews()", e.getMessage(), e);
        }
    }
    private void updateSQLiteVrViews(FestivalVrView[] vrViews){
        try{
            //If no of array element is not zero
            if(vrViews.length != 0){
                festivalVrViewDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (FestivalVrView vrView : vrViews) {
                    this.festivalVrViewDao.insert(vrView);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
