package devnik.trancefestivalticker.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.GetBitmapFromURLAsync;

public class VRView extends DialogFragment
        implements GetBitmapFromURLAsync.GetBitmapFromURLCompleted {
    private static final String TAG = VRView.class.getSimpleName();
    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;
    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    public boolean loadImageSuccessful;
    private ProgressDialog dialog;

    private Options panoOptions = new Options();
    private View view;
    private InputStream istr = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_vr_view, container, false);

        // Make the source link clickable.
        //TextView sourceText = (TextView) view.findViewById(R.id.source);
        //sourceText.setText(Html.fromHtml(getString(R.string.source)));
        //sourceText.setMovementMethod(LinkMovementMethod.getInstance());

        panoWidgetView = (VrPanoramaView) view.findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());

        // Initial launch of the app or an Activity recreation due to rotation.
        //handleIntent(getIntent());
        AssetManager assetManager = getActivity().getAssets();


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
        this.dialog.show();
        new GetBitmapFromURLAsync("https://firebasestorage.googleapis.com/v0/b/macro-duality-197512.appspot.com/o/atWorkMono.jpg?alt=media&token=aa91e7eb-1482-49c5-8c4b-071b727b3135",
                this).execute();

        try {
            istr.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close input stream: " + e);
        }
        return view;
    }
    @Override
    public void onGetBitmapFromURLCompleted(Bitmap bitmap){
        panoWidgetView.loadImageFromBitmap(bitmap, panoOptions);
        this.dialog.hide();
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
                    VRView.this.getContext(), "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }
}
