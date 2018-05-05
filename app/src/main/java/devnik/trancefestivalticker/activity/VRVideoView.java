package devnik.trancefestivalticker.activity;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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

import java.io.IOException;
import java.io.InputStream;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.GetBitmapFromURLAsync;

public class VRVideoView extends Fragment
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
    private Uri fileUri;

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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_vr_video_view, container, false);

        if(tabIsVisible) {
            loadVRVideo();
        }
        /*AssetManager assetManager = getActivity().getAssets();


        try {
            istr = assetManager.open("vr_test.jpg");
            panoOptions = new Options();
            panoOptions.inputType = Options.TYPE_MONO;
        } catch (IOException e) {
            Log.e(TAG, "Could not decode default bitmap: " + e);

        }

        //TODO Image Caching
        this.dialog = new ProgressDialog(getActivity());
        this.dialog.setMessage("Processing...");
        this.dialog.show();*/

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
                loadVRVideo();
            }
        }else{
            //Is unselected
            if(videoWidgetView != null){
                //Prevent continue if the neigbourgh tab is clicked
                videoWidgetView.pauseRendering();
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
    private void loadVRVideo(){
        try {

            seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            seekBar.setOnSeekBarChangeListener(new SeekBarListener());
            statusText = (TextView) view.findViewById(R.id.status_text);


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
            videoWidgetView.loadVideoFromAsset("myvideo_1.mp4", options);
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
    private void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        volumeToggle.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
        videoWidgetView.setVolume(isMuted ? 0.0f : 1.0f);
    }

    public boolean isMuted() {
        return isMuted;
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

    /**
     * Helper class to manage threading.
     */
    /*class VideoLoaderTask extends AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Pair<Uri, VrVideoView.Options>... fileInformation) {
            try {
                if (fileInformation == null || fileInformation.length < 1
                        || fileInformation[0] == null || fileInformation[0].first == null) {
                    // No intent was specified, so we default to playing the local stereo-over-under video.
                    VrVideoView.Options options = new VrVideoView.Options();
                    options.inputType = VrVideoView.Options.TYPE_MONO;
                    videoWidgetView.loadVideoFromAsset("myvideo.mp4", options);
                } else {
                    videoWidgetView.loadVideo(fileInformation[0].first, fileInformation[0].second);
                }
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

            return true;
        }
    }*/
}
