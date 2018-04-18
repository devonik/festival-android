package devnik.trancefestivalticker.api;

import android.util.Log;

import org.greenrobot.greendao.query.Query;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.ObjectStreamField;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDao;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import devnik.trancefestivalticker.model.FestivalDetailImages;
import devnik.trancefestivalticker.model.FestivalDetailImagesDao;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalTicketPhaseDao;
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
    private FestivalTicketPhaseDao festivalTicketPhaseDao;
    private WhatsNewDao whatsNewDao;

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

        festivalTicketPhaseDao = daoSession.getFestivalTicketPhaseDao();
        whatsNewDao = daoSession.getWhatsNewDao();

        loadFestivals();
        loadFestivalDetails();
        loadFestivalDetailImages();

        //MusicGenre
        loadMusicGenre();
        loadMusicGenreFestivals();

        //TicketPhase
        loadFestivalTicketPhases();
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
        }
        catch (Exception e) {
            Log.e("loadFestivals()", e.getMessage(), e);
        }
    }
    public void updateSQLiteFestivals(final Festival[] festivals){

        try{
            //If no of array element is not zero
            if(festivals.length != 0){

                //Cheack Whats new
                for(final Festival remoteFestival : festivals){
                    final Festival localFestival = festivalDao.queryBuilder().where(FestivalDao.Properties.Festival_id.eq(remoteFestival.getFestival_id())).unique();
                    if(localFestival != null){
                        //Item exestiert bereits lokal
                        DiffNode diff = ObjectDifferBuilder.buildDefault().compare(remoteFestival, localFestival);
                        diff.visit(new DiffNode.Visitor()
                        {
                            public void node(DiffNode node, Visit visit)
                            {
                                final Object baseValue = node.canonicalGet(remoteFestival);
                                final Object workingValue = node.canonicalGet(localFestival);
                                WhatsNew whatsNew = new WhatsNew();
                                whatsNew.setItem(node.getPath() + " changed from \n" +
                                                 baseValue + " to " + workingValue);
                                whatsNew.setFestivalName(localFestival.getName());
                                whatsNewDao.insert(whatsNew);
                                final String message = node.getPath() + " changed from " +
                                        baseValue + " to " + workingValue;
                                System.out.println(message);
                            }
                        });

                        //festivalDao.update(remoteFestival);
                    }else{
                        //Festival is new
                        WhatsNew whatsNew = new WhatsNew();
                        whatsNew.setItem("Neu!");
                        whatsNew.setFestivalName(remoteFestival.getName());
                        whatsNewDao.insert(whatsNew);
                        //festivalDao.insert(remoteFestival);
                    }
                }



                festivalDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < festivals.length; i++) {

                    Festival remoteFestival = festivals[i];
                    this.festivalDao.insert(remoteFestival);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

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
            Log.e("loadFestivalDetails()", e.getMessage(), e);
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
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

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
            Log.e("loadDetailImages()", e.getMessage(), e);
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
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

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
            Log.e("loadMusicGenre()", e.getMessage(), e);
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
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //*******************************MusicGenreFestivals***************************************//
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
            Log.e("loadMusicGenreFe()", e.getMessage(), e);
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
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //*******************************FestivalTicketPhases***************************************//
    public void loadFestivalTicketPhases(){
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
    public void updateSQLiteFestivalTicketPhases(FestivalTicketPhase[] festivalTicketPhases){

        try{
            //If no of array element is not zero
            if(festivalTicketPhases.length != 0){
                festivalTicketPhaseDao.deleteAll();
                // Loop through each array element, get JSON object which has festival and username
                for (int i = 0; i < festivalTicketPhases.length; i++) {
                    FestivalTicketPhase festivalTicketPhase = festivalTicketPhases[i];
                    this.festivalTicketPhaseDao.insert(festivalTicketPhase);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
