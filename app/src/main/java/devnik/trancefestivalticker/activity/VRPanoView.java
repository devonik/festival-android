package devnik.trancefestivalticker.activity;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;

import java.io.IOException;
import java.io.InputStream;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.GetBitmapFromURLAsync;

public class VRPanoView extends Fragment
        implements GetBitmapFromURLAsync.GetBitmapFromURLCompleted {
    private static final String TAG = VRPanoView.class.getSimpleName();
    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;
    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    public boolean loadImageSuccessful;
    private ProgressDialog dialog;
    private ProgressBar progressBar;
    private Options panoOptions = new Options();
    private View view;
    private InputStream istr = null;
    boolean _areLecturesLoaded = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View progressBaseView = inflater.inflate(R.layout.progress_bar, container, false);
        progressBar = (ProgressBar) progressBaseView.findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_IN);

        view = inflater.inflate(R.layout.fragment_vr_pano_view, container, false);

        // Make the source link clickable.
        //TextView sourceText = (TextView) view.findViewById(R.id.source);
        //sourceText.setText(Html.fromHtml(getString(R.string.source)));
        //sourceText.setMovementMethod(LinkMovementMethod.getInstance());

        panoWidgetView = (VrPanoramaView) view.findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());

        // Initial launch of the app or an Activity recreation due to rotation.
        //handleIntent(getIntent());
        /*AssetManager assetManager = getActivity().getAssets();


        try {
            istr = assetManager.open("vr_test.jpg");
            panoOptions = new Options();
            panoOptions.inputType = Options.TYPE_MONO;
        } catch (IOException e) {
            Log.e(TAG, "Could not decode default bitmap: " + e);

        }*/


        /*try {
            istr.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close input stream: " + e);
        }*/
        return view;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !_areLecturesLoaded ) {
            loadVRPano();
            _areLecturesLoaded = true;
        }
    }
    public void loadVRPano(){
        //TODO Image Caching
        new GetBitmapFromURLAsync("https://firebasestorage.googleapis.com/v0/b/macro-duality-197512.appspot.com/o/SAM_100_0032.jpg?alt=media&token=518a6d15-fb12-4a5d-8933-3c5c1ef1cc7e",
                this).execute();

    }
    @Override
    public void onGetBitmapFromURLCompleted(Bitmap bitmap){
        panoWidgetView.loadImageFromBitmap(bitmap, panoOptions);
        progressBar.setVisibility(View.GONE);
    }
    @Override
    public void onPause() {
        panoWidgetView.pauseRendering();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        panoWidgetView.resumeRendering();
    }

    @Override
    public void onDestroy() {
        // Destroy the widget and free memory.
        panoWidgetView.shutdown();

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        /*if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }*/
        super.onDestroy();
    }
    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    VRPanoView.this.getActivity(), "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }
}
