package ua.kaganovych.superlocationproject.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
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
import ua.kaganovych.superlocationproject.service.ActivityDetectionService;

public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private LocationRequest locationRequest;
    private static final int UPDATE_INTERVAL = 15 * 1000;
    private Context context;
    private GoogleApiClient googleApiClient;
    private Location currentLocation;
    private LocationFoundCallback locationFoundCallback;
    private String statusMessage;
    private String updateMessage;

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

    @SuppressWarnings("all")
    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (currentLocation != null) {
            // Print current location if not null
            Log.d("DEBUG", "current location: " + currentLocation.toString());
            locationFoundCallback.onLocationFound(currentLocation);
        }
        requestActivityUpdates();
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

    private void requestActivityUpdates() {
        statusMessage = "Activity detection succeeded"; // For debugging
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                googleApiClient,
                UPDATE_INTERVAL,
                getActivityDetectionPendingIntent()).setResultCallback(resultCallback);
    }

    public void removeActivityUpdates() {
        statusMessage = "Activity detection stopped"; // For debugging
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                googleApiClient,
                getActivityDetectionPendingIntent()).setResultCallback(resultCallback);
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        final Intent intent = new Intent(context, ActivityDetectionService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public Location getCurrentLocation() {
        return currentLocation;
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

    /**
    Debugging
     */
    @SuppressWarnings("all")
    private ResultCallback<Status> resultCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (status.isSuccess()) {
                Log.d("TAG", statusMessage);
                if (!TextUtils.isEmpty(updateMessage)) {
                    Log.d("TAG", updateMessage);
                }
                return;
            }
            Log.d("TAG", "resultCallbackFailed");
        }
    };

    public LocationRequest getRequest() {
        return locationRequest;
    }

    public void createLocationRequestAndStart(int priority, long interval, long fastestInterval) {
        if (locationRequest == null) {
            locationRequest = LocationRequest.create();
        }
        locationRequest.setPriority(priority);
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestInterval);
        startLocationUpdates();
    }

    @SuppressWarnings("all")
    public void startLocationUpdates() {
        updateMessage = "Location updates started"; // for debugging
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this).setResultCallback(resultCallback);
    }

    public void stopLocationUpdates() {
        updateMessage = "Location updates stopped"; // for debugging
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this).setResultCallback(resultCallback);
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }
}
