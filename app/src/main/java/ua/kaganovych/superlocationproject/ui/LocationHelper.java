package ua.kaganovych.superlocationproject.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ua.kaganovych.superlocationproject.R;

public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int UPDATE_INTERVAL = 10 * 1000;
    private static final int FASTEST_INTERVAL = 5000;
    private Context context;
    private GoogleApiClient googleApiClient;
    public Location currentLocation;
    private LocationRequest locationRequest;
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
                .build();
        createLocationRequest();
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

    public void startLocationUpdates() {
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
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
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
        startLocationUpdates();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
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

    protected void createLocationRequest() {
        // Create the location request
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        Log.d("TAG", currentLocation.toString());
    }

    public interface LocationFoundCallback {
        void onLocationFound(Location location);
    }
}
