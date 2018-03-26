package devnik.trancefestivalticker.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.adapter.SectionAdapter;
import devnik.trancefestivalticker.helper.CustomExceptionHandler;
import devnik.trancefestivalticker.model.CustomDate;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDao;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import devnik.trancefestivalticker.model.FestivalDetailImages;
import devnik.trancefestivalticker.model.FestivalDetailImagesDao;
import devnik.trancefestivalticker.model.MusicGenre;
import devnik.trancefestivalticker.model.MusicGenreDao;
import devnik.trancefestivalticker.model.WhatsNew;
import devnik.trancefestivalticker.model.WhatsNewDao;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

import static devnik.trancefestivalticker.sync.SyncAdapter.getSyncAccount;

public class MainActivity extends AppCompatActivity{
    private List<Festival> festivals;
    private List<WhatsNew> whatsNews;
    private FestivalDao festivalDao;
    private FestivalDetailDao festivalDetailDao;
    private FestivalDetailImagesDao festivalDetailImagesDao;
    private MusicGenreDao musicGenreDao;
    private List<MusicGenre> musicGenres;
    private Query<Festival> festivalQuery;
    private List<FestivalDetail> festivalDetails;
    private List<FestivalDetailImages> festivalDetailImages;
    private WhatsNewDao whatsNewDao;
    private SectionedRecyclerViewAdapter sectionAdapter;
    private RecyclerView recyclerView;
    // Progress Dialog Object
    private ProgressDialog pDialog;
    private Account mAccount;
    private DaoSession daoSession;
    private Menu menu;
    public static boolean isAppRunning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        isAppRunning = true;
        //get the festival DAO
        daoSession = ((App)this.getApplication()).getDaoSession();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpView();


        festivalDao = daoSession.getFestivalDao();
        festivalDetailDao = daoSession.getFestivalDetailDao();
        festivalDetails = festivalDetailDao.queryBuilder().build().list();

        festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();
        festivalDetailImages = festivalDetailImagesDao.queryBuilder().build().list();
        //whatsNewDao = daoSession.getWhatsNewDao();
        //whatsNewDao.insert(null);
        // query all festivals, sorted a-z by their text
        festivalQuery = festivalDao.queryBuilder().orderAsc(FestivalDao.Properties.Datum_start).build();



        updateFestivalThumbnailView();
        //triggerRemoteSync();

        //Register Custom Exception Handler
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        mAccount = getSyncAccount(this);
    }
    @Override protected void onDestroy() { super.onDestroy(); isAppRunning = false; }
    public void updateFestivalThumbnailView(){
        //whatsNews = whatsNewDao.queryBuilder().build().list();
        festivals = festivalQuery.list();
        List<MusicGenre> musicGenres = festivals.get(7).getMusicGenres();
        Log.e("Test", "musicGenres: "+musicGenres);
        //If tests exists in SQLite DB
        if(festivals.size() > 0){

            ArrayList<Festival> festivalsInSection = new ArrayList<>();

            ArrayList<CustomDate> customDateHeader = new ArrayList<>();
            CustomDate customDate = new CustomDate();
            for(int i = 0; i<festivals.size();){
                Festival festival = festivals.get(i);
                String festivalMonth = (String) DateFormat.format("MMMM",festival.getDatum_start());
                String festivalYear = (String) DateFormat.format("yyyy", festival.getDatum_start());

                //Wenn es das erste Festival ist, und noch kein Header vorhanden ist
                if(customDateHeader.size()==0){

                    customDate.setMonth(festivalMonth);
                    customDate.setYear(festivalYear);
                    customDateHeader.add(customDate);
                    //Holet alle Items aus dem aktuellen Monat aus der SQLite Database
                    List<Festival> festivalsByMonth = getFestivalsByMonth(festival);
                    festivalsInSection = new ArrayList<>();

                    //Holt alle Items in dem Monat
                    for(Festival item : festivalsByMonth){
                        festivalsInSection.add(item);

                    }
                    i = festivalsByMonth.size();
                    sectionAdapter.addSection(new SectionAdapter(getApplicationContext(), customDate,festivalsInSection, getSupportFragmentManager()));
                }
                //Neue Section, wenn neuer Monat
                else if(!customDate.getMonth().equalsIgnoreCase(festivalMonth)){
                    customDate = new CustomDate();
                    customDate.setMonth(festivalMonth);
                    customDate.setYear(festivalYear);
                    customDateHeader.add(customDate);

                    List<Festival> festivalsByMonth = getFestivalsByMonth(festival);
                    festivalsInSection = new ArrayList<>();
                    for(Festival item : festivalsByMonth){
                        festivalsInSection.add(item);

                    }
                    i+=festivalsByMonth.size();
                    sectionAdapter.addSection(new SectionAdapter(getApplicationContext(), customDate,festivalsInSection, getSupportFragmentManager()));
                }
            }
        }
    }
    public List<Festival> getFestivalsByMonth(Festival festival){
        Date lastDayOfMonth = getLastDateOfMonth(festival.getDatum_start());
        Date firstDayOfMonth = getFirstDateOfMonth(festival.getDatum_start());

        Query festivalByMonth = festivalDao.queryBuilder().where(
                FestivalDao.Properties.Datum_start.between(firstDayOfMonth, lastDayOfMonth)
        ).build();

        return festivalByMonth.list();
    }
    public static Date getLastDateOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }
    public static Date getFirstDateOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }
    protected void setUpView(){
        //Set the List Array list in ListView
        // Create an instance of SectionedRecyclerViewAdapter
        sectionAdapter = new SectionedRecyclerViewAdapter();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (sectionAdapter.getSectionItemViewType(position)){
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return 2;
                    default:
                        return 1;
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(sectionAdapter);

        //Initialize Progress Dialog properties
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Transfering Data from Remote MySQL DB and Syncing SQLite. Please wait...");
        pDialog.setCancelable(false);

        //Brauch ich das noch???
        setSupportActionBar(toolbar);
    }
    //Options Menu (ActionBar Menu)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        //Add Music Genres to Filter
        musicGenreDao = daoSession.getMusicGenreDao();
        musicGenres = musicGenreDao.queryBuilder().build().list();
        for (MusicGenre item: musicGenres) {

            //groupFilterByMusicGenre.collapseActionView()
              menu.add(R.id.group_filter_by_music_genre, Menu.FLAG_PERFORM_NO_CLOSE, item.getId().intValue(), item.getName());

        }
        menu.setGroupCheckable(R.id.group_filter_by_music_genre,true,false);
        menu.setGroupEnabled(R.id.group_filter_by_music_genre, true);

        this.menu = menu;
        return true;
    }
    //When Options Menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Handle action bar item clicks here
        switch (item.getItemId()) {
            case R.id.refresh:
                pDialog.show();
                // Pass the settings flags by inserting them in a bundle
                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(
                        ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
                ContentResolver.requestSync(mAccount, getApplicationContext().getString(R.string.content_authority), settingsBundle);
                return true;
            case R.id.resetDb:
                festivalDao.deleteAll();
                festivalDetailDao.deleteAll();
                festivalDetailImagesDao.deleteAll();
                return true;
            case 1:
               // if (item.isChecked()) item.setChecked(false);
               // else item.setChecked(true);
                //return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    // Reload MainActivity
    public void reloadActivity() {
        //Intent objIntent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(getIntent());
    }
    public void updateUI(){
        Toast.makeText(this,"Want to update UI",Toast.LENGTH_LONG).show();
        updateFestivalThumbnailView();
    }
}
