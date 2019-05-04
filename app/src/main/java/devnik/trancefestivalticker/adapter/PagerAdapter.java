package devnik.trancefestivalticker.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import devnik.trancefestivalticker.activity.detail.FestivalDetailFragment;
import devnik.trancefestivalticker.activity.detail.MapFragmentDialog;
import devnik.trancefestivalticker.activity.detail.TicketFragmentDialog;
import devnik.trancefestivalticker.activity.detail.VRPanoView;
import devnik.trancefestivalticker.activity.detail.VRVideoView;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalVrView;

/**
 * Created by nik on 09.03.2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int numTabs;
    private Festival festival;
    private FestivalDetail festivalDetail;
    private FestivalTicketPhase actualFestivalTicketPhase;
    private FestivalVrView photoVrView;
    private FestivalVrView videoVrView;

    public PagerAdapter(FragmentManager fm, int numTabs, Festival festival, FestivalDetail festivalDetail, FestivalVrView photoVrView, FestivalVrView videoVrView, FestivalTicketPhase actualFestivalTicketPhase) {
        super(fm);
        this.numTabs = numTabs;
        this.festival = festival;
        this.festivalDetail = festivalDetail;
        this.actualFestivalTicketPhase = actualFestivalTicketPhase;
        this.photoVrView = photoVrView;
        this.videoVrView = videoVrView;
    }

    @Override
    public Fragment getItem(int position) {

        Bundle festivalBundle = new Bundle();
        festivalBundle.putSerializable("festival", festival);
        festivalBundle.putSerializable("festivalDetail", festivalDetail);
        festivalBundle.putSerializable("actualFestivalTicketPhase", actualFestivalTicketPhase);
        switch (position) {
            case 0:
                FestivalDetailFragment tab1 = new FestivalDetailFragment();
                tab1.setArguments(festivalBundle);
                return tab1;
            case 1:
                MapFragmentDialog tab2 = new MapFragmentDialog();
                tab2.setArguments(festivalBundle);
                return tab2;
            case 2:
                if(photoVrView != null) {
                    VRPanoView tab3 = new VRPanoView();
                    Bundle photoVrBundle = new Bundle();
                    photoVrBundle.putSerializable("photoVrView", photoVrView);

                    tab3.setArguments(photoVrBundle);
                    return tab3;
                }else if(videoVrView != null) {
                    VRVideoView tab4 = new VRVideoView();
                    Bundle videoVrBundle = new Bundle();
                    videoVrBundle.putSerializable("videoVrView", videoVrView);
                    tab4.setArguments(videoVrBundle);
                    return tab4;
                }else{
                    TicketFragmentDialog tab5 = new TicketFragmentDialog();
                    tab5.setArguments(festivalBundle);
                    return tab5;
                }
            case 3:
                if(photoVrView != null && videoVrView != null) {
                    VRVideoView tab4 = new VRVideoView();
                    Bundle videoVrBundle = new Bundle();
                    videoVrBundle.putSerializable("videoVrView", videoVrView);
                    tab4.setArguments(videoVrBundle);
                    return tab4;
                }
                return null;
            case 4:
                if(photoVrView != null && videoVrView != null) {
                    TicketFragmentDialog tab5 = new TicketFragmentDialog();
                    tab5.setArguments(festivalBundle);
                    return tab5;
                }
                return null;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
