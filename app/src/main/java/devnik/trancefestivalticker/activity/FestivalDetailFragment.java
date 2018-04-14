package devnik.trancefestivalticker.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import org.greenrobot.greendao.query.Query;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.UITagHandler;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailImages;
import devnik.trancefestivalticker.model.FestivalDetailImagesDao;
import devnik.trancefestivalticker.model.FestivalTicketPhase;

/**
 * Created by nik on 07.03.2018.
 */

public class FestivalDetailFragment extends DialogFragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    private Festival festival;
    private FestivalDetail festivalDetail;
    private FestivalTicketPhase actualFestivalTicketPhase;

    private FestivalDetailImagesDao festivalDetailImagesDao;
    private Query<FestivalDetailImages> festivalDetailImagesQuery;
    private List<FestivalDetailImages> festivalDetailImages;

    private TextView homepage_url, ticket_url, lblTitle, lblDate, description, price, ticketPhaseTitle;
    private View view;
    private SliderLayout imageSlider;


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
        ticketPhaseTitle = (TextView) view.findViewById(R.id.ticketPhaseTitle);
        price = (TextView) view.findViewById(R.id.price);
        description = (TextView) view.findViewById(R.id.description);

        imageSlider = (SliderLayout) view.findViewById(R.id.slider);
        //Scale slider



        festival = (Festival) getArguments().getSerializable("festival");
        festivalDetail = (FestivalDetail) getArguments().getSerializable("festivalDetail");
        actualFestivalTicketPhase = (FestivalTicketPhase) getArguments().getSerializable("actualFestivalTicketPhase");

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
    public String festivalStatus(){
        Date start = festival.getDatum_start();
        Date end = festival.getDatum_end();
        Date today = new Date();

        if(today.after(start) && today.before(end)){
            //Festival is now!
            return "( Läuft gerade! )";
        }
        else if(today.before(start)){
            //Festival start is comming
            return "( Noch "+calcDaysTillStart(start)+" Tage )";
        }else if(today.after(start)){
            //Festival is expired
            return "( Abgelaufen! )";
        }
        return "";
    }
    public int calcDaysTillStart(Date startDate){
        Date today = new Date();
        return Days.daysBetween(new LocalDate(today.getTime()), new LocalDate(startDate.getTime())).getDays();
    }
    public void loadData(){
        lblTitle.setText(festival.getName());

        //TODO aulagern in string resource für translation
        lblDate.setText(DateFormat.format("dd.MM.yyyy", festival.getDatum_start())+ " - " + DateFormat.format("dd.MM.yyyy", festival.getDatum_end()) + " "+festivalStatus());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            homepage_url.setText(Html.fromHtml(festivalDetail.getHomepage_url(), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));
            ticket_url.setText(Html.fromHtml(festivalDetail.getTicket_url(), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));
            description.setText(Html.fromHtml(festivalDetail.getDescription(), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));
        }else{
            homepage_url.setText(Html.fromHtml(festivalDetail.getHomepage_url(),null, new UITagHandler()));
            ticket_url.setText(Html.fromHtml(festivalDetail.getTicket_url(),null, new UITagHandler()));
            description.setText(Html.fromHtml(festivalDetail.getDescription(), null, new UITagHandler()));
        }
        //Important to make the hrefs clickable
        description.setMovementMethod(LinkMovementMethod.getInstance());
        homepage_url.setMovementMethod(LinkMovementMethod.getInstance());
        ticket_url.setMovementMethod(LinkMovementMethod.getInstance());

        //Set Price
        if(actualFestivalTicketPhase != null) {
            ticketPhaseTitle.setText(actualFestivalTicketPhase.getTitle());
            price.setText(String.valueOf(actualFestivalTicketPhase.getPrice()));
        }
    }
    public void initImageSlider(){
        for(FestivalDetailImages image : festivalDetailImages){
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            textSliderView
                    .description(image.getTitle())
                    .image(image.getUrl())
                    .error(R.drawable.no_internet)
                    .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
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
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

}
