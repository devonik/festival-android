package devnik.trancefestivalticker.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.google.vr.sdk.widgets.video.VrVideoView;

import org.greenrobot.greendao.query.Query;

import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.adapter.PagerAdapter;
import devnik.trancefestivalticker.helper.CustomExceptionHandler;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalVrView;
import devnik.trancefestivalticker.model.FestivalVrViewDao;


/**
 * Created by nik on 09.03.2018.
 */

public class DetailActivity extends AppCompatActivity{
    private Festival festival;
    private FestivalTicketPhase actualFestivalTicketPhase;
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
        actualFestivalTicketPhase = (FestivalTicketPhase) extras.get("actualFestivalTicketPhase");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        festivalDetailDao = daoSession.getFestivalDetailDao();
        festivalDetailQuery = festivalDetailDao.queryBuilder().where(FestivalDetailDao.Properties.Festival_id.eq(festival.getFestival_id())).build();
        festivalDetail = festivalDetailQuery.unique();

        DaoSession daoSession = ((App)this.getApplication()).getDaoSession();
        FestivalVrViewDao festivalVrViewDao = daoSession.getFestivalVrViewDao();
        FestivalVrView photoVrView = festivalVrViewDao.queryBuilder().where(FestivalVrViewDao.Properties.FestivalDetailId.eq(festivalDetail.getFestival_detail_id()),FestivalVrViewDao.Properties.Type.eq("photo")).unique();
        FestivalVrView videoVrView = festivalVrViewDao.queryBuilder().where(FestivalVrViewDao.Properties.FestivalDetailId.eq(festivalDetail.getFestival_detail_id()),FestivalVrViewDao.Properties.Type.eq("video")).unique();

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Info"));
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        if(photoVrView != null) {
            tabLayout.addTab(tabLayout.newTab().setText("360 Foto"));
        }
        if(videoVrView != null) {
            tabLayout.addTab(tabLayout.newTab().setText("360 Video"));
        }
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), festival, festivalDetail, photoVrView, videoVrView, actualFestivalTicketPhase);

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
            public void onTabUnselected(TabLayout.Tab tab) {
                //2 = VRPanoView, 3 = VRVideoView
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        //Register Custom Exception Handler
        //Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
    }
}
