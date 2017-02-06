package com.capstone.petros.cmsc436msdetector;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class TappingActivity extends AppCompatActivity {
    RadioButton leftButton, rightButton;

    boolean testInProgress = false;
    boolean isLeftHand = false;
    boolean firstTest = true;
    int numberOfTaps, previousTextNumberOfTaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tapping);

        leftButton = (RadioButton)findViewById(R.id.Left);
        rightButton = (RadioButton)findViewById(R.id.Right);

        numberOfTaps = 0;
        previousTextNumberOfTaps = 0;

        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.getIndeterminateDrawable().setColorFilter(0xFFDD2400, android.graphics.PorterDuff.Mode.SRC_IN);
        bar.getProgressDrawable().setColorFilter(0xFFDD2400, PorterDuff.Mode.SRC_IN);
    }

    public void processTap(View v) {
        ImageView testBox = (ImageView)findViewById(R.id.test_box);
        if(!testInProgress) {
            testInProgress = true;
            TextView tv = (TextView)findViewById(R.id.start_text);
            tv.setTextSize(100);
            tv.setText("");
            testBox.setBackgroundColor(0xFFFFFFFF);

            // Disable the top bar
            leftButton.setEnabled(false);
            rightButton.setEnabled(false);
            testBox.setEnabled(false);

            // If it is not the first test, animate the "time" bar refilling.
            if(!firstTest) {
                new CountDownTimer(1501, 10) {
                    public void onTick(long millisUntilFinished) {
                        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                        float scale = (bar.getMax() * 1.0f) / 1500.0f; //Scale of total progress to refill rate.
                        bar.setProgress((int)(scale * (1500 - millisUntilFinished)));
                    }

                    public void onFinish() {
                        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                        bar.setProgress(bar.getMax());
                    }
                }.start();
            }

            firstTest = false;

            // I set 3100, 1000 because if the time remaining is less than the interval,
            // it explicitly does not call onTick() (skip last call) and just delays until complete.
            // If using 3000, 1000, it would show 2 (1 second), 1 (2 seconds)
            new CountDownTimer(3100, 1000) {
                TextView tv = (TextView)findViewById(R.id.start_text);
                public void onTick(long millisUntilFinished) {
                    tv.setText(Long.toString(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    tv.setText("Tap!");

                    new CountDownTimer(1000,1000) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            ImageView testBox = (ImageView)findViewById(R.id.test_box);
                            testBox.setEnabled(true);
                            tv.setText("");
                            // Start the timer
                            new CountDownTimer(10000, 10) {
                                public void onTick(long millisUntilFinished) {
                                    // Update the progress bar

                                    ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                    bar.setProgress((int)millisUntilFinished);
                                }

                                public void onFinish() {

                                    testInProgress = false;

                                    previousTextNumberOfTaps = numberOfTaps;
                                    numberOfTaps = 0;

                                    // Set screen to red and show Test Over
                                    ImageView testBox = (ImageView)findViewById(R.id.test_box);
                                    testBox.setBackgroundColor(0xFFDD2400);
                                    testBox.setClickable(false);

                                    tv.setText("Test\nOver");

                                    // Set Progress bar to 0:
                                    ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                                    bar.setProgress(0);

                                    // Show Results
                                    new CountDownTimer(2000, 1000){
                                        public void onTick(long millisUntilFinished) {
                                        }
                                        public void onFinish(){
                                            leftButton.setEnabled(true);
                                            rightButton.setEnabled(true);
                                            tv.setTextSize(30);
                                            tv.setText("You tapped " + previousTextNumberOfTaps + " times.\n" +
                                                    "Tap again to restart.\n\n" +
                                                    "Switch hands with\n" +
                                                    "the buttons above.");
                                            ImageView testBox = (ImageView)findViewById(R.id.test_box);
                                            testBox.setBackgroundColor(0xFFDDDDDD);
                                            testBox.setClickable(true);
                                        }
                                    }.start();
                                }
                            }.start();
                        }
                    }.start();
                }
            }.start();
        }
        else {
            // Register the tap and show green
            numberOfTaps++;

            testBox.setBackgroundColor(0xFF4b7a4a);
            new CountDownTimer(50, 100) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    ImageView testBox = (ImageView)findViewById(R.id.test_box);
                    if(testInProgress)
                        testBox.setBackgroundColor(0xFFFFFFFF);
                }

            }.start();
        }
    }

    public void onHandChanged(View v) {
        RadioButton rb = (RadioButton) v;

        isLeftHand = v.getId() == R.id.Left;
    }
}
