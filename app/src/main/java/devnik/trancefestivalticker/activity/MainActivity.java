package devnik.trancefestivalticker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.adapter.IFilterableSection;
import devnik.trancefestivalticker.adapter.SectionAdapter;
import devnik.trancefestivalticker.helper.CustomExceptionHandler;
import devnik.trancefestivalticker.helper.MultiSelectionSpinner;
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
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class MainActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener{
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
    private DaoSession daoSession;
    private Menu menu;
    private Toolbar toolbar;
    private MultiSelectionSpinner multiSelectionSpinner;

    public static boolean isAppRunning = false;

    //Tour Guide
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private String preferenceUserNeedGuiding;
    public ChainTourGuide mTourGuideHandler;
    private Animation enterAnimation, exitAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //To force crashlytics Error
        //Crashlytics.getInstance().crash(); // Force a crash

        /* setup enter and exit animation */
        //For Touring
        enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(600);
        enterAnimation.setFillAfter(true);

        exitAnimation = new AlphaAnimation(1f, 0f);
        exitAnimation.setDuration(600);
        exitAnimation.setFillAfter(true);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        preferenceUserNeedGuiding = sharedPref.getString(getString(R.string.devnik_trancefestivalticker_preference_need_tour_guide), "yes");

        isAppRunning = true;
        //get the festival DAO
        daoSession = ((App)this.getApplication()).getDaoSession();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpView();

        festivalDao = daoSession.getFestivalDao();
        festivalQuery = festivalDao.queryBuilder().orderAsc(FestivalDao.Properties.Datum_start).build();

        festivalDetailDao = daoSession.getFestivalDetailDao();
        festivalDetails = festivalDetailDao.queryBuilder().build().list();

        festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();
        festivalDetailImages = festivalDetailImagesDao.queryBuilder().build().list();
        //whatsNewDao = daoSession.getWhatsNewDao();
        //whatsNewDao.insert(null);
        // query all festivals, sorted a-z by their text


        musicGenreDao = daoSession.getMusicGenreDao();
        musicGenres = musicGenreDao.queryBuilder().build().list();

        List<String> genreNames = new ArrayList<>();
        if(musicGenres!=null){
            for (MusicGenre item: musicGenres) {
                genreNames.add(item.getName());
            }

            multiSelectionSpinner = (MultiSelectionSpinner) findViewById(R.id.filter_spinner);
            MultiSelectionSpinner.placeholderText = "Filter by Music Genre ...";
            multiSelectionSpinner.setItems(genreNames);
            multiSelectionSpinner.setListener(this);

        }

        updateFestivalThumbnailView();


        //Register Custom Exception Handler
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        if(preferenceUserNeedGuiding.equals("yes")) {
            //Lister ist nötig, da der Guide erst anfangen darf, wenn der adapter fertig ist
            recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    recyclerView.removeOnLayoutChangeListener(this);
                    runOverlay_TourGuide();
                }
            });
        }
    }
    @Override
    protected void onResume()
    {
        super.onResume();

    }
    private void runOverlay_TourGuide(){
        //Scroll to top
        recyclerView.smoothScrollToPosition(0);
        Overlay overlay = new Overlay()
                .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                // Note: disable click has no effect when setOnClickListener is used, this is here for demo purpose
                // if setOnClickListener is not used, disableClick() will take effect
                .disableClick(false)
                .disableClickThroughHole(true)
                .setStyle(Overlay.Style.CIRCLE);
        // the return handler is used to manipulate the cleanup of all the tutorial elements
        ChainTourGuide tourGuide0 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Willkommen!")
                        .setDescription("Dies ist ein kurzer Guide über die Funktionen der Startseite")
                        .setGravity(Gravity.BOTTOM)
                )
                .setOverlay(overlay)
                .playLater(multiSelectionSpinner);

        ChainTourGuide tourGuide1 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Filtere die Festivals")
                        .setDescription("Hier kannst du die Festivals nach Music Genres filtern")
                        .setGravity(Gravity.BOTTOM)
                )
                .setOverlay(overlay)
                .playLater(multiSelectionSpinner);

        ChainTourGuide tourGuide2 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Langer Touch")
                        .setDescription("Wenn du ein Element mit einem Langen Touch berührst, siehst du eine kurze Info dazu.")
                        .setGravity(Gravity.BOTTOM | Gravity.RIGHT)
                )
                .setOverlay(overlay)
                .playLater(SectionAdapter.firstItem);

        ChainTourGuide tourGuide3 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Normaler Touch")
                        .setDescription("Wenn du ein Element kurz berührst öffnet sich eine Detail Ansicht. Dort findest du weitere Infos und Features zum Festival")
                        .setGravity(Gravity.BOTTOM | Gravity.LEFT)
                )
                .setOverlay(overlay)
                .playLater(SectionAdapter.secondItem);

        ChainTourGuide tourGuide4 = ChainTourGuide.init(this)
                .setToolTip(new ToolTip()
                        .setTitle("Hilfe")
                        .setDescription("Du kannst mich jederzeit erneut rufen, wenn du hilfe brauchst")
                        .setGravity(Gravity.BOTTOM | Gravity.LEFT)
                )
                .setOverlay(new Overlay()
                        .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                        // Note: disable click has no effect when setOnClickListener is used, this is here for demo purpose
                        // if setOnClickListener is not used, disableClick() will take effect
                        .disableClick(false)
                        .disableClickThroughHole(false)
                        .setStyle(Overlay.Style.CIRCLE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(preferenceUserNeedGuiding.equals("yes")){
                                    //Wenn der User das erste mal das Tutorial "geschafft" hat, wird es nun immer ausgeblendet
                                    //Shared Preference
                                    sharedPrefEditor.putString(getString(R.string.devnik_trancefestivalticker_preference_need_tour_guide), "no");
                                    sharedPrefEditor.commit();
                                }
                                mTourGuideHandler.next();
                            }
                        }))
                .playLater(toolbar.findViewById(R.id.action_show_tour_guide));

        Sequence sequence = new Sequence.SequenceBuilder()
                .add(tourGuide0, tourGuide1, tourGuide2, tourGuide3, tourGuide4)
                .setDefaultOverlay(new Overlay()
                        .setEnterAnimation(enterAnimation)
                        .setExitAnimation(exitAnimation)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTourGuideHandler.next();
                            }
                        })
                )
                .setDefaultPointer(null)
                .setContinueMethod(Sequence.ContinueMethod.OVERLAY_LISTENER)
                .build();


        mTourGuideHandler = ChainTourGuide.init(this).playInSequence(sequence);
    }

    @Override protected void onDestroy() { super.onDestroy(); isAppRunning = false; }
    public void updateFestivalThumbnailView(){
        //whatsNews = whatsNewDao.queryBuilder().build().list();
        festivals = festivalQuery.list();

        //If tests exists in SQLite DB
        if(festivals.size() > 0){

            ArrayList<Festival> festivalsInSection;

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);

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

        setSupportActionBar(toolbar);

    }
    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {
        for (Section section : sectionAdapter.getCopyOfSectionsMap().values()) {
            if (section instanceof IFilterableSection) {
                View v = SectionAdapter.firstItem;
                ((IFilterableSection) section).filter(strings);
            }
            sectionAdapter.notifyDataSetChanged();
        }
    }
    //Options Menu (ActionBar Menu)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }
    //When Options Menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Handle menu bar item clicks here
        switch (item.getItemId()){
            case R.id.action_show_tour_guide:
                runOverlay_TourGuide();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
