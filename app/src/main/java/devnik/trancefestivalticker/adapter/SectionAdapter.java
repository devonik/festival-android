package devnik.trancefestivalticker.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.DetailActivity;
import devnik.trancefestivalticker.model.CustomDate;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDao;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalTicketPhaseDao;
import devnik.trancefestivalticker.model.MusicGenre;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.bumptech.glide.request.RequestOptions.centerCropTransform;

/**
 * Created by nik on 21.02.2018.
 */

public class SectionAdapter extends StatelessSection implements View.OnLongClickListener, IFilterableSection {
    private Context mContext;
    private ArrayList<Festival> festivals;
    private ArrayList<Festival> filteredFestivalList;
    private FestivalDetailDao festivalDetailDao;
    private Query<FestivalDetail> festivalDetailQuery;
    private FestivalDao festivalDao;
    private DaoSession daoSession;
    private FestivalTicketPhaseDao festivalTicketPhaseDao;
    private List<FestivalTicketPhase> festivalTicketPhases;
    private CustomDate customDate;
    public FragmentManager fragmentManager;

    public SectionAdapter(Context context, CustomDate customDate, ArrayList<Festival> festivals, FragmentManager fragmentManager) {

        // call constructor with layout resources for this Section header and items
        super(new SectionParameters.Builder(R.layout.gallery_thumbnail)
                .headerResourceId(R.layout.gallery_section)
                .build());
        mContext = context;

        this.customDate = customDate;
        this.festivals = festivals;
        this.fragmentManager = fragmentManager;
        this.filteredFestivalList = new ArrayList<>(festivals);
        daoSession = ((App)context).getDaoSession();
        festivalTicketPhaseDao = daoSession.getFestivalTicketPhaseDao();
        festivalDao = daoSession.getFestivalDao();
    }

    @Override
    public int getContentItemsTotal() {
        return filteredFestivalList.size(); // number of items of this section
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        // return a custom instance of ViewHolder for the items of this section
        return new MyItemViewHolder(view);
    }
    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

        final MyItemViewHolder itemHolder = (MyItemViewHolder) holder;
        final Festival festival = filteredFestivalList.get(position);
        final FestivalTicketPhase actualFestivalTicketPhase = festivalTicketPhaseDao.queryBuilder()
                .where(FestivalTicketPhaseDao.Properties.Festival_id.eq(festival.getFestival_id()),
                        FestivalTicketPhaseDao.Properties.Sold.eq("no"),
                        FestivalTicketPhaseDao.Properties.Started.eq("yes")
                ).build().unique();

        RequestOptions glideOptions = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.no_internet)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        // bind your view here
                Glide.with(mContext)
                        .load(festival.getThumbnail_image_url())
                        .apply(glideOptions)
                        .into(itemHolder.thumbnail);

            itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("festival", festival);
                intent.putExtra("actualFestivalTicketPhase", actualFestivalTicketPhase);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                }
            });

        itemHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                StringBuilder output = new StringBuilder();
                output.append(festival.getName()+": "+DateFormat.format("dd.MM",festival.getDatum_start()));
                if(actualFestivalTicketPhase != null){
                    output.append(", "+actualFestivalTicketPhase.getPrice()+" €");
                }
                Toast.makeText(mContext, output, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }
    @Override
    public boolean onLongClick(View view) {

        return true;
    }
    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }
    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.monthHeader.setText(customDate.getMonth());

    }
    @Override
    public void filter(List<String> musicGenres) {

        if (musicGenres.size()==0) {
            //Reset Filter
            filteredFestivalList = new ArrayList<>(festivals);
            this.setVisible(true);
        } else {
            filteredFestivalList.clear();
            for(String filterGenre:musicGenres){
                for (Festival festival : festivals) {
                    if(!filteredFestivalList.contains(festival)) {
                        for (MusicGenre musicGenre : festival.getMusicGenres()) {

                            if (musicGenre.getName() == filterGenre) {
                                filteredFestivalList.add(festival);
                            }
                        }
                    }
                }
            }
            this.setVisible(!filteredFestivalList.isEmpty());
        }
    }

    class MyItemViewHolder extends RecyclerView.ViewHolder{
        public ImageView thumbnail;
        public TextView title, subtitle;
        public Festival festival;
        public MyItemViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        }

    }
    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        //private final TextView yearHeader;
        private final TextView monthHeader;


        HeaderViewHolder(View view) {
            super(view);
            //yearHeader = (TextView) view.findViewById(R.id.yearHeader);
            monthHeader = (TextView) view.findViewById(R.id.monthHeader);
        }
    }
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private SectionAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final SectionAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
