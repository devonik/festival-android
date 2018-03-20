package devnik.trancefestivalticker.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import devnik.trancefestivalticker.model.WhatsNew;
import devnik.trancefestivalticker.model.WhatsNewDao;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity{
    private List<Festival> festivals;
    private List<WhatsNew> whatsNews;
    private FestivalDao festivalDao;
    private FestivalDetailDao festivalDetailDao;
    private FestivalDetailImagesDao festivalDetailImagesDao;
    private Query<Festival> festivalQuery;
    private List<FestivalDetail> festivalDetails;
    private List<FestivalDetailImages> festivalDetailImages;
    private WhatsNewDao whatsNewDao;
    private SectionedRecyclerViewAdapter sectionAdapter;
    private RecyclerView recyclerView;
    // Progress Dialog Object
    private ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpView();

        //get the festival DAO
        DaoSession daoSession = ((App)this.getApplication()).getDaoSession();
        festivalDao = daoSession.getFestivalDao();
        festivalDetailDao = daoSession.getFestivalDetailDao();
        festivalDetails = festivalDetailDao.queryBuilder().build().list();

        festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();
        festivalDetailImages = festivalDetailImagesDao.queryBuilder().build().list();
        //whatsNewDao = daoSession.getWhatsNewDao();
        //whatsNewDao.insert(null);
        // query all festivals, sorted a-z by their text
        festivalQuery = festivalDao.queryBuilder().orderAsc(FestivalDao.Properties.Datum_start).build();

        festivals = festivalQuery.list();
        updateFestivalThumbnailView();
        //triggerRemoteSync();

        //Register Custom Exception Handler
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("festival");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("test","Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("test", "Failed to read value.", error.toException());
            }
        });
    }
    public void updateFestivalThumbnailView(){
        //whatsNews = whatsNewDao.queryBuilder().build().list();

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
        return true;
    }
    //When Options Menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Handle action bar item clicks here
        int id = item.getItemId();
        //When Sync action button is clicked
        if(id == R.id.refresh){
            pDialog.show();
            //triggerRemoteSync();
            reloadActivity();
            return true;
        }
        if(id == R.id.resetDb){
            festivalDao.deleteAll();
            festivalDetailDao.deleteAll();
            festivalDetailImagesDao.deleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void triggerRemoteSync(){
        if(isNetworkAvailable()) {
            //new FestivalApi(MainActivity.this,getApplicationContext(),pDialog,festivals).execute();
            //new FestivalDetailApi(getApplicationContext(), festivalDetails).execute();
            //new FestivalDetailImagesApi(getApplicationContext(), festivalDetailImages).execute();
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
}
