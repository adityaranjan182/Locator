package com.example.locator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ArrayList<LatLng> locationTrails;
    private  LocationListener locationListener;
    private LocationManager locationManager;
    public Button bluetooth;
    //public Location location;
    private final long minTime = 300000l;
    private final Float minDist = 5f;
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    Polyline polyline = null;
    public static LatLng updated;
    public static String timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetooth = findViewById(R.id.bluetooth);
        setContentView(R.layout.activity_maps);
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        locationTrails = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

//        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        // adding polylines
        final PolylineOptions polylineOptions = new PolylineOptions().addAll(locationTrails).clickable(true);
        polyline = googleMap.addPolyline(polylineOptions);
        polyline.setWidth(12f);
        currentLocation();
        polylineOptions.add(updated);
        polyline = mMap.addPolyline(polylineOptions);
        polyline.setColor(Color.rgb(164,83,38));
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updated = new LatLng(location.getLatitude(), location.getLongitude());

                // adding new updated location
                locationTrails.add(updated);
                //adding marker
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                 timeStamp  = dateFormat.format(new Date());

                mMap.addMarker(new MarkerOptions().position(updated).title("Timestamp : " + timeStamp));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(updated,18.0f));

                //adding new polyline
                polylineOptions.add(updated);
                polyline = mMap.addPolyline(polylineOptions);
                polyline.setColor(Color.rgb(164,83,38));
                DocumentReference docref = fstore.collection(fauth.getCurrentUser().getPhoneNumber()).document(timeStamp);
                Map<String,LatLng> currentlocation = new HashMap<>();
                currentlocation.put(timeStamp,updated);
                docref.set(currentlocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //requesting location updates for location listener
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //setting location update time and distance
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                // GPS enabled
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDist, locationListener);
            }else {
                // GPS not enabled
                Toast.makeText(this, "GPS Service is disabled", Toast.LENGTH_SHORT).show();
                showGPSDisabledAlert();
            }
        }catch (SecurityException | NullPointerException e) {
                Toast.makeText(this,"GPS services is disabled",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
        }
    }

    private void showGPSDisabledAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS Services is disabled.\nWould you like to enable it?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public void showdevices(View view){
        bluetooth = findViewById(R.id.bluetooth);
        startActivity(new Intent(getApplicationContext(),ListOfDevices.class));
    }

    public void currentLocation(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timeStamp  = dateFormat.format(new Date());
        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double lat = current.getLatitude();
            double lang = current.getLongitude();
            updated = new LatLng(lat,lang);
            locationTrails.add(updated);
            MarkerOptions options = new MarkerOptions().position(updated).title("Start Time"+" "+timeStamp);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(options);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(updated,18.0f));

        }catch (SecurityException |  NullPointerException e ){
            e.printStackTrace();
        }
    }
}