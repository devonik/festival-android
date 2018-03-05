package devnik.trancefestivalticker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.model.CustomDate;
import devnik.trancefestivalticker.model.FestivalMonth;
import devnik.trancefestivalticker.model.Image;
/**
 * Created by nik on 19.02.2018.
 *
 * This is a adapter class which inflates the gallery_thumbnail.xml and renders the images in recyclerView.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {
    private List<FestivalMonth> festivalMonths;
    private List<Image> images;
    private List<CustomDate> customDates;
    private Context mContext;

    private LayoutInflater mInflater;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public TextView title, subtitle;


        public MyViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            title = (TextView) view.findViewById(R.id.title);
            subtitle = (TextView) view.findViewById(R.id.customDate);

        }
    }

    public GalleryAdapter(Context context, List<FestivalMonth> festivalMonths) {
        //this(context,festivalMonths);
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mContext = context;
        this.festivalMonths = festivalMonths;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = mInflater.inflate(R.layout.gallery_thumbnail, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FestivalMonth festivalMonth = festivalMonths.get(position);

        Image image = festivalMonth.getImages().get(position);
        System.out.println("customDates: "+ customDates);
        CustomDate customDate = festivalMonth.getCustomDate();
        if(customDate.getMonth() != null){
            //TODO!!!!
            //Conditional inflate ???
            //Header ist solange null, bis das layout geladen wird
            //mInflater.inflate(R.layout.gallery_section, null, false);
            //holder.header.setText(customDate.getMonth());

            //return;
        }
        //holder.header.setText(customDate.getMonth());
        holder.title.setText(image.getName());
        holder.subtitle.setText(image.getTimestamp());

        Glide.with(mContext).load(image.getMedium())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.thumbnail);


    }

    @Override
    public int getItemCount() {
        return festivalMonths.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GalleryAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final GalleryAdapter.ClickListener clickListener) {
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
