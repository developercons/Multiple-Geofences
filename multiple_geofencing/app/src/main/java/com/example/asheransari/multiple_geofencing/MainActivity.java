package com.example.asheransari.multiple_geofencing;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private TextView textLat, textLong;

    private MapFragment mapFragment;

    List<LatLng> listLocation = null;

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    //office location..
//    private void initList() {
//        listLocation = new ArrayList<>();
//        listLocation.add(new LatLng(24.930312, 67.057874));
//        listLocation.add(new LatLng(24.930312, 67.057874));
//        listLocation.add(new LatLng(24.930359, 67.057711));
//        listLocation.add(new LatLng(24.930148, 67.057918));
//        listLocation.add(new LatLng(24.930051, 67.058007));
//        listLocation.add(new LatLng(24.930508, 67.057563));
//        listLocation.add(new LatLng(24.930597, 67.057017));
//    }

////    home location..
//    private void initList() {
//        listLocation = new ArrayList<>();
//        listLocation.add(new LatLng(24.899482, 67.046752));
//        listLocation.add(new LatLng(24.899570, 67.046874));
//        listLocation.add(new LatLng(24.899530, 67.047425));
//        listLocation.add(new LatLng(24.898946, 67.047393));
//        listLocation.add(new LatLng(24.898699, 67.047738));
//        listLocation.add(new LatLng(24.898868, 67.048098));
//        listLocation.add(new LatLng(24.899460, 67.048146));
//    }
//    home location..
    private void initList() {
        listLocation = new ArrayList<>();
        listLocation.add(new LatLng(24.899482, 67.046752));
        listLocation.add(new LatLng(24.899614, 67.046463));
        listLocation.add(new LatLng(24.899653, 67.046032));
        listLocation.add(new LatLng(24.899714, 67.045381));
        listLocation.add(new LatLng(24.899786, 67.044887));
        listLocation.add(new LatLng(24.899949, 67.044317));
        listLocation.add(new LatLng(24.899477, 67.044253));
        listLocation.add(new LatLng(24.899030, 67.044175));
        listLocation.add(new LatLng(24.898290, 67.044100));//dehli tabri house..
    }

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textLat = (TextView) findViewById(R.id.lat);
        textLong = (TextView) findViewById(R.id.lon);

        //initializing List.
        initList();
        // create GoogleApiClient
        createGoogleApi();

        // initialize GoogleMaps
        initGMaps();

//        //createMarkes..
//        if (map != null){
//            markerForGeofenceArrayList(listLocation);
//        }
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.e(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.geofence: {
                if (map != null && googleApiClient.isConnected()){
                    markerForGeofenceArrayList(listLocation);
                }else{
                    Toast.makeText(this, "Not execute", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
            case R.id.clear: {
//                clearGeofence();
                clearGeoFenceArrayList();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.e(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.e(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.e(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }

    // Initialize GoogleMaps
    private void initGMaps() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, "onMapReady()");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.e(TAG, "onMapClick(" + latLng + ")");
//        markerForGeofence(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "onMarkerClickListener: " + marker.getPosition());
        return false;
    }

    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;

    // Start location Updates
    private void startLocationUpdates() {
        Log.e(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
        writeActualLocation(location);
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected()");
        getLastKnownLocation();
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed()");
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.e(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.e(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.e(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    private void writeActualLocation(Location location) {
        textLat.setText("Lat: " + location.getLatitude());
        textLong.setText("Long: " + location.getLongitude());

    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }




    private ArrayList<Marker> geoFenceMarkerArrayList = new ArrayList<>();

    private void markerForGeofenceArrayList(List<LatLng> latLngs) {

        for (int i = 0; i < latLngs.size(); i++) {
            Log.e(TAG, "markerForGeofence(longitude:" + latLngs.get(i).longitude + ", and :latitude:" + latLngs.get(i).latitude + ")");
            String title = latLngs.get(i).latitude + ", " + latLngs.get(i).longitude;
            // Define marker options
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLngs.get(i))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title(title);

            if (map != null) {
                geoFenceMarkerArrayList.add(map.addMarker(markerOptions));
//                float zoom = 14f;
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngs.get(i), zoom);
//                map.animateCamera(cameraUpdate);
            }
            Log.e(TAG,"size: "+geoFenceMarkerArrayList.size());
        }

        startGeodenceForArrayList();
    }

    // Start Geofence creation process
    private void startGeodenceForArrayList() {
        Log.e(TAG, "startGeofence()");
        if (geoFenceMarkerArrayList != null && geoFenceMarkerArrayList.size() != 0) {
            for (int i = 0; i < geoFenceMarkerArrayList.size(); i++) {
                Geofence geofence = createGeofence(geoFenceMarkerArrayList.get(i).getPosition(), GEOFENCE_RADIUS);
                GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
                addGeofence(geofenceRequest);
            }
        } else {
            Log.e(TAG, "Geofence marker is null");
        }

    }


    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    //    private static final float GEOFENCE_RADIUS = 500.0f; // in meters
    private static final float GEOFENCE_RADIUS = 15.0f; // in meters


    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.e(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.e(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.e(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.e(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.e(TAG, "onResult: " + status);

        if (status.isSuccess()) {
            drawGeofence(geoFenceMarkerArrayList);
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private ArrayList<Circle> getFenceLimitArrayList = new ArrayList<>();

    private void drawGeofence(ArrayList<Marker> geoFenceMarkerArrayList) {
        if (getFenceLimitArrayList.size() == 0) {
            for (int i = 0; i < geoFenceMarkerArrayList.size(); i++) {
                CircleOptions circleOptions = new CircleOptions()
                        .center(geoFenceMarkerArrayList.get(i).getPosition())
                        .strokeColor(Color.argb(50, 70, 70, 70))
                        .fillColor(Color.argb(100, 150, 150, 150))
                        .radius(GEOFENCE_RADIUS);
                getFenceLimitArrayList.add(map.addCircle(circleOptions));
            }
        }
    }


    private void clearGeoFenceArrayList() {
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, createGeofencePendingIntent())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            geoFenceMarkerArrayList.clear();
                            getFenceLimitArrayList.clear();
                            map.clear();
                        }
                    }
                });
    }


}
