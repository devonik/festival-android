package devnik.trancefestivalticker.activity.detail.tickets;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.adapter.detail.tickets.TicketGalleryAdapter;
import devnik.trancefestivalticker.helper.BitmapUtils;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.UserTickets;
import devnik.trancefestivalticker.model.UserTicketsDao;

public class TicketFragmentDialog extends DialogFragment implements RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener,
        TicketGalleryAdapter.ClickListener {
    private View rootView;
    private  FloatingActionButton fabRemove;

    //FloatingActionMenu
    private RapidFloatingActionLayout rfaLayout;
    private RapidFloatingActionButton rfaButton;
    private RapidFloatingActionHelper rfabHelper;

    private ImageView ticketImg;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;


    private static final int READ_REQUEST_CODE = 42;
    private boolean tabIsVisible = false;
    private Festival festival;
    private UserTicketsDao userTicketsDao;
    private List<UserTickets> userTickets;
    private TicketGalleryAdapter ticketGalleryAdapter;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private File photoFile;
    private Uri photoURI;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_festival_ticket, container, false);
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

            fabRemove = rootView.findViewById(R.id.floating_btn_remove_tickets);
            fabRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeTickets();
                }
            });

            //Init state
            fabRemove.setVisibility(View.GONE);

            for(UserTickets ticket : userTickets){
                //Check file exist
                DocumentFile file = DocumentFile.fromSingleUri(getContext(),Uri.parse(ticket.getTicketUri()));

                if(file == null || !file.exists()){
                    userTickets.remove(ticket);
                    userTicketsDao.delete(ticket);
                }
            }
        }

        //Setting FloatinActionButton Menu
        rfaLayout = (RapidFloatingActionLayout) rootView.findViewById(R.id.rfad_ticket_options_layout);
        rfaButton = (RapidFloatingActionButton) rootView.findViewById(R.id.rfaf_add_ticket_options);

        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(getContext());
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Suche")
                .setResId(R.drawable.baseline_image_search_black_24)
                .setIconNormalColor(0xff43c6ac)
                .setIconPressedColor(0xff0d5302)
                .setLabelColor(0xff43c6ac)
                .setWrapper(0)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Kamera")
                .setResId(R.drawable.baseline_photo_camera_black_24)
                .setIconNormalColor(0xff191654)
                .setIconPressedColor(0xff283593)
                .setLabelColor(0xff283593)
                .setWrapper(1)
        );
        rfaContent
                .setItems(items)
        ;

        rfabHelper = new RapidFloatingActionHelper(
                getContext(),
                rfaLayout,
                rfaButton,
                rfaContent
        ).build();
        return rootView;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        tabIsVisible = isVisibleToUser;
    }
    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {
        switch (position){
            case TicketRfabEnum.RFAB_PEFORM_SEARCH:
                performFileSearch();
                break;
            case TicketRfabEnum.RFAB_TAKE_PHOTO:
                onOpenCamera();
                break;


        }
        rfabHelper.toggleContent();
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {
        switch (position){
            case TicketRfabEnum.RFAB_PEFORM_SEARCH:
                performFileSearch();
                break;
            case TicketRfabEnum.RFAB_TAKE_PHOTO:
                onOpenCamera();
                break;


        }
        rfabHelper.toggleContent();
    }
    private void onOpenCamera(){
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        else
        {
            Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                // Create the File where the photo should go
                try {
                    photoFile = BitmapUtils.createTicketImageFile(getContext(), festival);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e("Create photo file","Can not create temp file for taking photo. Exception:"+ex.getMessage());
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(getContext(),
                            "devnik.trancefestivalticker.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
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
    }
    public void onTicketItemClicked(int position){
        //ticketGalleryAdapter.isSelected(position) is true when the item is selected by long press
        //ticketGalleryAdapter.getSelectedItemCount() is over 0 when there are any items selected by long press
        //Only toggle selection if one of this ist true
        if(ticketGalleryAdapter.isSelected(position) || ticketGalleryAdapter.getSelectedItemCount() > 0){
            toggleSelection(position);
        }

    }
    public boolean onTicketItemLongClicked(int position){
        toggleSelection(position);
        return true;
    }
    /**
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        ticketGalleryAdapter.toggleSelection(position);
        int count = ticketGalleryAdapter.getSelectedItemCount();

        if (count > 0) {
            fabRemove.setVisibility(View.VISIBLE);
        } else {
            fabRemove.setVisibility(View.GONE);
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

    /**
     * this remove button is shown when there are selected tickets by long press
     * this function is called when this button is clicked
     */
    public void removeTickets(){
        List<Integer> selectedIndexes = ticketGalleryAdapter.getSelectedItems();
        //We need a copy here cuz when we remove it by index it will get a new index
        List<UserTickets> selectedUserTickets = new ArrayList<UserTickets>();
        for(Integer index : selectedIndexes){
            //Get the selected user ticket, delete it from view and database and refresh the adapter
            //-1 because the item will be removed and the list sort the list after calling remove()
            UserTickets userTicket = userTickets.get(index);
            selectedUserTickets.add(userTicket);
            //Toggle Selection cuz we want to
            //1. Deselect all removed tickets
            //2. Hide delete button
            //3. Show add button
            toggleSelection(index);
        }
        removeTicketsByList(selectedUserTickets);
    }
    public void removeTicketsByList(List<UserTickets> userTickets){
        for (UserTickets userTicket : userTickets){
            this.userTickets.remove(userTicket);
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
        }
        ticketGalleryAdapter.notifyDataSetChanged();
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
                    userTickets.add(userTicket);
                    ticketGalleryAdapter.notifyDataSetChanged();
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }



            }
        }
        //Open Camera Action
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            //Add UserTicket
            UserTickets userTicket = new UserTickets();
            userTicket.setFestivalId(festival.getFestival_id());
            userTicket.setTicketUri(photoURI.toString());
            userTicket.setTicketType(BitmapUtils.getMimeType(photoURI, getContext()));
            userTicketsDao.insert(userTicket);

            userTickets.add(userTicket);
            ticketGalleryAdapter.notifyDataSetChanged();

        }
    }

}
