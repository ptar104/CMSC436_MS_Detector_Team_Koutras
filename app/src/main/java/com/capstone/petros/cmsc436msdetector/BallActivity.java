package com.capstone.petros.cmsc436msdetector;

import android.app.FragmentManager;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

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
                ((BallView)findViewById(R.id.ballView)).resetTest();
                FragmentManager fragmentManager = getFragmentManager();
                CompletionFragment frag = new CompletionFragment();
                frag.show(fragmentManager, null);
            }
        }
    };

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

}
