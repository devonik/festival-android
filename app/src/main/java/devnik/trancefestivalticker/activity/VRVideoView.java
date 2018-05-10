package devnik.trancefestivalticker.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import devnik.trancefestivalticker.Manifest;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.GetBitmapFromURLAsync;
import devnik.trancefestivalticker.helper.PermissionUtils;
import devnik.trancefestivalticker.helper.UITagHandler;
import devnik.trancefestivalticker.model.FestivalVrView;

public class VRVideoView extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback
        //implements GetBitmapFromURLAsync.GetBitmapFromURLCompleted
        {
    private static final String TAG = VRVideoView.class.getSimpleName();

    /**
     * Preserve the video's state when rotating the phone.
     */
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during  rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    /**
     * Arbitrary constants and variable to track load status. In this example, this variable should
     * only be accessed on the UI thread. In a real app, this variable would be code that performs
     * some UI actions when the video is fully loaded.
     */
    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri videoUri;

    /** Configuration information for the video. **/
    private Options videoOptions = new Options();

    //private VideoLoaderTask backgroundVideoLoaderTask;

    /**
     * The video view and its custom UI elements.
     */
    protected VrVideoView videoWidgetView;

    /**
     * Seeking UI & progress indicator. The seekBar's progress value represents milliseconds in the
     * video.
     */
    private SeekBar seekBar;
    private TextView statusText;

    private ImageButton volumeToggle;
    private boolean isMuted;
    private View view;
    /**
     * By default, the video will start playing as soon as it is loaded. This can be changed by using
     * {@link VrVideoView#pauseVideo()} after loading the video.
     */
    private boolean isPaused = false;
    boolean _areLecturesLoaded = false;
    private boolean tabIsVisible = false;

    private boolean userWantDownloadVideo = false;
    private boolean videoExistLocal = false;

    //Video Download
    //Permission
    private int REQUEST_STORAGE = 1;
    private boolean mPermissionDenied = false;
    // Progress Dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    // File url to download
    private static String file_url = "https://niklas-grieger.de/files/360video/test.mp4";

    private FestivalVrView vrView;
    private String fileName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_vr_video_view, container, false);

        //Need check, cuz onCreateView is called even if the neigbourgh tab is clicked... cuz pagerview cache it
        if(tabIsVisible) {
            vrView = (FestivalVrView) getArguments().getSerializable("videoVrView");
            try {
                URL url = new URL(vrView.getUrl());
                fileName = FilenameUtils.getName(url.getPath());
                enableStoragePermission();
                String path = Environment
                        .getExternalStorageDirectory().toString()
                        + "/"+fileName;
                File f = new File(path);  //
                videoUri = Uri.fromFile(f);
                if(f.exists()){
                    loadVRVideo();
                }
                else{
                    userWantDownloadVideoDialog();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        videoWidgetView = (VrVideoView) view.findViewById(R.id.video_view);
        statusText = (TextView) view.findViewById(R.id.status_text);

        return view;
    }
    /*
    * event of the fragment to know if its visible to the user and then load your data
    * Tab is selected/unselected
    * */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            tabIsVisible = true;
            if(view != null){
                videoWidgetView.resumeRendering();
                enableStoragePermission();
                String path = Environment
                        .getExternalStorageDirectory().toString()
                        + "/"+fileName;
                File f = new File(path);
                videoUri = Uri.fromFile(f);
                if(f.exists()){
                    loadVRVideo();
                }
                else{
                    userWantDownloadVideoDialog();
                }

            }
        }else{
            //Is unselected
            if(videoWidgetView != null){
                //Prevent continue if the neigbourgh tab is clicked
                videoWidgetView.pauseRendering();
                //videoWidgetView.shutdown();
            }
            tabIsVisible = false;

        }
    }
    public int getLoadVideoStatus() {
        return loadVideoStatus;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("VRVIDEO","onDestroyView");
        //videoWidgetView.pauseRendering();
        //videoWidgetView.shutdown();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEST_STORAGE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Enable the external storage write layer if the permission has been granted.
            enableStoragePermission();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }
    private void enableStoragePermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(getActivity(), REQUEST_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
        } //else if (mMap != null) {
            // Access to the location has been granted to the app.
            //mMap.setMyLocationEnabled(true);

        //}
    }
    private void loadVRVideo(){
        try {

            seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            seekBar.setOnSeekBarChangeListener(new SeekBarListener());

            // Bind input and output objects for the view.
            videoWidgetView = (VrVideoView) view.findViewById(R.id.video_view);
            videoWidgetView.setEventListener(new ActivityEventListener());

            volumeToggle = (ImageButton) view.findViewById(R.id.volume_toggle);
            volumeToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setIsMuted(!isMuted);
                }
            });

            loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;
            VrVideoView.Options options = new VrVideoView.Options();
            options.inputType = VrVideoView.Options.TYPE_MONO;


            videoWidgetView.loadVideo(videoUri, options);
        } catch (IOException e) {
            // An error here is normally due to being unable to locate the file.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            // Since this is a background thread, we need to switch to the main thread to show a toast.
            videoWidgetView.post(new Runnable() {
                @Override
                public void run() {
                    Toast
                            .makeText(VRVideoView.this.getActivity(), "Error opening file. ", Toast.LENGTH_LONG)
                            .show();
                }
            });
            Log.e(TAG, "Could not open video: " + e);
        }
    }
    private void userWantDownloadVideoDialog(){
        AlertDialog.Builder builderDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        builderDialogBuilder.setTitle("Video Download");
        TextView creditTextView = new TextView(getActivity());
        creditTextView.setPadding(15,15,15,15);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            creditTextView.setText(Html.fromHtml(getString(R.string.vr_video_ask_download), Html.FROM_HTML_MODE_COMPACT,null, new UITagHandler()));
        }else{
            creditTextView.setText(Html.fromHtml(getString(R.string.vr_video_ask_download),null, new UITagHandler()));
        }
        //Important to make the hrefs clickable
        //creditTextView.setMovementMethod(LinkMovementMethod.getInstance());

        builderDialogBuilder.setView(creditTextView);
        // Set up the buttons
        builderDialogBuilder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage("Video wird geladen... Bitte warten");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.show();
                new DownloadFileFromURL().execute(file_url);
            }
        });
        //TODO Später button -> go back
        /*builderDialogBuilder.setNegativeButton("Später", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                Integer test = fm.getBackStackEntryCount();

            }
        });*/
        builderDialogBuilder.create();
        builderDialogBuilder.show();

    }
    private void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        volumeToggle.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
        videoWidgetView.setVolume(isMuted ? 0.0f : 1.0f);
    }

    public boolean isMuted() {
        return isMuted;
    }
    @Override
    public void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }

    @Override
    public void onDestroy() {
        // Destroy the widget and free memory.
        videoWidgetView.shutdown();
        super.onDestroy();
    }
    private void togglePause() {
        if (isPaused) {
            videoWidgetView.playVideo();
        } else {
            videoWidgetView.pauseVideo();
        }
        isPaused = !isPaused;
        updateStatusText();
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        status.append(isPaused ? "Paused: " : "Playing: ");
        status.append(String.format("%.2f", videoWidgetView.getCurrentPosition() / 1000f));
        status.append(" / ");
        status.append(videoWidgetView.getDuration() / 1000f);
        status.append(" seconds.");
        statusText.setText(status.toString());
    }

    /**
     * When the user manipulates the seek bar, update the video position.
     */
    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoWidgetView.seekTo(progress);
                updateStatusText();
            } // else this was from the ActivityEventHandler.onNewFrame()'s seekBar.setProgress update.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrVideoEventListener {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Successfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
            seekBar.setMax((int) videoWidgetView.getDuration());
            updateStatusText();
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Toast.makeText(
                    VRVideoView.this.getActivity(), "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            //Neuer Versuch
            //new DownloadFileFromURL().execute(file_url);
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {
            togglePause();
        }

        /**
         * Update the UI every frame.
         */
        @Override
        public void onNewFrame() {
            updateStatusText();
            seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            videoWidgetView.seekTo(0);
        }
    }

    //Download Video
    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(progress_bar_type);
            pDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/"+fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            //dismissDialog(progress_bar_type);
            pDialog.hide();
            loadVRVideo();
        }

    }
}
