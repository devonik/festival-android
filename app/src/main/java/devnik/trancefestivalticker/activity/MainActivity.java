package devnik.trancefestivalticker.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kobakei.ratethisapp.RateThisApp;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.adapter.IFilterableSection;
import devnik.trancefestivalticker.adapter.SectionAdapter;
import devnik.trancefestivalticker.helper.MultiSelectionSpinner;
import devnik.trancefestivalticker.helper.UITagHandler;
import devnik.trancefestivalticker.model.AppInfo;
import devnik.trancefestivalticker.model.AppInfoDao;
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
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;

public class MainActivity extends AppCompatActivity implements MultiSelectionSpinner.OnMultipleItemsSelectedListener{
    private List<Festival> festivals;
    private List<WhatsNew> whatsNews;
    private FestivalDao festivalDao;
    private Query<Festival> festivalQuery;
    private SectionedRecyclerViewAdapter sectionAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private MultiSelectionSpinner multiSelectionSpinner;

    public static boolean isAppRunning = false;

    //Last Sync
    private String preferenceLastSync;

    public ChainTourGuide mTourGuideHandler;
    private Animation enterAnimation, exitAnimation;

    private AlertDialog.Builder builderDialogBuilder;

    //App Meta Data in DB
    private AppInfoDao appInfoDao;
    private AppInfo appInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaoSession daoSession = ((App) this.getApplication()).getDaoSession();

        //Tour Guide
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        appInfoDao = daoSession.getAppInfoDao();
        appInfo = appInfoDao.queryBuilder().where(AppInfoDao.Properties.Id.eq(1L)).build().unique();

        String preferenceUserNeedGuiding = sharedPref.getString(getString(R.string.devnik_trancefestivalticker_preference_need_tour_guide), "yes");
        //Whats New
        String getPreferenceWhatsNewDone = sharedPref.getString(getString(R.string.devnik_trancefestivalticker_preference_whats_new_done), "no");
        preferenceLastSync = appInfo.getLastSync();//sharedPref.getString(getString(R.string.devnik_trancefestivalticker_preference_last_sync), "Noch nicht gesynct");
        isAppRunning = true;
        /* setup enter and exit animation */
        //For Touring
        enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(600);
        enterAnimation.setFillAfter(true);

        exitAnimation = new AlphaAnimation(1f, 0f);
        exitAnimation.setDuration(600);
        exitAnimation.setFillAfter(true);

        setContentView(R.layout.activity_main);
        setUpView();

        festivalDao = daoSession.getFestivalDao();
        festivalQuery = festivalDao.queryBuilder().orderAsc(FestivalDao.Properties.Datum_start).build();

        FestivalDetailDao festivalDetailDao = daoSession.getFestivalDetailDao();
        List<FestivalDetail> festivalDetails = festivalDetailDao.queryBuilder().build().list();

        FestivalDetailImagesDao festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();
        List<FestivalDetailImages> festivalDetailImages = festivalDetailImagesDao.queryBuilder().build().list();
        //whatsNewDao = daoSession.getWhatsNewDao();
        //whatsNewDao.insert(null);
        // query all festivals, sorted a-z by their text

        MusicGenreDao musicGenreDao = daoSession.getMusicGenreDao();
        List<MusicGenre> musicGenres = musicGenreDao.queryBuilder().build().list();

        WhatsNewDao whatsNewDao = daoSession.getWhatsNewDao();
        whatsNews = whatsNewDao.queryBuilder().list();

        List<String> genreNames = new ArrayList<>();
        if(musicGenres !=null){
            for (MusicGenre item: musicGenres) {
                genreNames.add(item.getName());
            }

            multiSelectionSpinner = findViewById(R.id.filter_spinner);
            MultiSelectionSpinner.placeholderText = "Filter by Music Genre ...";
            multiSelectionSpinner.setItems(genreNames);
            multiSelectionSpinner.setListener(this);

        }

        updateFestivalThumbnailView();


        if(whatsNews.size() > 0 && appInfo.getWhatsNewDone().equals("no")){
            //Wenn Whats new einträge vorhanden sind
            initWhatsNewDialog();
        }

        //Rating App on Google Play
        RateThisApp.Config config = new RateThisApp.Config(7,5);
        config.setTitle(R.string.rating_title);
        config.setMessage(R.string.rating_message);
        config.setYesButtonText(R.string.rating_button_yes);
        config.setNoButtonText(R.string.rating_button_no);
        config.setCancelButtonText(R.string.rating_button_cancel);
        RateThisApp.init(config);
        // Monitor launch times and interval from installation
        RateThisApp.onCreate(this);
        // If the condition is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

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
                    festivalsInSection.addAll(festivalsByMonth);
                    i = festivalsByMonth.size();
                    sectionAdapter.addSection(new SectionAdapter(getApplicationContext(), customDate,festivalsInSection));
                }
                //Neue Section, wenn neuer Monat
                else if(!customDate.getMonth().equalsIgnoreCase(festivalMonth)){
                    customDate = new CustomDate();
                    customDate.setMonth(festivalMonth);
                    customDate.setYear(festivalYear);
                    customDateHeader.add(customDate);

                    List<Festival> festivalsByMonth = getFestivalsByMonth(festival);
                    festivalsInSection = new ArrayList<>();
                    festivalsInSection.addAll(festivalsByMonth);
                    i+=festivalsByMonth.size();
                    sectionAdapter.addSection(new SectionAdapter(getApplicationContext(), customDate,festivalsInSection));
                }
            }
        }
    }
    public List getFestivalsByMonth(Festival festival){
        Date lastDayOfMonth = getLastDateOfMonth(festival.getDatum_start());
        Date firstDayOfMonth = getFirstDateOfMonth(festival.getDatum_start());

        Query festivalByMonth = festivalDao.queryBuilder().where(
                FestivalDao.Properties.Datum_start.between(firstDayOfMonth,lastDayOfMonth)
        ).build();

        return festivalByMonth.list();
    }
    public static Date getLastDateOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }
    public static Date getFirstDateOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }
    protected void setUpView(){
        //Set the List Array list in ListView
        // Create an instance of SectionedRecyclerViewAdapter
        sectionAdapter = new SectionedRecyclerViewAdapter();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar = findViewById(R.id.toolbar);
        }

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


        setSupportActionBar(toolbar);

    }
    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {
        for (Section section : sectionAdapter.getCopyOfSectionsMap().values()) {
            if (section instanceof IFilterableSection) {
                ((IFilterableSection) section).filter(strings);
            }
            sectionAdapter.notifyDataSetChanged();
        }
    }
    //Options Menu (ActionBar Menu)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItemShowGuide = menu.findItem(R.id.action_show_tour_guide);
        if(appInfo.getFinishedTourGuide().equals("no")) {
            if(festivals.size()>0){
                runOverlay_TourGuide();
            }else{
                Toast.makeText(this,"Festivals konnten nicht geladen werden. Hast du Netz?",Toast.LENGTH_SHORT).show();
            }

        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }
    //When Options Menu is selected
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Handle menu bar item clicks here
        switch (item.getItemId()){
            case R.id.action_show_tour_guide:
                runOverlay_TourGuide();
                return true;
            case R.id.menu_policy:
                showPolicy();
                return true;
            case R.id.menu_credits:
                showCredits();
                return true;
            case R.id.menu_organizer:
                showOrganizerDialog();
                return true;
            case R.id.menu_faq:
                showFAQDialog();
                return true;
            case R.id.whats_new:
                initWhatsNewDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void runOverlay_TourGuide(){
        try {

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
                    .setOverlay(new Overlay()
                            .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                            // Note: disable click has no effect when setOnClickListener is used, this is here for demo purpose
                            // if setOnClickListener is not used, disableClick() will take effect
                            .disableClick(false)
                            .disableClickThroughHole(true)
                            .setStyle(Overlay.Style.ROUNDED_RECTANGLE))
                    .playLater(toolbar);

            ChainTourGuide tourGuide1 = ChainTourGuide.init(this)
                    .setToolTip(new ToolTip()
                            .setTitle("Filtere die Festivals")
                            .setDescription("Hier kannst du die Festivals nach Music Genres filtern")
                            .setGravity(Gravity.BOTTOM)
                    )
                    .setOverlay(new Overlay()
                            .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                            // Note: disable click has no effect when setOnClickListener is used, this is here for demo purpose
                            // if setOnClickListener is not used, disableClick() will take effect
                            .disableClick(false)
                            .disableClickThroughHole(true)
                            .setStyle(Overlay.Style.ROUNDED_RECTANGLE))
                    .playLater(multiSelectionSpinner);

            RecyclerView.ViewHolder firstFestivalViewHolder = recyclerView.findViewHolderForAdapterPosition(1);
            assert firstFestivalViewHolder != null;
            View firstFestivalView = firstFestivalViewHolder.itemView;

            ChainTourGuide tourGuide2 = ChainTourGuide.init(this)
                    .setToolTip(new ToolTip()
                            .setTitle("Langer Touch")
                            .setDescription("Wenn du ein Element mit einem langen Touch berührst, siehst du eine kurze Info dazu.")
                            .setGravity(Gravity.TOP | Gravity.END)
                    )
                    .setOverlay(overlay)
                    .playLater(firstFestivalView);

            int secondFestivalPosition = 2;
            RecyclerView.ViewHolder secondFestivalViewHolder = recyclerView.findViewHolderForAdapterPosition(secondFestivalPosition);
            assert secondFestivalViewHolder != null;
            View secondFestivalView = secondFestivalViewHolder.itemView;

                ChainTourGuide tourGuide3 = ChainTourGuide.init(this)
                        .setToolTip(new ToolTip()
                                .setTitle("Normaler Touch")
                                .setDescription("Wenn du ein Element kurz berührst öffnet sich eine Detail Ansicht. Dort findest du weitere Infos und Features zum Festival")
                                .setGravity(Gravity.TOP | Gravity.START)
                        )
                        .setOverlay(overlay)
                        .playLater(secondFestivalView);
            // View Type 6 = Header, 2 = Item
            if(secondFestivalViewHolder.getItemViewType() == 6){
                secondFestivalViewHolder = recyclerView.findViewHolderForAdapterPosition(secondFestivalPosition+1);
                secondFestivalView = secondFestivalViewHolder.itemView;

                tourGuide3 = ChainTourGuide.init(this)
                        .setToolTip(new ToolTip()
                                .setTitle("Normaler Touch")
                                .setDescription("Wenn du ein Element kurz berührst öffnet sich eine Detail Ansicht. Dort findest du weitere Infos und Features zum Festival")
                                .setGravity(Gravity.TOP | Gravity.END)
                        )
                        .setOverlay(overlay)
                        .playLater(secondFestivalView);
            }
            ChainTourGuide tourGuide4 = ChainTourGuide.init(this)
                    .setToolTip(new ToolTip()
                            .setTitle("Hilfe")
                            .setDescription("Du kannst mich jederzeit erneut rufen, wenn du hilfe brauchst")
                            .setGravity(Gravity.BOTTOM)
                    )
                    .setOverlay(new Overlay()
                            .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                            // Note: disable click has no effect when setOnClickListener is used, this is here for demo purpose
                            // if setOnClickListener is not used, disableClick() will take effect
                            .disableClick(false)
                            .disableClickThroughHole(true)
                            .setStyle(Overlay.Style.ROUNDED_RECTANGLE)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (appInfo.getFinishedTourGuide().equals("no")) {
                                        //Wenn der User das erste mal das Tutorial "geschafft" hat, wird es nun immer ausgeblendet
                                        //Shared Preference
                                        /*SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
                                        sharedPrefEditor.putString(getString(R.string.devnik_trancefestivalticker_preference_need_tour_guide), "no");
                                        sharedPrefEditor.apply();*/
                                        appInfo.setFinishedTourGuide("yes");
                                        appInfoDao.update(appInfo);
                                    }
                                    mTourGuideHandler.next();
                                }
                            }))
                    .playLater(toolbar);

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
        }catch(Exception ex){
            Log.e("Fehler im Tourguide:", "Folgender Fehler: "+ex);
        }
    }
    public void showFAQDialog(){
        builderDialogBuilder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        builderDialogBuilder.setTitle("FAQ - Häufig gestellte Fragen");
        TextView creditTextView = new TextView(this);
        creditTextView.setPadding(15,15,15,15);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            creditTextView.setText(Html.fromHtml(getString(R.string.faq_text), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));
        }else{
            creditTextView.setText(Html.fromHtml(getString(R.string.faq_text),null, new UITagHandler()));
        }
        //Important to make the hrefs clickable
        //creditTextView.setMovementMethod(LinkMovementMethod.getInstance());


        builderDialogBuilder.setView(creditTextView);
        // Set up the buttons
        builderDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setIcon(R.drawable.faq);

        builderDialogBuilder.create();
        builderDialogBuilder.show();
    }
    public void showOrganizerDialog(){
        builderDialogBuilder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        builderDialogBuilder.setTitle("Ihre Präsenzmöglichkeiten");
        TextView creditTextView = new TextView(this);
        creditTextView.setPadding(15,15,15,15);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            creditTextView.setText(Html.fromHtml(getString(R.string.organizer_text), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));
        }else{
            creditTextView.setText(Html.fromHtml(getString(R.string.organizer_text),null, new UITagHandler()));
        }
        //Important to make the hrefs clickable
        creditTextView.setMovementMethod(LinkMovementMethod.getInstance());


        builderDialogBuilder.setView(creditTextView);
        // Set up the buttons
        builderDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setIcon(R.drawable.organizer);
        builderDialogBuilder.create();
        builderDialogBuilder.show();
    }
    private void showPolicy(){

        builderDialogBuilder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);

        WebView wv = new WebView(this);
        wv.loadUrl(getString(R.string.policy_url));
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
            }
        });
        builderDialogBuilder.setView(wv);
        // Set up the buttons
        builderDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.paragraph);

        builderDialogBuilder.create();
        builderDialogBuilder.show();
    }
    private void showCredits(){

        builderDialogBuilder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        builderDialogBuilder.setTitle("Sonstiges");
        TextView creditTextView = new TextView(this);
        creditTextView.setPadding(15,15,15,15);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            creditTextView.setText(Html.fromHtml(getString(R.string.credits_text), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));
        }else{
            creditTextView.setText(Html.fromHtml(getString(R.string.credits_text),null, new UITagHandler()));
        }
        //Important to make the hrefs clickable
        creditTextView.setMovementMethod(LinkMovementMethod.getInstance());


        builderDialogBuilder.setView(creditTextView);
        // Set up the buttons
        builderDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setIcon(R.drawable.credits);
        builderDialogBuilder.create();
        builderDialogBuilder.show();
    }
    public void initWhatsNewDialog(){
        StringBuilder stringBuilder = new StringBuilder();
        if(whatsNews.size() == 0){
            stringBuilder.append("Es sind keine Informationen vorhanden!");
        }else{
            for(WhatsNew item : whatsNews){
                stringBuilder.append(item.getContent())
                             .append("\n\n");
            }
        }
        TextView textView = new TextView(this);
        textView.setText("Letzte Sync: "+preferenceLastSync);
        textView.setTextSize(12);
        textView.setGravity(Gravity.END);

        builderDialogBuilder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        builderDialogBuilder.setTitle("News")
                .setMessage("Letzte Änderungen:\n\n "+stringBuilder)
                .setView(textView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if (appInfo.getWhatsNewDone().equals("no")) {
                            //Wenn der User das erste mal das Tutorial "geschafft" hat, wird es nun immer ausgeblendet
                            //Shared Preference
                            /*SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
                            sharedPrefEditor.putString(getString(R.string.devnik_trancefestivalticker_preference_whats_new_done), "yes");
                            sharedPrefEditor.apply();*/
                            appInfo.setWhatsNewDone("yes");
                            appInfoDao.update(appInfo);
                        }
                        dialog.cancel();
                    }
                })
                .setIcon(R.drawable.news);

        builderDialogBuilder.create();
        builderDialogBuilder.show();
    }
}
