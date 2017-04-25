package com.capstone.petros.cmsc436msdetector;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class WalkingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings uiSettings;

    private boolean locationDenied = false;
    private boolean testOngoing = false;
    private boolean foundLocation = false;

    private static final int MY_LOCATION_REQUEST_CODE = 1;
    LocationManager locationManager = null;
    LocationListener locationListener;

    private long prevTime = -1, timeSinceLastUpdateVel = 0, timeSinceLastUpdateLine = 0, timeSinceSigLost = 0;
    private LatLng prevLatLngLine;
    private double prevLatVel = 0, prevLongVel = 0;
    private float totalSpeed = 0;
    private float averageSpeed = 0;
    private long totalTimeRecorded = 0;
    private boolean lostSig = false;

    /***DEBUG***/
    private ArrayList<LatLng> pointList;
    CountDownTimer fakePointsTimer;
    /***********/

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
        //test points (Sydney)
//        pointList.add(new LatLng(-34, 151));
//        pointList.add(new LatLng(-33, 151));
//        pointList.add(new LatLng(-30, 154));
//        pointList.add(new LatLng(-28, 160));

        //test points (View to AV Williams)
        pointList.add(new LatLng(38.992646, -76.935111));
        pointList.add(new LatLng(38.992849, -76.935565));
        pointList.add(new LatLng(38.992670, -76.935814));
        pointList.add(new LatLng(38.991975, -76.935788));
        pointList.add(new LatLng(38.991263, -76.935747));
        pointList.add(new LatLng(38.990754, -76.936133));

        // REMOVE (Mark's phone cannot get a location update for some reason)
        Button startButton = (Button) findViewById(R.id.startTestBtn);
        startButton.setEnabled(true);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(),"Got a location update",Toast.LENGTH_LONG).show();
                handleLocationUpdates(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("Status change?");
            }

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }

    private void handleLocationUpdates(Location location) {
        long currTime = System.currentTimeMillis();
        long deltaTime = currTime - prevTime;
        if(location == null) {
            // Can't find location!
            System.out.println("No location yet...");
            if(testOngoing && prevTime != -1){
                if(!lostSig) {
                    lostSig = true;
                    timeSinceSigLost = 0;
                }
                else {
                    timeSinceSigLost += deltaTime;
                }
                if(timeSinceSigLost > 10000){ // 10 seconds w/o signal
                    testOngoing = false;// TODO(MARK): ABORT TEST
                    Toast.makeText(this,"This test requires GPS",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        else {
            System.out.println("Lat: " + location.getLatitude() + " Long: "+location.getLongitude());

            if(!foundLocation){
                foundLocation = true;

                // TODO(MARK): Enable test to begin.
                Button startButton = (Button) findViewById(R.id.startTestBtn);
                startButton.setEnabled(true);
            }
            if(testOngoing && prevTime != -1){
                if(lostSig){
                    // Just got signal back. Don't count this towards speed.
                    timeSinceSigLost += deltaTime;
                    if(timeSinceSigLost > 10000) { // 10 seconds w/o signal
                        testOngoing = false;// TODO(MARK): ABORT TEST
                        Toast.makeText(this,"This test requires GPS",Toast.LENGTH_LONG).show();
                        finish();
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
                        totalTimeRecorded += deltaTime;
                        averageSpeed = totalSpeed / totalTimeRecorded;                          // TODO(Mark): Average speed = totalSpeed / totalTimeRecorded.
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
                Toast.makeText(this,"This test requires GPS",Toast.LENGTH_LONG).show(); // TODO MARK Abort
                System.out.println("Location seems to be denied...");
            }
        }
    }

    public void saveMap() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                try {
                    // Need to be at a new folder
                    String fName = UUID.randomUUID().toString() + ".png";
                    File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Walking Test Results");
                    if(!folder.exists()){
                        if(!folder.mkdirs()){
                            System.out.println("Folder creation failed...");
                        }
                    }
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Walking Test Results/"+fName);

                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    ContentValues values = new ContentValues();

                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());

                    getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    // Clean up the map and execute post-end-of-test protocol
                    mMap.clear();
                    Button startButton = (Button) findViewById(R.id.startTestBtn);
                    startButton.setEnabled(true);
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
            Log.d("MARK","Registering Location Updates");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);   // With permission, start listening
        }  else {
            if (!locationDenied) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        , MY_LOCATION_REQUEST_CODE);
            }
            else {
                // End the test
                Toast.makeText(this,"This test requires GPS",Toast.LENGTH_LONG).show(); // TODO MARK Abort
                System.out.println("Location seems to be denied...");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        testOngoing = false;
        locationManager.removeUpdates(locationListener);
        locationManager = null;

        fakePointsTimer.cancel();
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

    public void oldDrawPoints() {
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

    public void drawPoints() {
        // MARK CODE: Generate the fake points around your last known location
        int timerInterval = 4000;
        fakePointsTimer = new CountDownTimer(timerInterval*pointList.size(),timerInterval) {
            int c = 0;

            @Override
            public void onTick(long l) {
                PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for(int i = 0; i < 2+c; i++){
                    LatLng point = pointList.get(i);
                    options.add(point);
                    builder.include(point);
                }

                mMap.addPolyline(options);

                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));

                c++;
            }

            @Override
            public void onFinish() {
                // After the fake data is done, finish the test
                endTest(null);
            }
        };
        fakePointsTimer.start();
    }

    public void startTest(View v) {
        averageSpeed = 0;
        prevLatVel = 0;
        prevLongVel = 0;
        totalTimeRecorded = 0;
        prevTime = -1;
        timeSinceLastUpdateVel = 0;
        timeSinceLastUpdateLine = 0;
        timeSinceSigLost = 0;

        testOngoing = true;

        /********DEBUG***********/
        drawPoints();
        averageSpeed = 2.0f;
        /***********************/

        Button startButton = (Button) findViewById(R.id.startTestBtn);
        Button endButton = (Button) findViewById(R.id.endTestBtn);

        startButton.setEnabled(false);
        endButton.setEnabled(true);
    }

    /*
     *  Called when the test ends
     */
    public void endTest(View v) {
        testOngoing = false;

        Button endButton = (Button) findViewById(R.id.endTestBtn);

        endButton.setEnabled(false);

        // Display results
        AlertDialog builder;
        builder = new AlertDialog.Builder(this).create();
        builder.setTitle("Test Ended");
        builder.setMessage("Test complete. Your average speed was "+averageSpeed+" m/s.");
        builder.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                saveMap();
            }
        });

        builder.show();
    }
//
//    /*
//     *  Called when the test should be interrupted
//     */
//    public void abortTest() {
//        testOngoing = false;
//    }

    public void showTutorial(View v) {
        FrameLayout frame = (FrameLayout)findViewById(R.id.walkingFrame);
        TextView tutorialView = (TextView) findViewById(R.id.walkingInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.walkingShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.walkingTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the walking test.\n\n" +
                    "It measures your average speed while you walk.\n\n" +
                    "Ensure that you are outside and have a GPS signal when performing this test.\n\n" +
                    "Press \"Start\" and start walking to begin.");
            shader.setVisibility(View.VISIBLE);
            tutorialButton.setColorFilter(0xFFF6FF00);
        }
        else {
            frame.setVisibility(View.GONE);
            shader.setVisibility(View.GONE);
            tutorialButton.setColorFilter(0xFF000000);
        }
    }
}
