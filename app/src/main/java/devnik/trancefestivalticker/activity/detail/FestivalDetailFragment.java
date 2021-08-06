package devnik.trancefestivalticker.activity.detail;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import org.apache.commons.text.StringEscapeUtils;
import org.greenrobot.greendao.query.Query;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    private List<FestivalDetailImages> festivalDetailImages;

    private TextView homepage_url, ticket_url, lblTitle, lblDate, description, price, ticketPhaseTitle;
    private SliderLayout imageSlider;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_festival_detail, container, false);
        lblTitle = view.findViewById(R.id.title);
        lblDate = view.findViewById(R.id.dateString);
        homepage_url = view.findViewById(R.id.homepage_url);
        ticket_url = view.findViewById(R.id.ticket_url);
        ticketPhaseTitle = view.findViewById(R.id.ticketPhaseTitle);
        price = view.findViewById(R.id.price);
        description = view.findViewById(R.id.description);

        imageSlider = view.findViewById(R.id.slider);
        ScrollView scrollView = view.findViewById(R.id.scrollView);
        LinearLayout linearLayoutSlider = view.findViewById(R.id.linear_layout_slider);

        assert getArguments() != null;
        festival = (Festival) getArguments().getSerializable("festival");
        festivalDetail = (FestivalDetail) getArguments().getSerializable("festivalDetail");
        actualFestivalTicketPhase = (FestivalTicketPhase) getArguments().getSerializable("actualFestivalTicketPhase");

        DaoSession daoSession = ((App) requireActivity().getApplication()).getDaoSession();
        //Nur wenn das Festival eingetragende Details hat
        if(festivalDetail!=null) {
            FestivalDetailImagesDao festivalDetailImagesDao = daoSession.getFestivalDetailImagesDao();
            Query<FestivalDetailImages> festivalDetailImagesQuery = festivalDetailImagesDao.queryBuilder().where(FestivalDetailImagesDao.Properties.FestivalDetailId.eq(festivalDetail.getFestival_detail_id())).build();
            festivalDetailImages = festivalDetailImagesQuery.list();

            initImageSlider();

            loadData();
        }

        //Try to scale image slider down if the scroll view is taken
        /*scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            boolean isShow = true;
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                int scrollX = scrollView.getScrollX();
                Log.e("OnScrollChange", "scrollY: "+scrollY+ "...scrollX"+scrollX);
                if(scrollY>=40){
                    isShow = true;
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
                    linearLayoutSlider.startAnimation(animation);
                    linearLayoutSlider.setVisibility(View.GONE);
                }else if(isShow){
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
                    linearLayoutSlider.startAnimation(animation);
                    linearLayoutSlider.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });*/
        return view;
    }
    private String festivalStatus(){
        Date start = festival.getDatum_start();
        Date end = festival.getDatum_end();
        Date today = new Date();

        if(today.after(start) && today.before(end)){
            //Festival is now!
            return "( Läuft gerade! )";
        }
        else if(today.before(start)){
            int daysTillStart = calcDaysTillStart(start);
            //Festival start is comming
            if(daysTillStart > 1){
                return "( Noch "+calcDaysTillStart(start)+" Tage )";
            }
            else if(daysTillStart == 1){
                return "( Morgen ist es soweit !! )";
            }

        }else if(today.after(start)){
            //Festival is expired
            return "( Abgelaufen! )";
        }
        return "";
    }
    private int calcDaysTillStart(Date startDate){
        Date today = new Date();
        return Days.daysBetween(new LocalDate(today.getTime()), new LocalDate(startDate.getTime())).getDays();
    }
    private void loadData(){
        lblTitle.setText(festival.getName());

        String dateStart = (String) DateFormat.format("dd.MM.yyyy", festival.getDatum_start());
        String dateEnd = (String) DateFormat.format("dd.MM.yyyy", festival.getDatum_end());
        
        String festivalDateResource = requireContext().getString(R.string.festival_date_placeholder, dateStart, dateEnd, festivalStatus());
        lblDate.setText(festivalDateResource);

        String homepageUrl = festivalDetail.getHomepage_url() != null ? festivalDetail.getHomepage_url() : "Unbekannt";
        String ticketUrl = festivalDetail.getTicket_url() != null ? festivalDetail.getTicket_url() : "Unbekannt";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){

            if(festivalDetail.getHomepage_url() != null)
                homepage_url.setText(Html.fromHtml(festivalDetail.getHomepage_url(), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));

            if(festivalDetail.getTicket_url() != null)
                ticket_url.setText(Html.fromHtml(festivalDetail.getTicket_url(), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));

            if(festivalDetail.getDescription() != null) {

                if (festivalDetail.getDescription().startsWith("<p>"))
                    description.setText(Html.fromHtml(festivalDetail.getDescription(), Html.FROM_HTML_MODE_COMPACT, null, new UITagHandler()));
                else
                    description.setText(StringEscapeUtils.unescapeJava(festivalDetail.getDescription()));
            }
            else description.setText(Html.fromHtml(getString(R.string.description_placeholder_text), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));

        }else{

            if(festivalDetail.getHomepage_url() != null)
                homepage_url.setText(Html.fromHtml(festivalDetail.getHomepage_url(), null, new UITagHandler()));

            if(festivalDetail.getTicket_url() != null)
                ticket_url.setText(Html.fromHtml(festivalDetail.getTicket_url(), null, new UITagHandler()));

            if(festivalDetail.getDescription() != null)
                if(festivalDetail.getDescription().startsWith("<p>")) description.setText(Html.fromHtml(festivalDetail.getDescription(), null, new UITagHandler()));
                else description.setText(StringEscapeUtils.unescapeJava(festivalDetail.getDescription()));

            else description.setText(Html.fromHtml(getString(R.string.description_placeholder_text), null, new UITagHandler()));

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
    private void initImageSlider(){
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
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
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
