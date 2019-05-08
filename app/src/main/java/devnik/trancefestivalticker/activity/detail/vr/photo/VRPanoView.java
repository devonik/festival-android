package devnik.trancefestivalticker.activity.detail.vr.photo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.PermissionUtils;
import devnik.trancefestivalticker.model.FestivalVrView;

public class VRPanoView extends DialogFragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = VRPanoView.class.getSimpleName();
    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;
    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    public boolean loadImageSuccessful;
    private View view;
    boolean _areLecturesLoaded = false;
    // Progress Dialog
    private ProgressDialog pDialog;
    private boolean tabIsVisible = false;
    //Permission
    private int REQUEST_STORAGE = 2;
    private boolean mPermissionDenied = false;

    private FestivalVrView vrView;
    private String fileName;
    private String cachePath;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_vr_pano_view, container, false);

        assert getArguments() != null;
        vrView = (FestivalVrView) getArguments().getSerializable("photoVrView");

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        /*cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable() ? Objects.requireNonNull(Objects.requireNonNull(getActivity()).getExternalCacheDir()).getPath() :
                Objects.requireNonNull(getActivity()).getCacheDir().getPath();*/
        cachePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/tftApp/360/photos";
        File folderToSave = new File(cachePath);
        if(!folderToSave.exists())
            folderToSave.mkdirs();

        if(tabIsVisible) {
            //Falls die nötige Berechtigung noch nicht vorhanden ist, wird erneut gefragt
            enableStoragePermission();

        }

        panoWidgetView = view.findViewById(R.id.pano_view);

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
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Enable the my location layer if the permission has been granted.
            downloadVRPano();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }
    private void enableStoragePermission() {

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the write external storage is missing.
            PermissionUtils.requestPermission(getActivity(), REQUEST_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE, true);

        } else{
            // Access to the external storage has been granted to the app.
            try {
                URL url = new URL(vrView.getUrl());
                fileName = FilenameUtils.getName(url.getPath());

                //Need check, cuz onCreateView is called even if the neigbourgh tab is clicked... cuz pagerview cache it
                // Check if media is mounted or storage is built-in, if so, try and use external cache dir
                // otherwise use internal cache dir

                String path = cachePath + "/"+fileName;
                File f = new File(path);  //
                if(f.exists()){
                    loadVRPano(path);
                }else{
                    downloadVRPano();
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            tabIsVisible = true;
            if(view != null){
                enableStoragePermission();
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
        new DownloadFileFromURL().execute(vrView.getUrl());
    }
    public void loadVRPano(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        panoWidgetView = view.findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());
        Options panoOptions = new Options();
        panoOptions.inputType = Options.TYPE_MONO;
        panoWidgetView.loadImageFromBitmap(bitmap, panoOptions);
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
            String path = cachePath + "/"+fileName;
            loadVRPano(path);
        }

    }
}
