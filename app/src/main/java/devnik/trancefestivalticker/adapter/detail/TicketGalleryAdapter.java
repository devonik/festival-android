package devnik.trancefestivalticker.adapter.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.File;
import java.net.URI;
import java.util.List;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.detail.TicketDetailActivity;
import devnik.trancefestivalticker.helper.PDFUtils;
import devnik.trancefestivalticker.model.UserTickets;

public class TicketGalleryAdapter extends RecyclerView.Adapter<TicketGalleryAdapter.ViewHolder>  {
    private List<UserTickets> userTickets;
    private Context context;

    private EventListener ticketGalleryAdapterListener;

    public interface EventListener {
        void onTicketRemoveByIndex(Integer index);
    }

    public TicketGalleryAdapter(Context context, List<UserTickets> userTickets, EventListener ticketGalleryAdapterListener) {
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
        Intent intent = ((Activity) context).getIntent();
        final int takeFlags = intent.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // Check for the freshest data.
        context.getContentResolver().takePersistableUriPermission(ticketUri, takeFlags);

        //Do something
        try {
            if(userTicket.getTicketType().equals("application/pdf")){
                String thumbPath = PDFUtils.generateImageFromPdf(ticketUri,context);
                Glide.with(context).load(thumbPath).into(viewHolder.img);
            }else{
                Glide.with(context).load(ticketUri).into(viewHolder.img);
            }

            //Add Listener to remove ticket
            viewHolder.btnRemoveTicket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer adapterPosition = viewHolder.getAdapterPosition();
                    ticketGalleryAdapterListener.onTicketRemoveByIndex(adapterPosition);
                }
            });
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent= new Intent(context,TicketDetailActivity.class);
                    intent.putExtra("user_ticket",new Gson().toJson(userTicket));
                    context.startActivity(intent);
                }
            });
        }catch(Exception ex){
            Log.e("Loading Ticket:","Can not load ticket. Exception: "+ex.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return userTickets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        FloatingActionButton btnRemoveTicket;

        ViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.ticket_thumbnail);
            btnRemoveTicket = view.findViewById(R.id.floating_btn_remove_ticket);
        }
    }
}
