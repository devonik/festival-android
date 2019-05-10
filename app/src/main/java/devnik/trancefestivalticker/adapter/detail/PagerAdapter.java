package devnik.trancefestivalticker.adapter.detail;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import devnik.trancefestivalticker.activity.detail.FestivalDetailFragment;
import devnik.trancefestivalticker.activity.detail.maps.MapFragmentDialog;
import devnik.trancefestivalticker.activity.detail.tickets.TicketFragmentDialog;
import devnik.trancefestivalticker.activity.detail.vr.photo.VRPanoView;
import devnik.trancefestivalticker.activity.detail.vr.video.VRVideoView;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalVrView;

/**
 * Created by nik on 09.03.2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    private final int numTabs;
    private final Festival festival;
    private final FestivalDetail festivalDetail;
    private final FestivalTicketPhase actualFestivalTicketPhase;
    private final FestivalVrView photoVrView;
    private final FestivalVrView videoVrView;

    public PagerAdapter(FragmentManager fm, int numTabs, Festival festival, FestivalDetail festivalDetail, FestivalVrView photoVrView, FestivalVrView videoVrView, FestivalTicketPhase actualFestivalTicketPhase) {
        super(fm);
        this.numTabs = numTabs;
        this.festival = festival;
        this.festivalDetail = festivalDetail;
        this.actualFestivalTicketPhase = actualFestivalTicketPhase;
        this.photoVrView = photoVrView;
        this.videoVrView = videoVrView;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        Bundle festivalBundle = new Bundle();
        festivalBundle.putSerializable("festival", festival);
        festivalBundle.putSerializable("festivalDetail", festivalDetail);
        festivalBundle.putSerializable("actualFestivalTicketPhase", actualFestivalTicketPhase);
        FestivalDetailFragment tab1 = new FestivalDetailFragment();
        switch (position) {
            case 0:
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
            case 4:
                if(photoVrView != null && videoVrView != null) {
                    TicketFragmentDialog tab5 = new TicketFragmentDialog();
                    tab5.setArguments(festivalBundle);
                    return tab5;
                }
            default:
                return tab1;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
