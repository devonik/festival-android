package devnik.trancefestivalticker.activity.detail.tickets;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.google.gson.Gson;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.model.UserTickets;

public class TicketDetailActivity extends AppCompatActivity {
    private ImageView ticketImage;
    private PDFView pdfView;
    private String uri;
    private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ticket);

        Bundle extras = getIntent().getExtras();

        assert extras != null;
        UserTickets userTicket = new Gson().fromJson(String.valueOf(extras.get("user_ticket")), UserTickets.class);
        if(userTicket != null) {
            if(userTicket.getTicketType().equals("application/pdf")){
                pdfView = findViewById(R.id.ticket_detail_pdf);
                pdfView.setVisibility(View.VISIBLE);
                pdfView.fromUri(Uri.parse(userTicket.getTicketUri()))
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        Log.e("Ticket Detail","Can not display pdf. Exception: "+t.getMessage());
                    }
                })
                .load();
            }else {
                ticketImage = findViewById(R.id.ticket_detail_image);
                ticketImage.setVisibility(View.VISIBLE);
                RequestOptions glideOptions = new RequestOptions()
                        .placeholder(R.drawable.progress_animation)
                        .error(R.drawable.no_internet)
                        .priority(Priority.HIGH)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop();
                Glide.with(this)
                        .load(Uri.parse(userTicket.getTicketUri()))
                        .apply(glideOptions)
                        .into(ticketImage);
            }
        }
    }
}
