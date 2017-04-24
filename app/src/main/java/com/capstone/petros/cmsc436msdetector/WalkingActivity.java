package com.capstone.petros.cmsc436msdetector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class WalkingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings uiSettings;
    private ArrayList<LatLng> pointList;

    private boolean locationDenied = false;
    private boolean testOngoing = true;
    private boolean foundLocation = false;

    private static final int MY_LOCATION_REQUEST_CODE = 1;
    LocationManager locationManager = null;
    LocationListener locationListener;

    private long prevTime = -1, timeSinceLastUpdateVel = 0, timeSinceLastUpdateLine = 0, timeSinceSigLost = 0;
    private LatLng prevLatLngLine;
    private double prevLatVel = 0, prevLongVel = 0;
    private float totalSpeed = 0;
    private long totalTimeRecored = 0;
    private boolean lostSig = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pointList = new ArrayList<>();
        //test points
        pointList.add(new LatLng(-34, 151));
        pointList.add(new LatLng(-33, 151));
        pointList.add(new LatLng(-30, 154));
        pointList.add(new LatLng(-28, 160));


        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                handleLocationUpdates(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("Status change?");
            }

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }

    private void handleLocationUpdates(Location location){
        long currTime = System.currentTimeMillis();
        long deltaTime = currTime - prevTime;
        TextView lat = (TextView)findViewById(R.id.walking_lat);
        TextView longi = (TextView)findViewById(R.id.walking_long);
        TextView vel = (TextView)findViewById(R.id.walking_vel);
        if(totalTimeRecored != 0){
            vel.setText("Vel: " + (totalSpeed/totalTimeRecored));
        }
        if(location == null) {
            // Can't find location!
            System.out.println("No location yet...");
            lat.setText("Geting lat still...");
            longi.setText("Geting long still...");
            if(testOngoing && prevTime != -1){
                if(!lostSig) {
                    lostSig = true;
                    timeSinceSigLost = 0;
                }
                else {
                    timeSinceSigLost += deltaTime;
                }
                if(timeSinceSigLost > 10000){ // 10 seconds w/o signal
                    // TODO(MARK): ABORT TEST
                }
            }
        }
        else {
            System.out.println("Lat: " + location.getLatitude() + " Long: "+location.getLongitude());
            lat.setText("Lat: " + location.getLatitude());
            longi.setText(" Long: "+location.getLongitude());

            if(!foundLocation){
                foundLocation = true;
                // TODO(MARK): Enable test to begin.
            }
            if(testOngoing && prevTime != -1){
                if(lostSig){
                    // Just got signal back. Don't count this towards speed.
                    timeSinceSigLost += deltaTime;
                    if(timeSinceSigLost > 10000) { // 10 seconds w/o signal
                        // TODO(MARK): ABORT TEST
                    }
                    else {
                        addPolyLine(prevLatLngLine, new LatLng(location.getLatitude(),location.getLongitude()), true);
                        prevLatLngLine = new LatLng(location.getLatitude(),location.getLongitude());
                        prevLatVel = location.getLatitude();
                        prevLongVel = location.getLongitude();
                        timeSinceLastUpdateLine = 0;
                        timeSinceLastUpdateVel = 0;
                    }
                }
                else if(prevLatLngLine == null){
                    prevLatLngLine = new LatLng(location.getLatitude(),location.getLongitude());
                    prevLatVel = location.getLatitude();
                    prevLongVel = location.getLongitude();
                    timeSinceLastUpdateLine = 0;
                    timeSinceLastUpdateVel = 0;
                }
                else {
                    timeSinceLastUpdateVel += deltaTime;
                    timeSinceLastUpdateLine += deltaTime;
                    if(timeSinceLastUpdateVel >= 100) { //Every Tenth of a second
                        float [] results = new float[1];
                        Location.distanceBetween(prevLatVel, prevLongVel,
                                location.getLatitude(), location.getLongitude(), results);
                        totalSpeed += results[0];
                        totalTimeRecored += deltaTime;
                        // TODO(Mark): Average speed = totalSpeed / totalTimeRecorded.
                        prevLatVel = location.getLatitude();
                        prevLongVel = location.getLongitude();
                        timeSinceLastUpdateVel = 0;
                    }
                    if(timeSinceLastUpdateLine >= 2000) { //Every 2 seconds
                        addPolyLine(prevLatLngLine, new LatLng(location.getLatitude(),location.getLongitude()), false);
                        prevLatLngLine = new LatLng(location.getLatitude(),location.getLongitude());
                        timeSinceLastUpdateLine = 0;
                    }
                }
            }

        }
        prevTime = currTime;
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        uiSettings = mMap.getUiSettings();
        //uiSettings.setZoomGesturesEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
        } else {
            if (!locationDenied) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        , MY_LOCATION_REQUEST_CODE);
            }
        }


        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Test
        /*
        Location l1 = new Location(LocationManager.GPS_PROVIDER);
        l1.setLatitude(0); l1.setLongitude(0);
        handleLocationUpdates(l1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){}
        Location l2 = new Location(LocationManager.GPS_PROVIDER);
        l2.setLatitude(50); l2.setLongitude(50);
        handleLocationUpdates(l2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){}
        Location l3 = new Location(LocationManager.GPS_PROVIDER);
        l3.setLatitude(75); l3.setLongitude(75);
        handleLocationUpdates(l3);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e){}
        Location l4 = new Location(LocationManager.GPS_PROVIDER);
        l4.setLatitude(-50); l4.setLongitude(-50);
        handleLocationUpdates(l4);
        */
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                locationDenied = true;
                // End the test
                System.out.println("Location seems to be denied...");
            }
        }

    }

    public void saveMap(View v) {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                try {
                    // Need to be at a new folder
                    FileOutputStream out = new FileOutputStream("/mnt/sdcard/Download/TeleSensors.png");
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mMap.snapshot(callback);

    }
    @Override
    protected void onResume(){
        super.onResume();
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }  else {
            if (!locationDenied) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        , MY_LOCATION_REQUEST_CODE);
            }
            else {
                // End the test
                System.out.println("Location seems to be denied...");
            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        locationManager.removeUpdates(locationListener);
        locationManager = null;
    }

    public void addPolyLine(LatLng point1, LatLng point2, boolean signalLost){
        PolylineOptions options = new PolylineOptions().width(5).geodesic(true);
        if(signalLost){
            options = options.color(Color.RED);
        }
        else {
            options = options.color(Color.BLUE);
        }
        options.add(point1);
        options.add(point2);
        mMap.addPolyline(options);
    }

    public void drawPoints(View v) {
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(int i = 0; i < pointList.size(); i++){
            LatLng point = pointList.get(i);
            options.add(point);
            builder.include(point);
        }

        mMap.addPolyline(options);

        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
    }



}
