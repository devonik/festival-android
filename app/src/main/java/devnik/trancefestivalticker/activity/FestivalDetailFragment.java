package devnik.trancefestivalticker.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.greenrobot.greendao.query.Query;

import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.UITagHandler;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailImages;
import devnik.trancefestivalticker.model.FestivalDetailImagesDao;

/**
 * Created by nik on 07.03.2018.
 */

public class FestivalDetailFragment extends DialogFragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    private Festival festival;
    private FestivalDetail festivalDetail;

    private FestivalDetailImagesDao festivalDetailImagesDao;
    private Query<FestivalDetailImages> festivalDetailImagesQuery;
    private List<FestivalDetailImages> festivalDetailImages;

    private TextView homepage_url, ticket_url, lblTitle, lblDate, description, price;
    private View view;
    private SliderLayout imageSlider;

    //AdMob
    private AdView mAdView;
    public static FestivalDetailFragment newInstance() {
        FestivalDetailFragment f = new FestivalDetailFragment();
        return f;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_festival_detail, container, false);
        lblTitle = (TextView) view.findViewById(R.id.title);
        lblDate = (TextView) view.findViewById(R.id.dateString);
        homepage_url = (TextView) view.findViewById(R.id.homepage_url);
        ticket_url = (TextView) view.findViewById(R.id.ticket_url);
        price = (TextView) view.findViewById(R.id.price);
        description = (TextView) view.findViewById(R.id.description);

        imageSlider = (SliderLayout) view.findViewById(R.id.slider);
        //AdMob

        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                mAdView.destroy();
                mAdView.setVisibility(View.GONE);
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        festival = (Festival) getArguments().getSerializable("festival");

        festivalDetail = (FestivalDetail) getArguments().getSerializable("festivalDetail");

        DaoSession daoSession = ((App)getActivity().getApplication()).getDaoSession();
        //Nur wenn das Festival eingetragende Details hat
        if(festivalDetail!=null) {
            festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();
            festivalDetailImagesQuery = festivalDetailImagesDao.queryBuilder().where(FestivalDetailImagesDao.Properties.FestivalDetailId.eq(festivalDetail.getFestival_detail_id())).build();
            festivalDetailImages = festivalDetailImagesQuery.list();

            initImageSlider();

            loadData();
        }
        return view;
    }
    public void loadData(){
        lblTitle.setText(festival.getName());
        lblDate.setText(DateFormat.format("dd.MM.yyyy", festival.getDatum_start())+ " - " + DateFormat.format("dd.MM.yyyy", festival.getDatum_end()));
        homepage_url.setText(festivalDetail.getHomepage_url());
        ticket_url.setText(festivalDetail.getTicket_url());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            description.setText(Html.fromHtml(festivalDetail.getDescription(), Html.FROM_HTML_MODE_COMPACT, null, new UITagHandler()));
        }else{
            description.setText(Html.fromHtml(festivalDetail.getDescription(), null, new UITagHandler()));
        }

    }
    public void initImageSlider(){
        for(FestivalDetailImages image : festivalDetailImages){
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            textSliderView
                    .description(image.getTitle())
                    .image(image.getUrl())
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",image.getTitle());

            imageSlider.addSlider(textSliderView);
        }

        imageSlider.setPresetTransformer(SliderLayout.Transformer.ZoomOut);
        imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Top);
        imageSlider.setCustomAnimation(new DescriptionAnimation());
        imageSlider.setDuration(4000);
        imageSlider.addOnPageChangeListener(this);

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    //For ImageSlider
    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(getActivity(),slider.getBundle().get("extra") + "",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        Log.d("Slider Demo", "Page Changed: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

}
