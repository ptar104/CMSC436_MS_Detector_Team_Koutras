package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

public class SwayActivity extends Activity {
    MediaPlayer mediaPlayer;
    int trialNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sway);
        trialNum = 1;
    }


    @Override
    protected void onStop(){
        super.onStop();
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void onClick(View v) {
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
        if(trialNum < 3) {  // Only prepare for another trial if you're on the first or second
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    // Start a timer until the next trial starts
                    CountDownTimer timer = new CountDownTimer(6000,1000) {
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
}
