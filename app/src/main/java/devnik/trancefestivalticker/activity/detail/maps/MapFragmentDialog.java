package devnik.trancefestivalticker.activity.detail.maps;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.model.DirectionsResult;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.MainActivity;
import devnik.trancefestivalticker.helper.PermissionUtils;
import devnik.trancefestivalticker.helper.animation.CustomBounceInterpolator;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;
import io.fabric.sdk.android.Logger;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by nik on 09.03.2018.
 */

public class MapFragmentDialog extends DialogFragment implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = MapFragmentDialog.class.getSimpleName();
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.

    //**************My Location**************//
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationClient;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private View myLocationButton;
    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";


    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    //**************My Location END**************//
    private ImageButton showRoute;
    private ImageButton showFestival;
    private static final int DEFAULT_ZOOM = 15;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPref;
    private Polyline carPolyline;
    private Festival festival;
    private FestivalDetail festivalDetail;
    private LatLng festivalLocation;
    private View rootView;

    public MapFragmentDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        updateValuesFromBundle(savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_festival_map, container, false);

        showRoute = rootView.findViewById(R.id.showRoute);
        showFestival = rootView.findViewById(R.id.showFestival);
        sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);


        assert getArguments() != null;
        festival = (Festival) getArguments().getSerializable("festival");
        festivalDetail = (FestivalDetail) getArguments().getSerializable("festivalDetail");

        assert festivalDetail != null;
        if (festivalDetail.getGeoLatitude() != null && festivalDetail.getGeoLongitude() != null) {
            festivalLocation = new LatLng(festivalDetail.getGeoLatitude(), festivalDetail.getGeoLongitude());
        } else {
            Toast.makeText(getActivity(), "Keine Ortsangabe des Festivals vorhanden!", Toast.LENGTH_LONG).show();
        }

        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_view_map);
        mapFragment.getMapAsync(this);


        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLastLocation();


        mSettingsClient = LocationServices.getSettingsClient(getActivity());

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        //createLocationCallback();
        //createLocationRequest();
        buildLocationSettingsRequest();



        return rootView;
    }
    private Location getLastLocation(){
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.e("MAP CURRENT LOCATION", "location: "+location);
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            mLastKnownLocation = location;
                            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                        }else if(mLastKnownLocation == null){
                            Toast.makeText(getActivity(),"GPS Signal nicht verfügbar", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return mLastKnownLocation;
    }
    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e(TAG, "onLocationResult: "+locationResult);
                super.onLocationResult(locationResult);

                mLastKnownLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            }
        };
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MapFragment f = (MapFragment) Objects.requireNonNull(getActivity()).getFragmentManager().findFragmentById(R.id.fragment_view_map);
        if (f != null)
            getActivity().getFragmentManager().beginTransaction().remove(f).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mLastKnownLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(Objects.requireNonNull(getActivity()), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    Activity#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for Activity#requestPermissions for more details.
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });
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


        if(festivalLocation!=null){
            //Falls eine Location in der DB eingetragen ist
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(festivalLocation, DEFAULT_ZOOM));

            googleMap.addMarker(new MarkerOptions()
                    .title(festival.getName())
                    .position(festivalLocation));

            showFestival.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(festivalLocation, DEFAULT_ZOOM));
                    startButtonBounceAnimation(view);
                }
            });
        }
        myLocationButton = ((View) rootView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        String preferenceEncodedCarPath = sharedPref.getString(getString(R.string.devnik_trancefestivalticker_preference_map_car_route), "");

        if(!preferenceEncodedCarPath.equals("")){
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
                getRouteToFestival();
                startButtonBounceAnimation(view);
            }
        });
    }
    private void startButtonBounceAnimation(View view){
        final Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
        // Use bounce interpolator with amplitude 0.2 and frequency 20
        CustomBounceInterpolator interpolator = new CustomBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        view.startAnimation(myAnim);
    }
    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if(rootView!=null) {
                // Get the current location of the device and set the position of the map.
                enableMyLocation();
            }
        }
    }

    private void enableMyLocation() {


        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

        }
    }
    @Override
    public boolean onMyLocationButtonClick() {
        if(mLastKnownLocation!=null) {
                if(carPolyline!=null){
                    //Wenn es eine gecachte Polyline gibt
                    carPolyline.remove();
                }
                moveCameraToCurrentLocation();
                startButtonBounceAnimation(myLocationButton);
        }
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
    private void moveCameraToCurrentLocation(){
        if(mLastKnownLocation!=null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }
    }
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Log.i("Maps:", "onMyLocationClick");
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

    private void getRouteToFestival(){
        if(getLastLocation() == null){
            Toast.makeText(getActivity(),"Die Route kann nicht berechnet werden. GPS Signal nicht verfügbar", Toast.LENGTH_SHORT).show();
        }else {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_direction_api))
                    .build();
            com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(festivalDetail.getGeoLatitude(), festivalDetail.getGeoLongitude());
            DirectionsApiRequest req = DirectionsApi.newRequest(context)
                    .origin(origin)
                    .destination(destination)
                    .language("de");
            // Synchronous
            try {
                DirectionsResult result = req.await();
                // Handle successful request.
                for (int i = 0; i < result.routes.length; i++) {
                    String distance = result.routes[i].legs[i].distance.toString();
                    String time = result.routes[i].legs[i].duration.toString();
                    IconGenerator iconFactory = new IconGenerator(getActivity());
                    iconFactory.setRotation(90);
                    iconFactory.setContentRotation(-90);
                    iconFactory.setStyle(IconGenerator.STYLE_BLUE);
                    addIcon(iconFactory, "Das Ziel ist " + distance + " von dir entfernt\n und dauert ca. " + time + " mit dem Auto", new LatLng(result.routes[i].legs[i].startLocation.lat, result.routes[i].legs[i].startLocation.lng));
                }
                String encodedPath = result.routes[0].overviewPolyline.getEncodedPath();
                List<LatLng> list = PolyUtil.decode(encodedPath);
                mMap.addPolyline(new PolylineOptions()
                        .addAll(list)
                        .color(Color.RED)
                        .geodesic(true)
                );
                moveCameraToCurrentLocation();

                //Shared Preference
                SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();
                sharedPrefEditor.putString(getString(R.string.devnik_trancefestivalticker_preference_map_car_route), encodedPath);
                sharedPrefEditor.apply();
            } catch (Exception e) {
                // Handle error
                Log.e("Festival Route", "Could not calc festival route: "+e);
            }
        }

    }
    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        mMap.addMarker(markerOptions);
    }

    private void updateUI(){
        assert getFragmentManager() != null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(this).attach(this).commit();
    }
}


