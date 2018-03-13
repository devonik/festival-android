package devnik.trancefestivalticker.activity;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Distance;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.DistanceMatrixRow;

import org.greenrobot.greendao.query.Query;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import devnik.trancefestivalticker.App;
import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.CustomExceptionHandler;
import devnik.trancefestivalticker.helper.PermissionUtils;
import devnik.trancefestivalticker.model.DaoSession;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import devnik.trancefestivalticker.model.FestivalDetailDao;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
/**
 * Created by nik on 09.03.2018.
 */

public class MapFragmentDialog extends DialogFragment  implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private GoogleMap mMap;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationClient;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private Button showRoute;
    private static final int DEFAULT_ZOOM = 15;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;
    private Polyline carPolyline;
    private Festival festival;
    private LatLng festivalLocation;
    private FestivalDetailDao festivalDetailDao;
    private Query<FestivalDetail> festivalDetailQuery;
    private FestivalDetail festivalDetail;
    public MapFragmentDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_festival_map, container, false);
        showRoute = rootView.findViewById(R.id.showRoute);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        festival = (Festival) getArguments().getSerializable("festival");
        DaoSession daoSession = ((App)getActivity().getApplication()).getDaoSession();
        festivalDetailDao = daoSession.getFestivalDetailDao();
        festivalDetailQuery = festivalDetailDao.queryBuilder().where(FestivalDetailDao.Properties.Festival_id.eq(festival.getFestival_id())).build();
        festivalDetail = festivalDetailQuery.unique();


        festivalLocation = new LatLng(festivalDetail.getGeoLatitude(),festivalDetail.getGeoLongitude());


        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_view_map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    //Register Custom Exception Handler
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(getActivity()));
        return rootView;
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        // Get the current location of the device and set the position of the map.
        enableMyLocation();


        LatLng circus = new LatLng(53.301641, 11.346728);

        //googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(festivalLocation, 13));

        googleMap.addMarker(new MarkerOptions()
                .title(festival.getName())
                .position(festivalLocation));

        String preferenceEncodedCarPath = sharedPref.getString(getString(R.string.devnik_trancefestivalticker_preference_map_car_route), "");

        if(preferenceEncodedCarPath != ""){
            List<LatLng> list = PolyUtil.decode(preferenceEncodedCarPath);
            LatLng latLng = list.get(list.size()-1);
            carPolyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .color(Color.RED)
                    .geodesic(true)
            );
        }
        showRoute.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                getDeviceLocation();
                if(festivalLocation == null){
                    Toast.makeText(getActivity(),"Die Position des Festivals ist nicht hinterlegt", Toast.LENGTH_SHORT).show();
                }
                else if(mLastKnownLocation == null){
                    Toast.makeText(getActivity(),"GPS Signal nicht verfügbar", Toast.LENGTH_SHORT).show();
                }else{
                    carPolyline.remove();
                    getDirections();
                }

            }
        });
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */

        try {
            if (!mPermissionDenied) {

                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(festivalLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }

                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void enableMyLocation() {


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            //getDistance();

        }
    }
    @Override
    public boolean onMyLocationButtonClick() {

        Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    /*@Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }*(

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getFragmentManager(), "dialog");
    }
    private void getDirections(){

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyAIQW5j7KSy--4ITxKDQTfWMc-pis_iyPs")
                .build();
        System.out.print(mLastKnownLocation);
        com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(festivalDetail.getGeoLatitude(),festivalDetail.getGeoLongitude());
        DirectionsApiRequest req = DirectionsApi.newRequest(context)
                .origin(origin)
                .destination(destination);
        // Synchronous
        try {
            DirectionsResult result = req.await();
            // Handle successful request.
            for(int i = 0; i<result.routes.length;i++){
                String distance = result.routes[i].legs[i].distance.toString();
                String time = result.routes[i].legs[i].duration.toString();
                IconGenerator iconFactory = new IconGenerator(getActivity());
                iconFactory.setRotation(90);
                iconFactory.setContentRotation(-90);
                iconFactory.setStyle(IconGenerator.STYLE_BLUE);
                addIcon(iconFactory, "Das Ziel ist "+distance+" von dir entfernt\n und dauert ca. "+time+" mit dem Auto", new LatLng(result.routes[i].legs[i].startLocation.lat, result.routes[i].legs[i].startLocation.lng));
            }
            String encodedPath = result.routes[0].overviewPolyline.getEncodedPath();
            List<LatLng> list = PolyUtil.decode(encodedPath);
            mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .color(Color.RED)
                    .geodesic(true)
                    );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

            //Shared Preference
            sharedPrefEditor.putString(getString(R.string.devnik_trancefestivalticker_preference_map_car_route), encodedPath);
            sharedPrefEditor.commit();
        } catch (Exception e) {
            // Handle error
        }

    }
    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        mMap.addMarker(markerOptions);
    }

}


