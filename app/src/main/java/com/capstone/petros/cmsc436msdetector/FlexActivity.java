package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

    private static final int GIVE = 15, MID_GIVE = 10;  // The "give" to be considered at the start or end
    private Vibrator vibrator;                          // Provides feedback upon a complete cycle
    private static final int VIBRATE_DURATION = 500;
    TextView flexCompleteCount, flexIncompleteCount;
    MediaPlayer mediaPlayer;

    private boolean touchedShoulder = false;
    private int completedCycles = 0, incompletedCycles = 0;

    private boolean testInProgress = false, down = false, released = true;
    private long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flex);

        sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        flexCompleteCount = (TextView)findViewById(R.id.flexCompleteText);
        flexIncompleteCount = (TextView)findViewById(R.id.flexIncompleteText);

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
                        //((TextView)findViewById(R.id.flexRollText)).setText("Roll: "+roll);
                        if(down && released && !testInProgress && roll < 15){
                            // Clear old test
                            resetTest();
                            // Start the test
                            testInProgress = true;
                            startTime = System.currentTimeMillis();
                        }
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
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    down = true;
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    down = false;
                    released = true;
                }
                return true;
            }
        });

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void resetTest(){
        state = STATE_IN_FIRST_HALF;
        roll = -1;
        touchedShoulder = false;
        completedCycles = 0;
        incompletedCycles = 0;

        testInProgress = false;
        startTime = 0;

        flexCompleteCount.setText("0");
        flexIncompleteCount.setText("0");
    }

    private void rollUpdated(float roll){
        // Since going "in" all the way isn't exactly 180 degrees...
        // 80 will be the midpoint.
        int startPoint = 0, endpoint = 160, midpoint = (endpoint + startPoint) / 2; // The start/end points
        switch (state){
            case STATE_IN_FIRST_HALF:
                if(roll > midpoint + MID_GIVE){
                    state = STATE_IN_SECOND_HALF;
                }
                break;
            case STATE_IN_SECOND_HALF:
                // Can either touch shoulder, or not go all the way.
                if(roll > endpoint - GIVE){
                    // Touched shoulder
                    touchedShoulder = true;
                    state = STATE_OUT_FIRST_HALF;
                }
                else if(roll < midpoint - MID_GIVE){
                    // Didn't touch shoulder, flexing out now.
                    touchedShoulder = false;
                    state = STATE_OUT_SECOND_HALF;
                }
                break;
            case STATE_OUT_FIRST_HALF:
                if(roll < midpoint - MID_GIVE){
                    state = STATE_OUT_SECOND_HALF;
                }
                break;
            case STATE_OUT_SECOND_HALF:
                // Can either go all the way out, or start to come back early.
                if(roll < startPoint + GIVE){
                    // One cycle complete here.
                    if(touchedShoulder){
                        completedCycles++;

                        flexCompleteCount.setText(""+completedCycles);
                        // Provide tactile feedback
                        vibrator.vibrate(VIBRATE_DURATION);
                    }
                    else {
                        incompletedCycles++;

                        flexIncompleteCount.setText(""+incompletedCycles);
                    }
                    state = STATE_IN_FIRST_HALF;
                }
                else if (roll > midpoint + MID_GIVE){
                    incompletedCycles++;
                    flexIncompleteCount.setText(""+incompletedCycles);

                    state = STATE_IN_SECOND_HALF;
                }

                if(completedCycles + incompletedCycles >= 10){
                    long totalTime = System.currentTimeMillis() - startTime;
                    testInProgress = false;
                    if(down){
                        released = false;
                    }
                    state = STATE_IN_FIRST_HALF;

                    // Play the completion sound:
                    if(mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    mediaPlayer = MediaPlayer.create(this, R.raw.completion);
                    mediaPlayer.start();
                }
                break;
        }
    }

    public void showTutorial(View v) {
        FrameLayout frame = (FrameLayout)findViewById(R.id.flexFrame);
        TextView tutorialView = (TextView) findViewById(R.id.flexInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.flexShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.flexTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the flex test.\n\n" +
                    "It measures how quickly you can extend and retract your arm.\n\n" +
                    "Retract then extend your arm 10 times.\n\n" +
                    "Try to keep your wrist fixed and make sure you touch your shoulder.\n\n" +
                    "To begin the test, simply touch the screen then start retracting and extending.");
            shader.setVisibility(View.VISIBLE);
            tutorialButton.setColorFilter(0xFFF6FF00);
        }
        else {
            frame.setVisibility(View.GONE);
            shader.setVisibility(View.GONE);
            tutorialButton.setColorFilter(0xFF000000);
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
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        sensorManager.unregisterListener(sel);
    }
}
