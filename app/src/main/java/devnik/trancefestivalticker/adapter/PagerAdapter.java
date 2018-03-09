package devnik.trancefestivalticker.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import devnik.trancefestivalticker.activity.FestivalDetailFragment;
import devnik.trancefestivalticker.activity.MapFragmentDialog;
import devnik.trancefestivalticker.model.Festival;

/**
 * Created by nik on 09.03.2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numTabs;
    private Festival festival;

    public PagerAdapter(FragmentManager fm, int numTabs, Festival festival) {
        super(fm);
        this.numTabs = numTabs;
        this.festival = festival;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FestivalDetailFragment tab1 = new FestivalDetailFragment();
                Bundle args = new Bundle();
                args.putSerializable("festival", festival);
                tab1.setArguments(args);
                return tab1;
            case 1:
                MapFragmentDialog tab2 = new MapFragmentDialog();
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
