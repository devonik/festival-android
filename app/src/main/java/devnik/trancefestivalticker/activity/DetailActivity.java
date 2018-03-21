package devnik.trancefestivalticker.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import org.greenrobot.greendao.query.Query;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.adapter.PagerAdapter;
import devnik.trancefestivalticker.api.FestivalDetailApi;
import devnik.trancefestivalticker.helper.CustomExceptionHandler;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;


/**
 * Created by nik on 09.03.2018.
 */

public class DetailActivity extends AppCompatActivity implements FestivalDetailApi.FestivalDetailApiCompleted {
    private Festival festival;
    private FestivalDetailDao festivalDetailDao;
    private Query<FestivalDetail> festivalDetailQuery;
    private DaoSession daoSession;
    private FestivalDetail festivalDetail;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        daoSession = ((App)getApplication()).getDaoSession();
        Bundle extras = getIntent().getExtras();

        festival = (Festival) extras.get("festival");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        //setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Info"));
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        festivalDetailDao = daoSession.getFestivalDetailDao();
        festivalDetailQuery = festivalDetailDao.queryBuilder().where(FestivalDetailDao.Properties.Festival_id.eq(festival.getFestival_id())).build();
        festivalDetail = festivalDetailQuery.unique();


        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), festival, festivalDetail);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // This method ensures that tab selection events update the ViewPager and page changes update the selected tab.
        // tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        //Register Custom Exception Handler
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
    }
    @Override
    public void onFestivalDetailApiCompleted(){
        Toast.makeText(this,"Task Completed",Toast.LENGTH_SHORT);


    }
}
