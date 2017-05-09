package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
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

import com.capstone.petros.cmsc436msdetector.Sheets.Sheets;

import java.text.DecimalFormat;

public class FlexActivity extends Activity implements Sheets.Host {
    SensorEventListener sel;
    SensorManager sensorManager;
    Sensor accelerometer, magnetometer;
    private float roll = -1;
    private Sheets sheet;
    public static final int LIB_ACCOUNT_NAME_REQUEST_CODE = 1001;
    public static final int LIB_AUTHORIZATION_REQUEST_CODE = 1002;
    public static final int LIB_PERMISSION_REQUEST_CODE = 1003;
    public static final int LIB_PLAY_SERVICES_REQUEST_CODE = 1004;
    public static final int LIB_CONNECTION_REQUEST_CODE = 1005;
    private static final int STATE_IN_FIRST_HALF = 0,
            STATE_IN_SECOND_HALF = 1,
            STATE_OUT_FIRST_HALF = 2,
            STATE_OUT_SECOND_HALF = 3;
    private int state = STATE_IN_FIRST_HALF;

    private static final int GIVE = 15, MID_GIVE = 15;  // The "give" to be considered at the start or end
    private Vibrator vibrator;                          // Provides feedback upon a complete cycle
    private static final int VIBRATE_DURATION = 500;
    TextView flexCompleteCount, flexIncompleteCount;
    MediaPlayer mediaPlayer;
    boolean doneRightTest = false, doneBothTests = false;

    private boolean touchedShoulder = false;
    private int completedCycles = 0, incompletedCycles = 0;

    private boolean testInProgress = false, down = false, released = true;
    private long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flex);

        sheet = new Sheets(this, this, getString(R.string.app_name));
        sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        flexCompleteCount = (TextView)findViewById(R.id.flexCompleteText);
        flexIncompleteCount = (TextView)findViewById(R.id.flexIncompleteText);

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        doneRightTest = false;

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
                        if(down && released && !testInProgress && roll < 15 && !doneBothTests){
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

        FragmentManager fragmentManager = getFragmentManager();
        FlexInstructionFragment frag = new FlexInstructionFragment();

        Bundle bundle = new Bundle();
        bundle.putString(InstructionFragment.MESSAGE_KEY,"Retract and extend your left arm 10 times.");
        frag.setArguments(bundle);

        frag.show(fragmentManager, null);
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
        int startPoint = 0, endpoint = 180, midpoint = (endpoint + startPoint) / 2; // The start/end points
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

                    // If right's not done, start that
                    if(!doneRightTest) {
                        // First, save the results
                        ((TextView)findViewById(R.id.flexResultsText2)).setText("      Completed Cycles: "+completedCycles);
                        ((TextView)findViewById(R.id.flexResultsText3)).setText("      Incomplete Cycles: "+incompletedCycles);
                        ((TextView)findViewById(R.id.flexResultsText4)).setText("      Time Taken: "+(totalTime/1000.0)+"s");
                        sendToSheets(Sheets.TestType.LH_FLEX, totalTime/1000.0);
                        doneRightTest = true;
                        FragmentManager fragmentManager = getFragmentManager();
                        FlexInstructionFragment frag = new FlexInstructionFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString(InstructionFragment.MESSAGE_KEY,"Retract and extend your right arm 10 times.");
                        frag.setArguments(bundle);

                        frag.show(fragmentManager, null);
                    }
                    else {
                        // First, save the results
                        ((TextView)findViewById(R.id.flexResultsText6)).setText("      Completed Cycles: "+completedCycles);
                        ((TextView)findViewById(R.id.flexResultsText7)).setText("      Incomplete Cycles: "+incompletedCycles);
                        ((TextView)findViewById(R.id.flexResultsText8)).setText("      Time Taken: "+(totalTime/1000.0)+"s");
                        sendToSheets(Sheets.TestType.RH_FLEX, totalTime/1000.0);
                        FragmentManager fragmentManager = getFragmentManager();
                        FlexInstructionFragment frag = new FlexInstructionFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString(InstructionFragment.MESSAGE_KEY,"Test completed. Hit 'continue' to view scores.");
                        frag.setArguments(bundle);

                        frag.show(fragmentManager, null);

                        // Show the scores now.
                        findViewById(R.id.flexScreen).setVisibility(View.GONE);
                        findViewById(R.id.flexScores).setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    private void sendToSheets(Sheets.TestType sheetType, double result) {
        //sheet.writeData(sheetType, getString(R.string.patientID), (float)result);
        sheet.writeTrials(sheetType, getString(R.string.patientID), (float)result);
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
                    "To begin the test, simply touch the screen with the in your arm fully extended, " +
                    "then start retracting and extending.");
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
        finish();
    }

    @Override
    public int getRequestCode(Sheets.Action action) {
        switch (action) {
            case REQUEST_ACCOUNT_NAME:
                return LIB_ACCOUNT_NAME_REQUEST_CODE;
            case REQUEST_AUTHORIZATION:
                return LIB_AUTHORIZATION_REQUEST_CODE;
            case REQUEST_PERMISSIONS:
                return LIB_PERMISSION_REQUEST_CODE;
            case REQUEST_PLAY_SERVICES:
                return LIB_PLAY_SERVICES_REQUEST_CODE;
            case REQUEST_CONNECTION_RESOLUTION:
                return LIB_CONNECTION_REQUEST_CODE;
            default:
                return -1;
        }
    }

    @Override
    public void notifyFinished(Exception e) {

    }
}
