package ua.kaganovych.superlocationproject.ui;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

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
import ua.kaganovych.superlocationproject.R;
import ua.kaganovych.superlocationproject.api.ApiHelper;
import ua.kaganovych.superlocationproject.api.response.DirectionResponse;
import ua.kaganovych.superlocationproject.model.Direction;

public class MainActivity extends AppCompatActivity {

    private MapFragment mapFragment;
    private LocationHelper locationHelper;
    private GoogleMap googleMap;
    private Marker finishMarker;
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        locationHelper = new LocationHelper(this, locationFoundCallback);
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mapFragment == null) {
            mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            // Check if we were successful in obtaining the map.
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
        if (locationHelper.getGoogleApiClient().isConnected()) {
            locationHelper.startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationHelper.getGoogleApiClient().isConnected()) {
            locationHelper.stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.disconnectClient();
    }

    private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            MainActivity.this.googleMap = googleMap;
            googleMap.setOnMapClickListener(onMapClickListener);
            googleMap.setOnInfoWindowClickListener(onInfoWindowClickListener);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
        }
    };

    private Marker addMarker(LatLng location, GoogleMap googleMap) {
        final Marker marker = googleMap.addMarker(new MarkerOptions().position(location));
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
        return marker;
    }

    private LocationHelper.LocationFoundCallback locationFoundCallback = new LocationHelper.LocationFoundCallback() {
        @Override
        public void onLocationFound(Location location) {
            if (googleMap != null) {
                addMarker(new LatLng(location.getLatitude(), location.getLongitude()), googleMap);
                Log.d("TAG", locationHelper.getCurrentLocation().toString());
            }
        }
    };

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
