package devnik.trancefestivalticker.activity;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.GetBitmapFromURLAsync;
import devnik.trancefestivalticker.helper.PermissionUtils;

public class VRPanoView extends Fragment
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = VRPanoView.class.getSimpleName();
    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;
    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    public boolean loadImageSuccessful;
    private Options panoOptions = new Options();
    private View view;
    boolean _areLecturesLoaded = false;
    // Progress Dialog
    private ProgressDialog pDialog;
    private boolean tabIsVisible = false;
    //Permission
    private int REQUEST_STORAGE = 1;
    private boolean mPermissionDenied = false;

    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri photoUri;
    // File url to download
    private static String file_url = "https://niklas-grieger.de/files/360panorma/atWorkMono.jpg";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_vr_pano_view, container, false);

        // Make the source link clickable.
        //TextView sourceText = (TextView) view.findViewById(R.id.source);
        //sourceText.setText(Html.fromHtml(getString(R.string.source)));
        //sourceText.setMovementMethod(LinkMovementMethod.getInstance());


        //Need check, cuz onCreateView is called even if the neigbourgh tab is clicked... cuz pagerview cache it
        if(tabIsVisible) {
            enableStoragePermission();
            String filename = "myphoto.jpg";
            String path = Environment
                    .getExternalStorageDirectory().toString()
                    + "/"+filename;
            File f = new File(path);  //
            if(f.exists()){
                loadVRPano();
            }else{
                downloadVRPano();
            }

        }
        panoWidgetView = (VrPanoramaView) view.findViewById(R.id.pano_view);

        return view;
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

        super.onDestroy();
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
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            tabIsVisible = true;
            if(view != null){
                enableStoragePermission();
                String filename = "myphoto.jpg";
                String path = Environment
                        .getExternalStorageDirectory().toString()
                        + "/"+filename;
                File f = new File(path);  //
                if(f.exists()){
                    loadVRPano();
                }else{
                    downloadVRPano();
                }
            }

        }else{
            tabIsVisible = false;
        }
    }
    public void downloadVRPano(){
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Foto wird geladen... Bitte warten");
        pDialog.setIndeterminate(true);
        //pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCancelable(true);
        pDialog.show();
        new DownloadFileFromURL().execute(file_url);
    }
    public void loadVRPano(){

        //TODO Image Caching
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        String filename = "myphoto.jpg";
        String path = Environment
                .getExternalStorageDirectory().toString()
                + "/"+filename;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        panoWidgetView = (VrPanoramaView) view.findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());
        panoOptions = new Options();
        panoOptions.inputType = Options.TYPE_MONO;
        panoWidgetView.loadImageFromBitmap(bitmap,panoOptions);
    }
    /*@Override
    public void onGetBitmapFromURLCompleted(Bitmap bitmap){
        panoWidgetView = (VrPanoramaView) view.findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());
        panoWidgetView.loadImageFromBitmap(bitmap, panoOptions);
        pDialog.hide();
    }*/
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
                        + "/myphoto.jpg");

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
            loadVRPano();
        }

    }
}
