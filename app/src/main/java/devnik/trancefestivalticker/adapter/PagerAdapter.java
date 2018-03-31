package devnik.trancefestivalticker.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import devnik.trancefestivalticker.activity.FestivalDetailFragment;
import devnik.trancefestivalticker.activity.MapFragmentDialog;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalTicketPhase;

/**
 * Created by nik on 09.03.2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numTabs;
    private Festival festival;
    private FestivalDetail festivalDetail;
    private FestivalTicketPhase actualFestivalTicketPhase;

    public PagerAdapter(FragmentManager fm, int numTabs, Festival festival, FestivalDetail festivalDetail, FestivalTicketPhase actualFestivalTicketPhase) {
        super(fm);
        this.numTabs = numTabs;
        this.festival = festival;
        this.festivalDetail = festivalDetail;
        this.actualFestivalTicketPhase = actualFestivalTicketPhase;
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
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
