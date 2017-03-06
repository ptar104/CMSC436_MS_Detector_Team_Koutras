package com.capstone.petros.cmsc436msdetector;

import android.app.FragmentManager;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

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

public class BallActivity extends Activity  implements TimedActivity {
    SensorEventListener sel;
    SensorManager sensorManager;
    Sensor accelerometer, magnetometer;

    BallView ballView;
    TextView threeSecondCountdownText;
    boolean doneRightTest = false;

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
                Bundle bundle = new Bundle();
                bundle.putString(InstructionFragment.MESSAGE_KEY, "Use your right hand to keep the ball in the center of the screen");
                frag.setArguments(bundle);
                frag.show(fragmentManager, null);
            }
            else {
                ballView.saveRightHandFscore();
                ((BallView)findViewById(R.id.ballView)).savePathToGallery(false);

                // Save results
                Log.d("Mark","Test is done, and the score was "+ballView.totalScore);
                appendResultsToInternalStorage(ballView.totalScore);

                ((BallView)findViewById(R.id.ballView)).resetTest();

                FragmentManager fragmentManager = getFragmentManager();
                CompletionFragment frag = new CompletionFragment();
                frag.show(fragmentManager, null);
            }
        }
    };

    public static final String BALL_TEST_DATA_FILENAME = "ball_test_data";
    SortedMap<Long, Double> recordMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball);

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

        // load the previous results
        recordMap = getResultsFromInternalStorage();
        if(recordMap != null) {
            for (Long date : recordMap.keySet()) {
                Log.d("Mark", "At " + date + ", you got " + recordMap.get(date));
            }
        }

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

    private void appendResultsToInternalStorage(double score) {
        Long date = System.currentTimeMillis();

        SortedMap<Long, Double> map = getResultsFromInternalStorage();

        if(map == null) {
            map = new TreeMap<>();
        }

        map.put(date,score);

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(BALL_TEST_DATA_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream writer = new ObjectOutputStream(outputStream);
            writer.writeObject(map);
            writer.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SortedMap getResultsFromInternalStorage() {
        FileInputStream inputStream;
        ObjectInputStream objectInputStream;

        TreeMap<Long, Double> map = null;

        try {
            inputStream = openFileInput(BALL_TEST_DATA_FILENAME);
            objectInputStream = new ObjectInputStream(inputStream);

            map = (TreeMap) objectInputStream.readObject();
            objectInputStream.close();
            inputStream.close();
        } catch(FileNotFoundException e) {
            // The file hasn't been created
        } catch(Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}
