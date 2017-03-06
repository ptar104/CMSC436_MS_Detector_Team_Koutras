package com.capstone.petros.cmsc436msdetector;

import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.widget.TextView;

import java.text.DecimalFormat;

public class TappingActivity extends AppCompatActivity {

    boolean testInProgress = false;
    boolean firstTest = true;
    int numberOfTaps, previousTextNumberOfTaps, tryNumber = 1;
    float rightSum = 0, leftSum = 0;
    TextView tv, tutorialView;
    CountDownTimer countDown, clearText, tenSeconds, finishedText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tapping);
        tv = (TextView) findViewById(R.id.start_text);

        numberOfTaps = 0;
        previousTextNumberOfTaps = 0;

        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
        bar.getIndeterminateDrawable().setColorFilter(0xFFDD2400, android.graphics.PorterDuff.Mode.SRC_IN);
        bar.getProgressDrawable().setColorFilter(0xFFDD2400, PorterDuff.Mode.SRC_IN);

        // I set 3100, 1000 because if the time remaining is less than the interval,
        // it explicitly does not call onTick() (skip last call) and just delays until complete.
        // If using 3000, 1000, it would show 2 (1 second), 1 (2 seconds)
        countDown = new CountDownTimer(3100, 1000) {
            TextView tv = (TextView) findViewById(R.id.start_text);

            public void onTick(long millisUntilFinished) {
                tv.setText(Long.toString(millisUntilFinished / 1000));
            }

            public void onFinish() {
                tv.setText("Tap!");
                clearText.start();
            }
        };

        clearText = new CountDownTimer(1000,1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                ImageView testBox = (ImageView) findViewById(R.id.test_box);
                testBox.setEnabled(true);
                tv.setText("");
                // Start the timer
                tenSeconds.start();
            }
        };

        tenSeconds = new CountDownTimer(10000, 10) {
            public void onTick(long millisUntilFinished) {
                // Update the progress bar

                ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                bar.setProgress((int) millisUntilFinished);
            }

            public void onFinish() {
                testInProgress = false;
                previousTextNumberOfTaps = numberOfTaps;
                numberOfTaps = 0;

                // Set screen to red and show Test Over
                ImageView testBox = (ImageView) findViewById(R.id.test_box);
                testBox.setBackgroundColor(0xFFDD2400);
                testBox.setClickable(false);

                tv.setText("Test\nOver");

                // Set Progress bar to 0:
                ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                bar.setProgress(0);

                // Show Results
                finishedText.start();
            }
        };

        finishedText = new CountDownTimer(2000, 3000){
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish(){
                tv.setTextSize(30);
                String output = " You tapped " + previousTextNumberOfTaps + " times!\n";

                if(tryNumber < 4){
                    rightSum += previousTextNumberOfTaps;
                    if(tryNumber == 3){
                        output += " That was trial " + tryNumber + " of 3\n "
                                + "with your right hand."
                                + "\n\n Switch to your left\n hand and tap to restart";
                    } else {
                        output += " Tap again to restart.\n\n"
                                + " That was trial " + tryNumber + " of 3\n "
                                + "with your right hand.";
                    }

                } else if(tryNumber == 6) {
                    DecimalFormat df = new DecimalFormat("#.#");

                    output += " Test complete! \n\n Right average: "+ df.format(rightSum/3) + " taps"
                           + "\n Left average: " + df.format(leftSum/3) +" taps";

                    //save averages using utils or something
                } else {
                    leftSum += previousTextNumberOfTaps;
                    output += " Tap again to restart.\n\n"
                            + " That was trial " + (6-tryNumber) + " of 3\n "
                            + "with your left hand.";
                }
                tryNumber++;
                tv.setText(output);
                ImageView testBox = (ImageView)findViewById(R.id.test_box);
                testBox.setBackgroundColor(0xFFDDDDDD);
                testBox.setClickable(true);
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        countDown.cancel();
        clearText.cancel();
        tenSeconds.cancel();
        finishedText.cancel();
    }

    public void showTutorial(View v) {
       tutorialView = (TextView) findViewById(R.id.tutorial_view);

        if (tutorialView.getVisibility() == View.GONE) {
            tutorialView.setVisibility(View.VISIBLE);
            tutorialView.setText("This is the tapping test. It measures how many taps you can make on the screen in a 10-second period. Tap the screen to begin, then tap as many times as you can. The test will automatically end after 10 seconds.");
        }
        else {
            tutorialView.setVisibility(View.GONE);
        }
    }

    public void processTap(View v) {
        if(tryNumber > 6){
            return;
        }
        ImageView testBox = (ImageView)findViewById(R.id.test_box);
        if(!testInProgress) {
            testInProgress = true;

            tv.setTextSize(100);
            tv.setText("");
            testBox.setBackgroundColor(0xFFFFFFFF);

            // Disable the top bar
            testBox.setEnabled(false);

            // If it is not the first test, animate the "time" bar refilling.
            if (!firstTest) {
                if (tryNumber == 4){
                    ((TextView)findViewById(R.id.currentHandText)).setText("Left");
                }
                new CountDownTimer(1501, 10) {
                    public void onTick(long millisUntilFinished) {
                        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                        float scale = (bar.getMax() * 1.0f) / 1500.0f; //Scale of total progress to refill rate.
                        bar.setProgress((int) (scale * (1500 - millisUntilFinished)));
                    }

                    public void onFinish() {
                        ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                        bar.setProgress(bar.getMax());
                    }
                }.start();
            }

            firstTest = false;
            countDown.start();

        } else {
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


}
