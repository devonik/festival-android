package devnik.trancefestivalticker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.model.CustomDate;
import devnik.trancefestivalticker.model.Image;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by nik on 21.02.2018.
 */

public class SectionAdapter extends StatelessSection {
    private Context mContext;
    private ArrayList<Image> images;
    private CustomDate customDate;

    public SectionAdapter(Context context, CustomDate customDate, ArrayList<Image> images) {
        // call constructor with layout resources for this Section header and items
        super(new SectionParameters.Builder(R.layout.gallery_thumbnail)
                .headerResourceId(R.layout.gallery_section)
                .build());
        mContext = context;
        this.images = images;
        this.customDate = customDate;
    }

    @Override
    public int getContentItemsTotal() {
        return images.size(); // number of items of this section
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        // return a custom instance of ViewHolder for the items of this section
        return new MyItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyItemViewHolder itemHolder = (MyItemViewHolder) holder;
        Image image = images.get(position);
        // bind your view here
        itemHolder.title.setText(image.getName());
        itemHolder.subtitle.setText(image.getTimestamp());

        Glide.with(mContext).load(image.getSmall())
                .thumbnail(0.5f)
                .placeholder(R.mipmap.ic_action_refresh)
                .error(R.drawable.warning_error_icon)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(itemHolder.thumbnail);
        /*itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SlideshowDialogFragment fragment = SlideshowDialogFragment.newInstance();
                FragmentTransaction transaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragment.show(transaction,"slideshow");
            }
        });*/
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

    class MyItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public TextView title, subtitle;

        public MyItemViewHolder(View itemView) {
            super(itemView);

            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            title = (TextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.customDate);


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
                clickListener.onClick(child, rv.getChildPosition(child));
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
