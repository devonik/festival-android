package devnik.trancefestivalticker.activity.detail.vr.video;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.IActivityListeners;
import devnik.trancefestivalticker.helper.PermissionUtils;
import devnik.trancefestivalticker.helper.UITagHandler;
import devnik.trancefestivalticker.model.FestivalVrView;


public class VRVideoView extends Fragment implements IActivityListeners
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
    private static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    private static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    private static final int LOAD_VIDEO_STATUS_ERROR = 2;
    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri videoUri;

            private AlertDialog errorLoadingDialog;
    //private VideoLoaderTask backgroundVideoLoaderTask;

    /**
     * The video view and its custom UI elements.
     */
    private VrVideoView videoWidgetView;

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
    private boolean tabIsVisible = false;

            private boolean mPermissionDenied = false;
    // Progress Dialog
    private ProgressDialog pDialog;
    private FestivalVrView vrView;
    private Uri videoServerUri;
    private String fileName;

    private String cachePath;
            @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_vr_video_view, container, false);
                assert getArguments() != null;
        vrView = (FestivalVrView) getArguments().getSerializable("videoVrView");

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        /*cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable() ? Objects.requireNonNull(Objects.requireNonNull(getActivity()).getExternalCacheDir()).getPath() :
                Objects.requireNonNull(getActivity()).getCacheDir().getPath();*/
        cachePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/tftApp/360/videos";
        File folderToSave = new File(cachePath);
        if(!folderToSave.exists()) {
            boolean mkdirs = folderToSave.mkdirs();
        }

        enableStoragePermission();
        //Need check, cuz onCreateView is called even if the neigbourgh tab is clicked... cuz pagerview cache it
        if(tabIsVisible) {

            videoServerUri = Uri.parse(vrView.getUrl());
            fileName = FilenameUtils.getName(videoServerUri.getPath());
            //Only for testing
            //String path = "/storage/19B0-BFBC/DCIM/Gear 360/PsyExp2018_MainFloor2.mp4";

            String path = cachePath + "/"+fileName;
            File f = new File(path);  //
            videoUri = Uri.fromFile(f);
            if(f.exists()){
                loadVRVideo();
            }
            else{
                showDialog();
            }
        }
        videoWidgetView = view.findViewById(R.id.video_view);
        statusText = view.findViewById(R.id.status_text);

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
                videoServerUri = Uri.parse(vrView.getUrl());
                fileName = FilenameUtils.getName(videoServerUri.getPath());

                //String path = "/storage/19B0-BFBC/DCIM/Gear 360/PsyExp2018_MainFloor2.MP4";
                String path = cachePath + "/"+fileName;
                File f = new File(path);
                videoUri = Uri.fromFile(f);
                if(f.exists()){
                    loadVRVideo();
                }
                else{
                    showDialog();
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
    private void enableStoragePermission() {

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            int REQUEST_STORAGE = 1;
            PermissionUtils.requestPermission(getActivity(), REQUEST_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
        } //else if (mMap != null) {
            // Access to the location has been granted to the app.
            //mMap.setMyLocationEnabled(true);

        //}
    }
    private void loadVRVideo(){
        try {

            seekBar = view.findViewById(R.id.seek_bar);
            seekBar.setOnSeekBarChangeListener(new SeekBarListener());


            // Bind input and output objects for the view.
            videoWidgetView = view.findViewById(R.id.video_view);
            videoWidgetView.setEventListener(new ActivityEventListener());

            volumeToggle = view.findViewById(R.id.volume_toggle);
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
            //videoWidgetView.post(new Runnable() {
             //   @Override
              //  public void run() {
              //      Toast
              //              .makeText(VRVideoView.this.getActivity(), "Error opening file. ", Toast.LENGTH_LONG)
               //             .show();
              //  }
           // });
           // Log.e(TAG, "Could not open video: " + e);
            showFileBrokenDialog();
        }
    }
    private void showDialog(){

        AlertDialog.Builder builderDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        builderDialogBuilder.setTitle("Video Download / Streaming");
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
                startDownload();
            }
        });
        builderDialogBuilder.setNeutralButton("Stream", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startStreaming();
            }
        });
        //TODO Dismiss dialog before exiting the Activity, otherwise it occurs an exeption
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
        float maxVolume = 1.0f;
        videoWidgetView.setVolume(isMuted ? 0.0f : maxVolume);
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
        String status = (isPaused ? "Paused: " : "Playing: ") +
                String.format("%.2f", videoWidgetView.getCurrentPosition() / 1000f) +
                " / " +
                videoWidgetView.getDuration() / 1000f +
                " seconds.";
        statusText.setText(status);
    }
    private void showFileBrokenDialog() {
        if (errorLoadingDialog == null) {
            AlertDialog.Builder builderDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
            builderDialogBuilder.setTitle("Video Fehlerhaft. Oops!");
            TextView creditTextView = new TextView(getActivity());
            creditTextView.setPadding(15, 15, 15, 15);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                creditTextView.setText(Html.fromHtml(getString(R.string.vr_video_download_failed), Html.FROM_HTML_MODE_COMPACT, null, new UITagHandler()));
            } else {
                creditTextView.setText(Html.fromHtml(getString(R.string.vr_video_download_failed), null, new UITagHandler()));
            }
            //Important to make the hrefs clickable
            //creditTextView.setMovementMethod(LinkMovementMethod.getInstance());

            builderDialogBuilder.setView(creditTextView);
            // Set up the buttons
            builderDialogBuilder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startDownload();
                }
            });
            builderDialogBuilder.setNeutralButton( "Stream", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    startStreaming();
                }});
            //TODO Später button -> go back
/*builderDialogBuilder.setNegativeButton("Später", new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {

        FragmentManager fm = getActivity().getSupportFragmentManager();
        Integer test = fm.getBackStackEntryCount();

    }
});*/
            errorLoadingDialog = builderDialogBuilder.create();
            errorLoadingDialog.show();

        }
    }
    private void startStreaming(){

        if(!checkURLisReachable(videoServerUri.toString())) return;

        VrVideoView.Options options = new VrVideoView.Options();
        options.inputType = VrVideoView.Options.TYPE_MONO;


        try {
                videoWidgetView.loadVideo(videoServerUri, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void startDownload(){
        if(!checkURLisReachable(vrView.getUrl())) return;

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Video wird geladen... Bitte warten");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        pDialog.show();
        new DownloadFileFromURL().execute(vrView.getUrl());
    }
    private boolean checkURLisReachable(String fileUrl) {
        URL url = null;
        try {
            url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int code = connection.getResponseCode();

        return code == 200;
    }
         catch (Exception e) {
             Log.e("Check url exists", "Could not load file by url. Error: "+e);
             return false;
        }
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
            //Datei Fehlerhaft
            showFileBrokenDialog();
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
    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

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
                OutputStream output = new FileOutputStream(cachePath + "/"+fileName);

                byte[] data = new byte[1024];

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
                Log.e("VR video Download", "Could not download vr video. error: "+e);
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
    //Activity Actions
    public void onDispatchKeyEvent(KeyEvent event, AudioManager audioManager){
        /*int action = event.getAction();
        int keyCode = event.getKeyCode();
        // Get the AudioManager instance

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    int current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    Log.e("show volume", ""+current_volume);
                }
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    int current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    Log.e("show volume", ""+current_volume);
                }
        }*/
    }
    public void onBackPressed(){}
}
