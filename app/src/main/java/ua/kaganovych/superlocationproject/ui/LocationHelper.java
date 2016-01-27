package ua.kaganovych.superlocationproject.ui;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ua.kaganovych.superlocationproject.R;
import ua.kaganovych.superlocationproject.service.LocationUpdateService;

public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private LocationRequest locationRequest;
    private static final int UPDATE_INTERVAL = 15 * 1000;
    private static final int FASTEST_INTERVAL = 5000;
    private Context context;
    private GoogleApiClient googleApiClient;
    public Location currentLocation;
    private LocationFoundCallback locationFoundCallback;

    public LocationHelper(Context context, LocationFoundCallback locationFoundCallback) {
        this.context = context;
        this.locationFoundCallback = locationFoundCallback;
        buildGoogleApiClient(context);
    }

    private void buildGoogleApiClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
    }

    public void connectClient() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    public void disconnectClient() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (currentLocation != null) {
            // Print current location if not null
            Log.d("DEBUG", "current location: " + currentLocation.toString());
            locationFoundCallback.onLocationFound(currentLocation);
        }
        requestActivityUpdates();
    }

    private void requestActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                googleApiClient,
                UPDATE_INTERVAL,
                getActivityDetectionPendingIntent()).setResultCallback(resultCallback);
    }

    public void removeActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                googleApiClient,
                getActivityDetectionPendingIntent()).setResultCallback(resultCallback);
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        final Intent intent = new Intent(context, LocationUpdateService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(context, context.getString(R.string.error_location_disconnected), Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(context, context.getString(R.string.error_location_network_lost), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("all")
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            connectClient();
            return;
        }
        Toast.makeText(context, context.getString(R.string.error_services_not_available), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        Log.d("TAG", currentLocation.toString());
        if (locationFoundCallback == null) return;
        locationFoundCallback.onLocationChanged(location);
    }

    public interface LocationFoundCallback {
        void onLocationFound(Location location);
        void onLocationChanged(Location location);
    }

    private ResultCallback<Status> resultCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (status.isSuccess()) {
                Log.d("TAG", "resultCallbackSucceeded");
                return;
            }
            Log.d("TAG", "resultCallbackFailed");
        }
    };

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    public LocationRequest getRequest() {
        return locationRequest;
    }

    public void createLocationRequestAndStart(int priority, int interval, int fastestInterval) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(priority);
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestInterval);
        Log.d("Priority", String.valueOf(locationRequest.getPriority()));
        startLocationUpdates();
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }
}
