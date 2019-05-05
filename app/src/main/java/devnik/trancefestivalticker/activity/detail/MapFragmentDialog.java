package devnik.trancefestivalticker.activity.detail;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import java.util.List;
import java.util.Objects;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.helper.PermissionUtils;
import devnik.trancefestivalticker.model.Festival;
import devnik.trancefestivalticker.model.FestivalDetail;

import static com.crashlytics.android.Crashlytics.TAG;

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
    private Button showFestival;
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

        //if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_festival_map, container, false);
            /*ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);*/
        //}
        /*try {



        } catch (InflateException e) {
            //map is already there, just return view as it is
            return rootView;
        }*/
        showRoute = rootView.findViewById(R.id.showRoute);
        showFestival = rootView.findViewById(R.id.showFestival);
        sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);


        festival = (Festival) getArguments().getSerializable("festival");
        festivalDetail = (FestivalDetail) getArguments().getSerializable("festivalDetail");

        assert festivalDetail != null;
        if(festivalDetail.getGeoLatitude() != null && festivalDetail.getGeoLongitude() != null) {
            festivalLocation = new LatLng(festivalDetail.getGeoLatitude(), festivalDetail.getGeoLongitude());
        }else{
            Toast.makeText(getActivity(),"Keine Ortsangabe des Festivals vorhanden!",Toast.LENGTH_LONG).show();
        }

        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_view_map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        return rootView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MapFragment f = (MapFragment) Objects.requireNonNull(getActivity()).getFragmentManager().findFragmentById(R.id.fragment_view_map);
        if (f != null)
            getActivity().getFragmentManager().beginTransaction().remove(f).commit();
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
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(festivalLocation, 13));

            googleMap.addMarker(new MarkerOptions()
                    .title(festival.getName())
                    .position(festivalLocation));

            showFestival.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(festivalLocation, 13));
                }
            });
        }


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
            }
        });
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
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
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */

        try {
            if (!mPermissionDenied) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(Objects.requireNonNull(getActivity()), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations, this can be null.
                                if (location != null) {
                                    mLastKnownLocation = location;
                                    // Logic to handle location object
                                    if(carPolyline!=null){
                                        //Wenn es eine gecachte Polyline gibt
                                        carPolyline.remove();
                                    }
                                }else{
                                    Toast.makeText(getActivity(),"GPS Signal nicht verfügbar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void enableMyLocation() {


        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

        }
    }
    @Override
    public boolean onMyLocationButtonClick() {
        if(mLastKnownLocation==null) {
            getDeviceLocation();
            moveCameraToCurrentLocation();
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
        if(mLastKnownLocation == null){
            getDeviceLocation();
        }else {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
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

}


