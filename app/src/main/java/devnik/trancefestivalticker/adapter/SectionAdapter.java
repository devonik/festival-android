package devnik.trancefestivalticker.adapter;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.text.format.DateFormat;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.DetailActivity;
import devnik.trancefestivalticker.model.CustomDate;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by nik on 21.02.2018.
 */

public class SectionAdapter extends StatelessSection implements View.OnLongClickListener {
    private Context mContext;
    private ArrayList<Festival> festivals;
    private FestivalDetailDao festivalDetailDao;
    private Query<FestivalDetail> festivalDetailQuery;

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
    }

    @Override
    public int getContentItemsTotal() {
        return festivals.size(); // number of items of this section
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        // return a custom instance of ViewHolder for the items of this section
        return new MyItemViewHolder(view);
    }
    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {

        final MyItemViewHolder itemHolder = (MyItemViewHolder) holder;
        final Festival festival = festivals.get(position);
        // bind your view here
                Glide.with(mContext).load(festival.getThumbnail_image_url())
                .thumbnail(0.5f)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.mandala_om_480x480)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)

                .into(itemHolder.thumbnail);

            itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("festival", festival);
                mContext.startActivity(intent);
                }
            });

        itemHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(mContext, festival.getName()+": "+ DateFormat.format("dd.MM",festival.getDatum_start()), Toast.LENGTH_SHORT).show();
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
