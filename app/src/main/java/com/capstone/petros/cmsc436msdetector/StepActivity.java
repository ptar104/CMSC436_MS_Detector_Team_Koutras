package com.capstone.petros.cmsc436msdetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StepActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    int trialNum = 1;

    TextView tutorialView;


    SensorEventListener stepSel, accSel;
    SensorManager sensorManager;
    Sensor usedAccelerometer, regAccelerometer, linAccelerometer, stepDetector;

    int stepCount = 0;
    long prevAccUpdateTime = -1, testStartTime = -1;
    boolean collectData = false;
    double currVelocity = 0, averageVelocity = 0;
    long totalMeasurementTime; // The time over which we measure the average velocity

    // The 'tick' stuff
    static final long TICK_TIME = 500; // Miliseconds between each tick. I'm trying half a second.
    long timeSinceLastTick = 0;
    double accTotal;

    // The collected data
    // The row is the test number. The 1st column is the average m/s, the second is the time taken
    // Ex: the time taken on the third trial = data[2][1]
    // average speed (m/s) on first trial = data[0][0]
    private double[][] data = new double[3][2];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        // Keep the screen on...
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
        linAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(linAccelerometer == null){
            regAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            usedAccelerometer = regAccelerometer;
        } else {
            // Linear has no gravity in it.
            usedAccelerometer = linAccelerometer;
        }
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
       /* if(stepDetector == null){
            // Can replace with accelerometer, but for now, I quit.
            System.out.println("Tsk tsk... no step detector.");
            finish();
        }*/

        // Accelerometer listener
        accSel = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                double zAcc = event.values[2];
                long currAccUpdateTime = System.currentTimeMillis();
                if(collectData && prevAccUpdateTime != -1){
                    long deltaTime = currAccUpdateTime - prevAccUpdateTime;
                    // To try to reduce error buildup, only apply acc change over the
                    // course of a 'tick'.
                    accTotal += zAcc * deltaTime;
                    timeSinceLastTick += deltaTime;

                    if(timeSinceLastTick > TICK_TIME){
                        // Get average acceleration
                        /*
                        double averageAcc = accTotal / timeSinceLastTick;
                        // Apply it to velocity
                        currVelocity += (averageAcc * timeSinceLastTick); // in m/s now
                        */
                        currVelocity += accTotal; // accTotal is, in a way, the integration of the acceleration
                        averageVelocity += (currVelocity * timeSinceLastTick);
                        totalMeasurementTime += timeSinceLastTick;
                        timeSinceLastTick = 0;
                        accTotal = 0;
                    }
                }
                prevAccUpdateTime = currAccUpdateTime;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {/*cough*/}
        };

        stepSel = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                // A step!!
                if(collectData) {
                    stepCount++;
                    if(stepCount >= 25){
                        endTrial();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {/*cough*/}
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(stepSel, stepDetector, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(accSel, usedAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(stepSel);
        sensorManager.unregisterListener(accSel);
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void startTest(View v) {

        // Play the instructions
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.sway_pre_prepare);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Start a timer until you say the first trial
                CountDownTimer timer = new CountDownTimer(8000,1000) {
                    @Override
                    public void onTick(long l) {

                    }
                    @Override
                    public void onFinish() {
                        sayInitialInstructions();
                    }
                };
                timer.start();
            }
        });
        mediaPlayer.start();
    }

    public void sayInitialInstructions() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.step_prep);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                CountDownTimer timer = new CountDownTimer(4000,1000) {
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
                mediaPlayer = MediaPlayer.create(this, R.raw.step_start_trial1);
                break;
            case 2:
                mediaPlayer = MediaPlayer.create(this, R.raw.step_start_trial2);
                break;
            case 3:
                mediaPlayer = MediaPlayer.create(this, R.raw.step_start_trial3);
                break;
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Start collecting the step data
                testStartTime = System.currentTimeMillis();
                currVelocity = 0;
                averageVelocity = 0;
                accTotal = 0;
                timeSinceLastTick = 0;
                totalMeasurementTime = 0;
                stepCount = 0;
                startCollectingData();
            }
        });
        mediaPlayer.start();
    }

    //25 steps

    public void simulateSteps(){
        stepCount = 25;
    }

    /*
     * Call this function when the 25 steps are recorded
     */
    public void endTrial() {
        stopCollectingData();
        long timeTaken = System.currentTimeMillis() - testStartTime;
        data[trialNum-1][0] = Math.abs(averageVelocity/(totalMeasurementTime*1000.0));
        data[trialNum-1][1] = timeTaken;

        // Just finished the trialNum'th trial
        //finishCollectingData(trialNum-1);
        if(trialNum == 3){
            finishAllTests();
        }
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        switch (trialNum) {
            case 1:
                mediaPlayer = MediaPlayer.create(this, R.raw.step_end_trial1);
                break;
            case 2:
                mediaPlayer = MediaPlayer.create(this, R.raw.step_end_trial2);
                break;
            case 3:
                mediaPlayer = MediaPlayer.create(this, R.raw.step_end_trial3);
                break;
        }
        if(trialNum < 3) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    // Start a timer until the next trial starts
                    // Put to 2000 at the end
                    CountDownTimer timer = new CountDownTimer(2000, 1000) {
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

    private void finishAllTests(){
        // TODO (ALEX): Display the UI of the results
        // All the data will be in the 2D "data" array.
        // See that array for what's inside it.
        // You probably don't have to display each trial (unless you want to)
        // The average will prob be fine.
        TextView tv = (TextView)findViewById(R.id.stepDebugTextView);
        System.out.println("Last data: m/s: " +data[2][0] + " time taken: "+data[2][1]);

        tv.setText("Trial 1: m/s: " + data[0][0] + " time taken: "+data[0][1] + "\n" +
                "Trial 2: m/s: " + data[1][0] + " time taken: "+data[1][1] + "\n" +
                "Trial 3: m/s: " + data[2][0] + " time taken: "+data[2][1]);

    }

    // Starts collecting data for the tests
    private void startCollectingData() {
        collectData = true;
    }

    // Stops collecting data for the tests
    private void stopCollectingData() {
        collectData = false;
    }

    public void showTutorial(View v) {
        FrameLayout frame = (FrameLayout)findViewById(R.id.swayFrame);
        tutorialView = (TextView) findViewById(R.id.swayInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.swayShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.swayTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the Step test.\n\n" +
                    "It measures your average indoor walking speed.\n\n" +
                    "You need a headband to perform this test. You also need the audio on your phone to be on.\n\n" +
                    "To take this test, put the phone in your headband behind your head.\n\n" +
                    "When prompted, begin walking forward, turning to avoid walls or obstructions.\n\n" +
                    "After 25 steps, the test will be complete.");
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

