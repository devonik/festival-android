package devnik.trancefestivalticker.activity.detail;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.greenrobot.greendao.query.Query;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.adapter.detail.PagerAdapter;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalVrView;
import devnik.trancefestivalticker.model.FestivalVrViewDao;
import devnik.trancefestivalticker.model.UserTicketsDao;


/**
 * Created by nik on 09.03.2018.
 */

public class DetailActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enablePermissions();

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        Bundle extras = getIntent().getExtras();

        assert extras != null;
        Festival festival = (Festival) extras.get("festival");
        FestivalTicketPhase actualFestivalTicketPhase = (FestivalTicketPhase) extras.get("actualFestivalTicketPhase");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        FestivalDetailDao festivalDetailDao = daoSession.getFestivalDetailDao();
        assert festival != null;
        Query<FestivalDetail> festivalDetailQuery = festivalDetailDao.queryBuilder().where(FestivalDetailDao.Properties.Festival_id.eq(festival.getFestival_id())).build();
        FestivalDetail festivalDetail = festivalDetailQuery.unique();

        FestivalVrViewDao festivalVrViewDao = daoSession.getFestivalVrViewDao();
        FestivalVrView photoVrView = festivalVrViewDao.queryBuilder().where(FestivalVrViewDao.Properties.FestivalDetailId.eq(festivalDetail.getFestival_detail_id()),FestivalVrViewDao.Properties.Type.eq("photo")).unique();
        FestivalVrView videoVrView = festivalVrViewDao.queryBuilder().where(FestivalVrViewDao.Properties.FestivalDetailId.eq(festivalDetail.getFestival_detail_id()),FestivalVrViewDao.Properties.Type.eq("video")).unique();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Info"));
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        if(photoVrView != null) {
            tabLayout.addTab(tabLayout.newTab().setText("360 Foto"));
        }
        if(videoVrView != null) {
            tabLayout.addTab(tabLayout.newTab().setText("360 Video"));
        }

        //Look for existing festival tickets
        UserTicketsDao userTicketsDao = daoSession.getUserTicketsDao();
        Long ticketCount = userTicketsDao.queryBuilder().where(UserTicketsDao.Properties.FestivalId.eq(festival.getFestival_id())).count();

        if(ticketCount > 0){
            tabLayout.addTab(tabLayout.newTab().setText("Tickets"));
        }else{
            tabLayout.addTab(tabLayout.newTab().setText("Import Tickets"));
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



    }
    public void enablePermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
        }
    }
}
