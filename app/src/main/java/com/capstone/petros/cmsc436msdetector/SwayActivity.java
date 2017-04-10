package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.media.MediaPlayer;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SwayActivity extends Activity {
    MediaPlayer mediaPlayer;
    int trialNum;

    TextView tutorialView;

    SensorEventListener orientationSel, accelerationSel;
    SensorManager sensorManager;
    Sensor accelerometer, magnetometer;

    double prevX=2, prevY=2, prevZ=2;
    double prevAccX = Integer.MAX_VALUE, prevAccY = Integer.MAX_VALUE, prevAccZ = Integer.MAX_VALUE;
    int oriCount = 0, accCount = 0; // Count to delay input, to try to avoid sudden spikes.
    double prevOriTime = -1, prevAccTime = -1;
    // Each sub array is as follows:
    // [Angle, change in X, change in Y, change in Z]
    double [][] testData = new double[3][4];
    double angleScore = 0, xScore = 0, yScore = 0, zScore = 0;
    boolean collectData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sway);
        trialNum = 1;
        sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        orientationSel = new SensorEventListener() {
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
                        // Azimuth (z): -180 -> 180 (says 0 to 360, I'm skeptical. Let's see)
                        // Pitch (x): -90 -> 90 (website says -180 to 180...)
                        // Roll (y): -180 -> 180 (website says -90 to 90..)
                        // These are all gotten in RAD, I'm pretty sure...

                        //System.out.println("Azimuth: "+orientation[0]+" Pitch: "+orientation[1]+" Roll: "+orientation[2]);

                        // Ideally, get a unit vector in the direction of the phone's normal...
                        oriCount++;
                        if(oriCount == 10) {
                            double x = Math.cos(orientation[0]) * Math.cos(orientation[1]);
                            double y = Math.sin(orientation[0]) * Math.cos(orientation[1]);
                            double z = Math.sin(orientation[1]);

                            double currTime = System.currentTimeMillis();
                            if (prevX != 2) {
                                // Get the angle between the vectors.
                                // Length of each should be 1.
                                double angle = Math.abs(Math.acos(x * prevX + y * prevY + z * prevZ)); // acos(dot product)
                                double deltaTime = currTime - prevOriTime;
                                if(collectData){
                                    angleScore += angle * deltaTime;
                                }

                            }
                            prevOriTime = currTime;
                            prevX = x;
                            prevY = y;
                            prevZ = z;
                            oriCount = 0;
                        }


                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {/*cough*/}
        };

        accelerationSel = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {

                accCount++;
                if(accCount == 10) {

                    double x = event.values[0];
                    double y = event.values[1];
                    double z = event.values[2];

                    double currTime = System.currentTimeMillis();

                    if (prevAccX != Integer.MAX_VALUE) {

                        double deltaX = Math.abs(prevAccX - x);
                        double deltaY = Math.abs(prevAccY - y);
                        double deltaZ = Math.abs(prevAccZ - z);

                        double deltaTime = currTime - prevOriTime;

                        if(collectData){
                            xScore += deltaX * deltaTime;
                            yScore += deltaY * deltaTime;
                            zScore += deltaZ * deltaTime;
                        }
                    }
                    prevOriTime = currTime;
                    prevAccX = x;
                    prevAccY = y;
                    prevAccZ = z;

                    accCount = 0;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {/*cough*/}
        };

    }


    @Override
    protected void onStop(){
        super.onStop();
        sensorManager.unregisterListener(orientationSel);
        sensorManager.unregisterListener(accelerationSel);
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void finishAllTests(){
        TextView topText = (TextView)findViewById(R.id.swayTopText);
        TextView bottomText = (TextView)findViewById(R.id.swayBottomText);
        //At the end, UI change

        double averageAngle = (testData[0][0] + testData[1][0] + testData[2][0]) / 3.0;
        double averageAcceleration = (testData[0][1] + testData[1][1] + testData[2][1] +
                testData[0][2] + testData[1][2] + testData[2][2] +
                testData[0][3] + testData[1][3] + testData[2][3])/9.0;

        topText.setText("Results:\n  Angle Score: "+averageAngle + "\n  Movement Score: "+averageAcceleration);


        Button saveBtn = (Button)findViewById(R.id.saveButton);
        saveBtn.setVisibility(View.VISIBLE);
        Button cancelBtn = (Button)findViewById(R.id.cancelButton);
        cancelBtn.setVisibility(View.VISIBLE);
    }

    public void startTest(View v) {
        //UI Change
        Button btn = (Button)findViewById(R.id.startTestBtn);
        TextView bottomText = (TextView)findViewById(R.id.swayBottomText);
        TextView topText = (TextView)findViewById(R.id.swayTopText);
        btn.setVisibility(View.GONE);
        bottomText.setVisibility(View.GONE);

        //Countdown and play sound
        topText.setText("Put the phone in your headband, behind your head.");

        // Play the instructions
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.sway_prep);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Start a timer until you say the first trial
                CountDownTimer timer = new CountDownTimer(2000,1000) {
                    @Override
                    public void onTick(long l) {

                    }
                    @Override
                    public void onFinish() {
                        trialNum = 1;
                        startTrial();
                    }
                };
                timer.start();
            }
        });
        mediaPlayer.start();
    }

    public void startTrial() {
        if(trialNum < 0 || trialNum > 3) {  // This should not happen
            return;
        }

        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        switch(trialNum) {
            case 1:
                mediaPlayer = MediaPlayer.create(this, R.raw.sway_start_trial1);
                break;
            case 2:
                mediaPlayer = MediaPlayer.create(this, R.raw.sway_start_trial2);
                break;
            case 3:
                mediaPlayer = MediaPlayer.create(this, R.raw.sway_start_trial3);
                break;
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Start a timer until the trial ends
                startCollectingData();
                CountDownTimer timer = new CountDownTimer(10000,1000) {
                    @Override
                    public void onTick(long l) {

                    }
                    @Override
                    public void onFinish() {
                        endTrial();
                    }
                };
                timer.start();
            }
        });
        mediaPlayer.start();
    }

    public void endTrial() {    // Just finished the trialNum'th trial
        finishCollectingData(trialNum-1);
        if(trialNum == 3){
            finishAllTests();
        }
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        switch (trialNum) {
            case 1:
                mediaPlayer = MediaPlayer.create(this, R.raw.sway_end_trial1);
                break;
            case 2:
                mediaPlayer = MediaPlayer.create(this, R.raw.sway_end_trial2);
                break;
            case 3:
                mediaPlayer = MediaPlayer.create(this, R.raw.sway_end_trial3);
                break;
        }
        if(trialNum < 3) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    // Start a timer until the next trial starts
                    // Put to 6000 at the end
                    CountDownTimer timer = new CountDownTimer(600, 1000) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            trialNum++;
                            startTrial();
                        }
                    };
                    timer.start();
                }
            });
        }
        mediaPlayer.start();
    }

    public void saveBtn(View v) {

    }

    public void cancelBtn(View v) {

    }

    public void showTutorial(View v) {
        FrameLayout frame = (FrameLayout)findViewById(R.id.swayFrame);
        tutorialView = (TextView) findViewById(R.id.swayInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.swayShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.swayTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the sway test.\n\n" +
                    "It measures how steady you can keep your head when your eyes are closed.\n\n" +
                    "You need a headband to perform this test. You also need the audio on your phone to be on.\n\n" +
                    "To take this test, put the phone in your headband behind your head.\n\n" +
                    "Close and open your eyes when prompted, and try to keep your head as still as possible.\n\n" +
                    "This test has three trials. You will be prompted to remove your phone after all three tests complete.");
            shader.setVisibility(View.VISIBLE);
            tutorialButton.setColorFilter(0xFFF6FF00);
        }
        else {
            frame.setVisibility(View.GONE);
            shader.setVisibility(View.GONE);
            tutorialButton.setColorFilter(0xFF000000);
        }
    }

    // Starts collecting data for the tests
    private void startCollectingData(){
        angleScore = 0;
        xScore = 0;
        yScore = 0;
        zScore = 0;
        collectData = true;
    }

    // Finished collecting data for the tests
    private void finishCollectingData(int testNum){
        collectData = false;
        testData[testNum][0] = angleScore;
        testData[testNum][1] = xScore;
        testData[testNum][2] = yScore;
        testData[testNum][3] = zScore;
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(orientationSel, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(orientationSel, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(accelerationSel, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

}
