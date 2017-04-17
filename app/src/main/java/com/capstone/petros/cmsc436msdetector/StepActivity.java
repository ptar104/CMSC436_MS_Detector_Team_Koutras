package com.capstone.petros.cmsc436msdetector;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StepActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    int trialNum = 1;

    int stepCount = 0;
    boolean collectData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void startTest(View v) {
        //UI Change

        //Countdown and play sound

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
                startCollectingData();
            }
        });
        mediaPlayer.start();
    }

    /**
     * Debug function to simulate the completion of 25 steps.
     * DELETE MEEEEEEEEEE
     * @param v
     */
    public void lastRecorded(View v) {
        endTrial();
    }

    /*
     * Call this function when the 25 steps are recorded
     */
    public void endTrial() {
        // Just finished the trialNum'th trial
        //finishCollectingData(trialNum-1);
        if(trialNum == 3){
            //finishAllTests();
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

    // Starts collecting data for the tests
    private void startCollectingData() {
        collectData = true;
    }
}
