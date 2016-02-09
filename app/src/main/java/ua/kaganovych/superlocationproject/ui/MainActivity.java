package ua.kaganovych.superlocationproject.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import ua.kaganovych.superlocationproject.util.MapUtils;
import ua.kaganovych.superlocationproject.util.NetworkUtils;

public class MainActivity extends AppCompatActivity {

    private MapFragment mapFragment;
    private LocationHelper locationHelper;
    private GoogleMap googleMap;
    private Marker finishMarker;
    private Marker myMarker;
    private LatLng finishLatLng;
    private LatLng myLatLng;
    private Polyline polyline;
    private PolylineOptions polylineOptions;
    private static final int POLYLINE_WIDTH = 15;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 123;
    private static final int REQUEST_CODE_GPS = 111;
    private Snackbar snackbar;
    private boolean enableAnimation;
    private Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!NetworkUtils.isInternetAvailable(this)) {
            Toast.makeText(getBaseContext(), getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!MapUtils.isGPSEnabled(this)) {
            showGPSSnackbar();
            return;
        }
        locationHelper = new LocationHelper(this, locationFoundCallback);
        setUpMapIfNeeded();
        this.savedInstanceState = savedInstanceState;
    }

    private void setUpMapIfNeeded() {
        if (mapFragment == null) {
            mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            mapFragment.setRetainInstance(true);
            if (mapFragment != null) {
                mapFragment.getMapAsync(onMapReadyCallback);
            }
        }
    }

    private void updateValuesFromBundle(Bundle bundle) {
        if (bundle == null) return;
        if (googleMap == null) return;
        if (bundle.keySet().contains("polylineOptions")) {
            polylineOptions = bundle.getParcelable("polylineOptions");
            polyline = googleMap.addPolyline((PolylineOptions) bundle.getParcelable("polylineOptions"));
        }
        if (bundle.keySet().contains("finishLatLng")) {
            finishLatLng = bundle.getParcelable("finishLatLng");
            addFinishMarker(finishLatLng);
        }
        if (bundle.keySet().contains("myLatLng")) {
            myLatLng = bundle.getParcelable("myLatLng");
            addMarker(myLatLng, googleMap, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (polylineOptions != null) {
            outState.putParcelable("polylineOptions", polylineOptions);
        }
        if (finishLatLng != null) {
            outState.putParcelable("finishLatLng", finishLatLng);
        }
        if (myLatLng != null) {
            outState.putParcelable("myLatLng", myLatLng);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtils.isInternetAvailable(this)) {
            Toast.makeText(getBaseContext(), getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!MapUtils.isGPSEnabled(this)) {
            showGPSSnackbar();
            return;
        }
        if (locationHelper == null) {
            locationHelper = new LocationHelper(this, locationFoundCallback);
            setUpMapIfNeeded();
        }
        checkForPermissions();
    }

    private void checkForPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            locationHelper.connectClient();
            return;
        }
        final int hasLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            return;
        }
        locationHelper.connectClient();
    }

    @SuppressWarnings("all")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationHelper.connectClient();
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    final NoPermissionDialog noPermissionDialog = new NoPermissionDialog();
                    noPermissionDialog.show(getFragmentManager(), "NoPermissionDialog");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(updateLocationReceiver, new IntentFilter(Config.ACTIVITY_TYPE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateLocationReceiver);
        if (locationHelper == null) return;
        if (locationHelper.getGoogleApiClient().isConnected()) {
            locationHelper.stopLocationUpdates();
            locationHelper.removeActivityUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationHelper == null) return;
        locationHelper.disconnectClient();
    }

    private BroadcastReceiver updateLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int type = intent.getIntExtra("type", DetectedActivity.UNKNOWN);
            switch (type) {
                case DetectedActivity.STILL:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS - 5000);
                        //for debugging
                        Log.d("TAG", "STILL");
                        Toast.makeText(getBaseContext(), "STILL " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.WALKING:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY, Config.INTERVAL, Config.FASTEST_INTERVAL);
                        //for debugging
                        Log.d("TAG", "WALKING");
                        Toast.makeText(getBaseContext(), "WALKING " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.ON_FOOT:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY, Config.INTERVAL, Config.FASTEST_INTERVAL);
                        //for debugging
                        Log.d("TAG", "ON_FOOT");
                        Toast.makeText(getBaseContext(), "ON_FOOT " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.RUNNING:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY, Config.INTERVAL, Config.FASTEST_INTERVAL);
                        //for debugging
                        Log.d("TAG", "RUNNING");
                        Toast.makeText(getBaseContext(), "RUNNING " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.TILTING:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY, Config.INTERVAL, Config.FASTEST_INTERVAL);
                        //for debugging
                        Log.d("TAG", "TILTING");
                        Toast.makeText(getBaseContext(), "TILTING " +
                                locationHelper.getRequest().getInterval() + " " +
                                locationHelper.getRequest().getFastestInterval(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.UNKNOWN:
                    if (locationHelper.getGoogleApiClient().isConnected()) {
                        locationHelper.createLocationRequestAndStart(LocationRequest.PRIORITY_HIGH_ACCURACY, Config.INTERVAL, Config.FASTEST_INTERVAL);
                        //for debugging
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
            googleMap.clear();
            googleMap.setOnMapClickListener(onMapClickListener);
            googleMap.setOnInfoWindowClickListener(onInfoWindowClickListener);
            updateValuesFromBundle(savedInstanceState);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
        }
    };

    private LocationHelper.LocationFoundCallback locationFoundCallback = new LocationHelper.LocationFoundCallback() {
        @Override
        public void onLocationFound(Location location) {
            if (googleMap == null) return;
            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            addMarker(myLatLng, googleMap, true);
            Log.d("TAG", locationHelper.getCurrentLocation().toString());

        }

        @Override
        public void onLocationChanged(Location location) {
            if (googleMap == null) return;
            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            addMarker(myLatLng, googleMap, enableAnimation);
            enableAnimation = false;
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
            finishLatLng = latLng;
            addFinishMarker(finishLatLng);
        }
    };

    private void addFinishMarker(LatLng latLng) {
        if (latLng == null) return;

        if (finishMarker != null) {
            finishMarker.remove();
        }
        finishMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                .title(getString(R.string.text_info_window_title))
                .snippet(getString(R.string.text_info_window_message)));
        finishMarker.showInfoWindow();
    }

    private GoogleMap.OnInfoWindowClickListener onInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            if (!NetworkUtils.isInternetAvailable(getBaseContext())) {
                Toast.makeText(getBaseContext(), getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getBaseContext(), getString(R.string.error_no_route), Toast.LENGTH_SHORT).show();
                return;
            }

            if (polyline != null) {
                polyline.remove();
            }

            polylineOptions = new PolylineOptions()
                    .width(POLYLINE_WIDTH)
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

    private void showGPSSnackbar() {
        snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.text_enable_gps), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.text_click), snackBarListener);
        snackbar.setActionTextColor(Color.RED);
        final View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.DKGRAY);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private View.OnClickListener snackBarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            enableAnimation = true;
            if (snackbar != null) {
                snackbar.dismiss();
            }
            final Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, REQUEST_CODE_GPS);
        }
    };
}
