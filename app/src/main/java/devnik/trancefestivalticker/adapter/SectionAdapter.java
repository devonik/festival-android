package devnik.trancefestivalticker.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.detail.DetailActivity;
import devnik.trancefestivalticker.model.CustomDate;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalTicketPhase;
import devnik.trancefestivalticker.model.FestivalTicketPhaseDao;
import devnik.trancefestivalticker.model.MusicGenre;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;


/**
 * Created by nik on 21.02.2018.
 */

public class SectionAdapter extends StatelessSection implements View.OnLongClickListener, IFilterableSection {
    private final Context mContext;
    private final ArrayList<Festival> festivals;
    private ArrayList<Festival> filteredFestivalList;
    private final FestivalTicketPhaseDao festivalTicketPhaseDao;
    private final CustomDate customDate;

    public SectionAdapter(Context context, CustomDate customDate, ArrayList<Festival> festivals) {

        // call constructor with layout resources for this Section header and items
        super(SectionParameters.builder()
                .itemResourceId(R.layout.gallery_thumbnail)
                .headerResourceId(R.layout.gallery_section)
                .build());
        mContext = context;

        this.customDate = customDate;
        this.festivals = festivals;
        this.filteredFestivalList = new ArrayList<>(festivals);
        DaoSession daoSession = ((App) context).getDaoSession();
        festivalTicketPhaseDao = daoSession.getFestivalTicketPhaseDao();
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
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.no_internet)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                ;
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
                output.append(festival.getName())
                        .append(": ")
                        .append(DateFormat.format("dd.MM", festival.getDatum_start()));

                if(actualFestivalTicketPhase != null){
                    output.append(", ")
                            .append(actualFestivalTicketPhase.getPrice())
                            .append(" €");
                }
                output.append(" ")
                        .append(festivalStatus(festival));
                Toast.makeText(mContext, output, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //Check Festival is over
        Date end = festival.getDatum_end();
        Date today = new Date();
        if(today.after(end)){
            //Festival is expired
            itemHolder.festivalOverBadgeIcon.setVisibility(View.VISIBLE);
        }else{
            itemHolder.festivalOverBadgeIcon.setVisibility(View.GONE);
        }

    }
    private String festivalStatus(Festival festival){
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

        }else if(today.after(end)){
            //Festival is expired
            return "( Abgelaufen! )";
        }
        return "";
    }
    private int calcDaysTillStart(Date startDate){
        Date today = new Date();
        return Days.daysBetween(new LocalDate(today.getTime()), new LocalDate(startDate.getTime())).getDays();
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

        headerHolder.yearHeader.setText(customDate.getYear());
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

                            if (musicGenre.getName().equals(filterGenre)) {
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
        public final ImageView thumbnail;
        public TextView title, subtitle;
        public Festival festival;
        final FloatingActionButton festivalOverBadgeIcon;

        MyItemViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            festivalOverBadgeIcon = itemView.findViewById(R.id.festival_done);

        }

    }
    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView yearHeader;
        private final TextView monthHeader;


        HeaderViewHolder(View view) {
            super(view);
            yearHeader = view.findViewById(R.id.yearHeader);
            monthHeader = view.findViewById(R.id.monthHeader);
        }
    }

}
