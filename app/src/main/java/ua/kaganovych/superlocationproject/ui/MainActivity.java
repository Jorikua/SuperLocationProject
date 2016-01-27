package ua.kaganovych.superlocationproject.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ua.kaganovych.superlocationproject.Config;
import ua.kaganovych.superlocationproject.R;
import ua.kaganovych.superlocationproject.api.ApiHelper;
import ua.kaganovych.superlocationproject.api.response.DirectionResponse;
import ua.kaganovych.superlocationproject.model.Direction;
import ua.kaganovych.superlocationproject.util.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    private MapFragment mapFragment;
    private LocationHelper locationHelper;
    private GoogleMap googleMap;
    private Marker finishMarker;
    private Marker myMarker;
    private Polyline polyline;
    private int lastType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationHelper = new LocationHelper(this, locationFoundCallback);
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mapFragment == null) {
            mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            if (mapFragment != null) {
                mapFragment.getMapAsync(onMapReadyCallback);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationHelper.connectClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateLocationReceiver, new IntentFilter(Config.ACTIVITY_TYPE_ACTION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateLocationReceiver);
        locationHelper.stopLocationUpdates();
        locationHelper.removeActivityUpdates();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.disconnectClient();
    }

    private BroadcastReceiver updateLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int type = intent.getIntExtra("type", DetectedActivity.UNKNOWN);
            switch (type) {
                case DetectedActivity.STILL:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        if (lastType == DetectedActivity.STILL) return;
                            lastType = DetectedActivity.STILL;
                            locationHelper.stopLocationUpdates();
                            locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                                    10000,
                                    7000);
                            Log.d("TAG", "STILL");
                            Toast.makeText(getBaseContext(), "STILL " +
                                    locationHelper.getRequest().getInterval() + " " +
                                    locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();

                    }
                    break;
                case DetectedActivity.WALKING:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        if (lastType == DetectedActivity.WALKING) return;
                        lastType = DetectedActivity.WALKING;
                        locationHelper.stopLocationUpdates();
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY,
                                7000,
                                5000);
                        Log.d("TAG", "WALKING");
                        Toast.makeText(getBaseContext(), "WALKING " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.ON_FOOT:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        if (lastType == DetectedActivity.ON_FOOT) return;
                        lastType = DetectedActivity.ON_FOOT;
                        locationHelper.stopLocationUpdates();
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY,
                                7000,
                                5000);
                        Log.d("TAG", "ON_FOOT");
                        Toast.makeText(getBaseContext(), "ON_FOOT " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.RUNNING:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        if (lastType == DetectedActivity.RUNNING) return;
                        lastType = DetectedActivity.RUNNING;
                        locationHelper.stopLocationUpdates();
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY,
                                11000,
                                10000);
                        Log.d("TAG", "RUNNING");
                        Toast.makeText(getBaseContext(), "RUNNING " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.TILTING:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        if (lastType == DetectedActivity.TILTING) return;
                        lastType = DetectedActivity.TILTING;
                        locationHelper.stopLocationUpdates();
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY,
                                9000,
                                7000);
                        Log.d("TAG", "TILTING");
                        Toast.makeText(getBaseContext(), "TILTING " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.UNKNOWN:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        if (lastType == DetectedActivity.UNKNOWN) return;
                        lastType = DetectedActivity.UNKNOWN;
                        locationHelper.stopLocationUpdates();
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY,
                                9000,
                                7000);
                        Log.d("TAG", "UNKNOWN");
                        Toast.makeText(getBaseContext(), "UNKNOWN " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            MainActivity.this.googleMap = googleMap;
            googleMap.setOnMapClickListener(onMapClickListener);
            googleMap.setOnInfoWindowClickListener(onInfoWindowClickListener);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
        }
    };

    private LocationHelper.LocationFoundCallback locationFoundCallback = new LocationHelper.LocationFoundCallback() {
        @Override
        public void onLocationFound(Location location) {
            if (googleMap != null) {
                addMarker(new LatLng(location.getLatitude(), location.getLongitude()), googleMap, true);
                Log.d("TAG", locationHelper.getCurrentLocation().toString());
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            if (googleMap != null) {
                addMarker(new LatLng(location.getLatitude(), location.getLongitude()), googleMap, false);
            }
        }
    };

    private Marker addMarker(LatLng location, GoogleMap googleMap, boolean animate) {
        if (myMarker != null) {
            myMarker.remove();
        }
        myMarker = googleMap.addMarker(new MarkerOptions().position(location));
        myMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin));
        if (animate) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
        return myMarker;
    }

    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (finishMarker != null) {
                finishMarker.remove();
            }
            finishMarker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
            .title("Do you want to get here?")
            .snippet("Click on window to lay the route!"));
            finishMarker.showInfoWindow();
        }
    };

    private GoogleMap.OnInfoWindowClickListener onInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            if (!NetworkUtils.isInternetAvailable(getBaseContext())) {
                Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            final Direction direction = new Direction();
            direction.fromLon = locationHelper.getCurrentLocation().getLongitude();
            direction.fromLat = locationHelper.getCurrentLocation().getLatitude();
            direction.toLon = marker.getPosition().longitude;
            direction.toLat = marker.getPosition().latitude;
            new ApiHelper(getBaseContext()).getDirection(direction, directionResponseCallback);
        }
    };

    private Callback<DirectionResponse> directionResponseCallback = new Callback<DirectionResponse>() {
        @Override
        public void success(DirectionResponse directionResponse, Response response) {

            if (directionResponse.polylineList == null) {
                Toast.makeText(getBaseContext(), "No route found", Toast.LENGTH_SHORT).show();
                return;
            }

            if (polyline != null) {
                polyline.remove();
            }

            final PolylineOptions polylineOptions = new PolylineOptions()
                    .width(15)
                    .color(ContextCompat.getColor(getBaseContext(), R.color.colorAccent))
                    .geodesic(true);
            for (LatLng latLng : directionResponse.polylineList) {
                polylineOptions.add(latLng);
            }
            polyline = googleMap.addPolyline(polylineOptions);
        }

        @Override
        public void failure(RetrofitError error) {

        }
    };
}
