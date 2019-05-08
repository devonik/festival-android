package devnik.trancefestivalticker.adapter.detail.tickets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.util.List;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.detail.tickets.TicketDetailActivity;
import devnik.trancefestivalticker.helper.PDFUtils;
import devnik.trancefestivalticker.model.UserTickets;

public class TicketGalleryAdapter extends SelectableAdapter<TicketGalleryAdapter.ViewHolder>  {
    private List<UserTickets> userTickets;
    private Context context;

    private ClickListener ticketGalleryAdapterListener;

    public interface ClickListener {
        void onTicketRemoveByIndex(Integer index);
        void onTicketItemClicked(int position);
        boolean onTicketItemLongClicked(int position);
    }

    public TicketGalleryAdapter(Context context, List<UserTickets> userTickets, ClickListener ticketGalleryAdapterListener) {
        this.context = context;
        this.userTickets = userTickets;
        this.ticketGalleryAdapterListener = ticketGalleryAdapterListener;
    }

    @NonNull
    @Override
    public TicketGalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ticket_thumbnail, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * gets the image url from adapter and passes to Glide API to load the image
     *
     * @param viewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final UserTickets userTicket = userTickets.get(i);
        String ticketUriString = userTicket.getTicketUri();
        Uri ticketUri = Uri.parse(ticketUriString);
        try {
            Intent intent = ((Activity) context).getIntent();
            final int takeFlags = intent.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            context.getContentResolver().takePersistableUriPermission(ticketUri, takeFlags);
        }catch (SecurityException e){
            e.printStackTrace();
        }
        RequestOptions glideOptions = new RequestOptions()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.no_internet)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                ;

        //Do something
        try {
            if (userTicket.getTicketType().equals("application/pdf")) {
                String thumbPath = PDFUtils.generateImageFromPdf(ticketUri, context);
                Glide.with(context)
                        .load(thumbPath)
                        .apply(glideOptions)
                        .into(viewHolder.img);
            } else {
                Glide.with(context)
                        .load(ticketUri)
                        .apply(glideOptions)
                        .into(viewHolder.img);
            }
        }catch(Exception ex){
            Log.e("Loading Ticket:","Can not load ticket. Exception: "+ex.getMessage());
            Toast.makeText(context, "Could not load ticket", Toast.LENGTH_LONG).show();
        }

        //Add Listener to remove ticket
        /*viewHolder.btnRemoveTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer adapterPosition = viewHolder.getAdapterPosition();
                ticketGalleryAdapterListener.onTicketRemoveByIndex(adapterPosition);
            }
        });*/
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ticketGalleryAdapter.isSelected(position) is true when the item is selected by long press
                //ticketGalleryAdapter.getSelectedItemCount() is over 0 when there are any items selected by long press
                //Only start detail activity if both are true
                if(!isSelected(viewHolder.getAdapterPosition()) && getSelectedItemCount() == 0) {
                    Intent intent = new Intent(context, TicketDetailActivity.class);
                    intent.putExtra("user_ticket", new Gson().toJson(userTicket));
                    context.startActivity(intent);
                }

                ticketGalleryAdapterListener.onTicketItemClicked(viewHolder.getAdapterPosition());
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                Log.d("On long click Ticket", "ticket long-clicked at position " + viewHolder.getAdapterPosition());
                ticketGalleryAdapterListener.onTicketItemLongClicked(viewHolder.getAdapterPosition());
                return true;
            }
        });

        // Highlight the item if it's selected
        viewHolder.selectedOverlay.setVisibility(isSelected(i) ? View.VISIBLE : View.INVISIBLE);

    }

    @Override
    public int getItemCount() {
        return userTickets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        //FloatingActionButton btnRemoveTicket;
        View selectedOverlay;

        ViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.ticket_thumbnail);
            //btnRemoveTicket = view.findViewById(R.id.floating_btn_remove_ticket);
            selectedOverlay = view.findViewById(R.id.selected_overlay);
        }
    }
}
