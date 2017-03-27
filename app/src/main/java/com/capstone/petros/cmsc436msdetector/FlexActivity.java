package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FlexActivity extends Activity {

    SensorEventListener sel;
    SensorManager sensorManager;
    Sensor accelerometer, magnetometer;
    private float roll = -1;

    private static final int STATE_IN_FIRST_HALF = 0,
            STATE_IN_SECOND_HALF = 1,
            STATE_OUT_FIRST_HALF = 2,
            STATE_OUT_SECOND_HALF = 3;
    private int state = STATE_IN_FIRST_HALF;

    private boolean touchedShoulder = false;
    private int completedCycles = 0, incompletedCycles = 0;

    private boolean testInProgress = false;
    private long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flex);

        sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sel = new SensorEventListener() {
            float[] mGravity;
            float[] mGeomagnetic;
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    mGravity = event.values;
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    mGeomagnetic = event.values;
                if (mGravity != null && mGeomagnetic != null) {
                    float Ro[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(Ro, I, mGravity, mGeomagnetic);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(Ro, orientation);
                        // Azimuth = orientation[0], pitch = [1], roll = [2]
                        // The roll is the one we care about.
                        roll = (float)(orientation[2] * (180/Math.PI)); // convert to degs.
                        roll = Math.abs(roll); // Orientation of roll doesn't matter
                        ((TextView)findViewById(R.id.flexRollText)).setText("Roll: "+roll);
                        if(testInProgress) {
                            rollUpdated(roll);
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {/*cough*/}
        };

        // detect when the user has touched the screen
        LinearLayout screen = (LinearLayout)findViewById(R.id.flexScreen);
        screen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN && !testInProgress
                        && roll < 15){
                    // Clear old test
                    resetTest();
                    // Start the test
                    testInProgress = true;
                    startTime = System.currentTimeMillis();
                }
                return true;
            }
        });

    }

    private void resetTest(){
        state = STATE_IN_FIRST_HALF;
        roll = -1;
        touchedShoulder = false;
        ((TextView)findViewById(R.id.flexTestText)).setText("Waiting...");
        completedCycles = 0;
        ((TextView)findViewById(R.id.flexCompleteText)).setText("Completed cycles: 0");
        incompletedCycles = 0;
        ((TextView)findViewById(R.id.flexIncompleteText)).setText("Incomplete cycles: 0");

        testInProgress = false;
        startTime = 0;
        ((TextView)findViewById(R.id.flexTimeText)).setText("Time: ...");
    }

    private void rollUpdated(float roll){
        // Since going "in" all the way isn't exactly 180 degrees...
        // 80 will be the midpoint.
        int startPoint = 0, endpoint = 160, midpoint = (endpoint + startPoint) / 2; // The start/end points
        int give = 15, midGive = 10; // The "give" to be considered at the start or end
        switch (state){
            case STATE_IN_FIRST_HALF:
                if(roll > midpoint + midGive){
                    state = STATE_IN_SECOND_HALF;
                }
                break;
            case STATE_IN_SECOND_HALF:
                // Can either touch shoulder, or not go all the way.
                if(roll > endpoint - give){
                    // Touched shoulder
                    touchedShoulder = true;
                    state = STATE_OUT_FIRST_HALF;
                }
                else if(roll < midpoint - midGive){
                    // Didn't touch shoulder, flexing out now.
                    touchedShoulder = false;
                    state = STATE_OUT_SECOND_HALF;
                }
                break;
            case STATE_OUT_FIRST_HALF:
                if(roll < midpoint - midGive){
                    state = STATE_OUT_SECOND_HALF;
                }
                break;
            case STATE_OUT_SECOND_HALF:
                // Can either go all the way out, or start to come back early.
                if(roll < startPoint + give){
                    // One cycle complete here.
                    if(touchedShoulder){
                        ((TextView)findViewById(R.id.flexTestText)).setText("Complete cycle detected");
                        completedCycles++;
                        ((TextView)findViewById(R.id.flexCompleteText)).setText("Completed cycles: "+completedCycles);
                    }
                    else {
                        ((TextView)findViewById(R.id.flexTestText)).setText("Incomplete cycle detected");
                        incompletedCycles++;
                        ((TextView)findViewById(R.id.flexIncompleteText)).setText("Incomplete cycles: "+incompletedCycles);
                    }
                    state = STATE_IN_FIRST_HALF;
                }
                else if (roll > midpoint + midGive){
                    ((TextView)findViewById(R.id.flexTestText)).setText("Incomplete cycle detected");
                    incompletedCycles++;
                    ((TextView)findViewById(R.id.flexIncompleteText)).setText("Incomplete cycles: "+incompletedCycles);
                    state = STATE_IN_SECOND_HALF;
                }

                if(completedCycles + incompletedCycles >= 10){
                    long totalTime = System.currentTimeMillis() - startTime;
                    ((TextView)findViewById(R.id.flexTimeText)).setText("Time: "+totalTime);
                    testInProgress = false;
                    state = STATE_IN_FIRST_HALF;
                }
                break;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(sel, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sel, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop(){
        super.onStop();
        sensorManager.unregisterListener(sel);
    }
}
