package com.capstone.petros.cmsc436msdetector;

import android.app.FragmentManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.capstone.petros.cmsc436msdetector.Sheets.Sheets;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

public class BallActivity extends Activity implements TimedActivity, Sheets.Host {
    private Sheets sheet;
    public static final int LIB_ACCOUNT_NAME_REQUEST_CODE = 1001;
    public static final int LIB_AUTHORIZATION_REQUEST_CODE = 1002;
    public static final int LIB_PERMISSION_REQUEST_CODE = 1003;
    public static final int LIB_PLAY_SERVICES_REQUEST_CODE = 1004;
    public static final int LIB_CONNECTION_REQUEST_CODE = 1005;
    SensorEventListener sel;
    SensorManager sensorManager;
    Sensor accelerometer, magnetometer;
    Activity activity = this;
    BallView ballView;
    TextView threeSecondCountdownText;
    boolean doneRightTest = false;

    public static final String BALL_TEST_DATA_FILENAME = "ball_test_data";

    // 3 second timer that counts down before the test starts
    CountDownTimer prepTimer = new CountDownTimer(3100, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            threeSecondCountdownText.setText(Long.toString(millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            ballView.toggleTestActive(true);
            threeSecondCountdownText.setText("");
            testTimer.start();
        }
    };

    CountDownTimer testTimer = new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {    // Start the next test if you still need to
            ballView.toggleTestActive(false);

            if(!doneRightTest) {
                doneRightTest = true;
                ballView.saveLeftHandFscore();
                ((BallView)findViewById(R.id.ballView)).savePathToGallery(true);
                ((BallView)findViewById(R.id.ballView)).resetTest();
                FragmentManager fragmentManager = getFragmentManager();
                InstructionFragment frag = new InstructionFragment();
                sendToSheets(ballView.fScore, Sheets.TestType.LH_LEVEL);
                Bundle bundle = new Bundle();
                bundle.putString(InstructionFragment.MESSAGE_KEY, "Test complete! The test results can be viewed in your gallery.");
                frag.setArguments(bundle);
                frag.show(fragmentManager, null);
            }
            else {
                ballView.saveRightHandFscore();
                ((BallView)findViewById(R.id.ballView)).savePathToGallery(false);

                // Save results
                Log.d("Mark","Test is done, and the score was "+ballView.totalScore);
                Utils.appendResultsToInternalStorage(activity, BALL_TEST_DATA_FILENAME, ballView.totalScore);

                // write the results to the google sheets
                sendToSheets(ballView.fScore, Sheets.TestType.RH_LEVEL);

                ((BallView)findViewById(R.id.ballView)).resetTest();

                FragmentManager fragmentManager = getFragmentManager();
                CompletionFragment frag = new CompletionFragment();
                Bundle bundle = new Bundle();
                bundle.putString(InstructionFragment.MESSAGE_KEY, "");
                frag.setArguments(bundle);
                frag.show(fragmentManager, null);
            }
        }
    };

    private void sendToSheets(double score, Sheets.TestType sheetType) {
        sheet.writeData(sheetType, getString(R.string.patientID), (float)score);
        sheet.writeTrials(sheetType, getString(R.string.patientID), (float)score);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        this.sheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.sheet.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball);
        sheet = new Sheets(this, this, getString(R.string.app_name));
        ballView = (BallView) findViewById(R.id.ballView);
        threeSecondCountdownText = (TextView)findViewById(R.id.start_text);

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
                        ((BallView)findViewById(R.id.ballView)).recieveSensorInput(
                                orientation[2], orientation[1], orientation[0]);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {/*cough*/}
        };

        doneRightTest = false;

        startTest();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prepTimer.cancel();
        testTimer.cancel();
        finish();
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

    public void startTest() {
        FragmentManager fragmentManager = getFragmentManager();
        InstructionFragment frag = new InstructionFragment();

        Bundle bundle = new Bundle();
        bundle.putString(InstructionFragment.MESSAGE_KEY,"Use your left hand to keep the ball in the center of the screen");
        frag.setArguments(bundle);

        frag.show(fragmentManager, null);
    }

    public void startPrepTimer() {
        prepTimer.start();
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
