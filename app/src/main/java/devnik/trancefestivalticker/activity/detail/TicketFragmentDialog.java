package devnik.trancefestivalticker.activity.detail;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.google.gson.Gson;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.MainActivity;
import devnik.trancefestivalticker.adapter.detail.TicketGalleryAdapter;
import devnik.trancefestivalticker.helper.BitmapUtils;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.UserTickets;
import devnik.trancefestivalticker.model.UserTicketsDao;

import static io.fabric.sdk.android.Fabric.TAG;

public class TicketFragmentDialog extends DialogFragment implements TicketGalleryAdapter.EventListener {
    private ImageView ticketImg;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;

    private static final int READ_REQUEST_CODE = 42;
    private boolean tabIsVisible = false;
    private Festival festival;
    private UserTicketsDao userTicketsDao;
    private List<UserTickets> userTickets;
    private TicketGalleryAdapter ticketGalleryAdapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_festival_ticket, container, false);
        if(tabIsVisible) {


            assert getArguments() != null;
            festival = (Festival) getArguments().getSerializable("festival");

            DaoSession daoSession = ((App) Objects.requireNonNull(getActivity()).getApplication()).getDaoSession();

            userTicketsDao = daoSession.getUserTicketsDao();
            userTickets = userTicketsDao.queryBuilder().where(UserTicketsDao.Properties.FestivalId.eq(festival.getFestival_id())).list();
            ticketImg = rootView.findViewById(R.id.ticket_thumbnail);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.ticket_fragment_recycler_view);
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
            recyclerView.setLayoutManager(gridLayoutManager);

            ticketGalleryAdapter = new TicketGalleryAdapter(getContext(), userTickets, this);
            recyclerView.setAdapter(ticketGalleryAdapter);

            for(UserTickets ticket : userTickets){
                //Check file exist
                DocumentFile file = DocumentFile.fromSingleUri(getContext(),Uri.parse(ticket.getTicketUri()));

                if(file == null || !file.exists()){
                    userTickets.remove(ticket);
                    userTicketsDao.delete(ticket);
                }
            }

            if (userTickets.size() == 0) {
                performFileSearch();
            } else {

                //Add Listener to add ticket
                FloatingActionButton fabAdd = rootView.findViewById(R.id.floating_btn_add_ticket);
                fabAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        performFileSearch();
                    }
                });
            }
        }
        return rootView;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        tabIsVisible = isVisibleToUser;
    }
    //Floating Ticket Remove Event
    public void onTicketRemoveByIndex(Integer index){
        UserTickets userTicket = userTickets.get(index);
        userTickets.remove(userTicket);
        ticketGalleryAdapter.notifyDataSetChanged();
        Log.e("Remove Ticket","Want to remove ticket at index: "+index);

        //Persist the remove and remove it from the database
        userTicketsDao.delete(userTicket);

        if(userTicket.getTicketType().equals("application/pdf")) {
            //Delete thumnail if it was an pdf
            String pdfFolder = Environment.getExternalStorageDirectory() + "/PDF";
            Boolean deleteThumbnail =
                    BitmapUtils.deleteThumbnail(
                            new File(pdfFolder),
                            FilenameUtils.getBaseName(Uri.parse(userTicket.getTicketUri()).getPath()) + "-thumb.png");
        }
        if(userTickets.size() == 0){
            //If this was the last ticket and no ticket is there - call file search
            performFileSearch();
        }
    }
    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        String[] mimeTypes = {"image/*","application/pdf"};
        intent.setType(!mimeTypes[0].equals("") ? mimeTypes[0] : "*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(intent, READ_REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                UserTickets userTicket = new UserTickets();
                userTicket.setFestivalId(festival.getFestival_id());
                assert uri != null;
                userTicket.setTicketUri(uri.toString());

                userTicket.setTicketType(BitmapUtils.getMimeType(uri, getContext()));
                userTicketsDao.save(userTicket);

                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    Objects.requireNonNull(getActivity()).getContentResolver().takePersistableUriPermission(uri, takeFlags);
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }

                userTickets.add(userTicket);
                ticketGalleryAdapter.notifyDataSetChanged();

            }
        }
    }
}
