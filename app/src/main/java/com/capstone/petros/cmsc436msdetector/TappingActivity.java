package com.capstone.petros.cmsc436msdetector;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.example.sheets436.Sheets;

import android.widget.RelativeLayout;
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
                sendToSheets();

                String output = "You tapped " + previousTextNumberOfTaps + " times!\n";

                if(tryNumber < 4){
                    rightSum += previousTextNumberOfTaps;
                    if(tryNumber == 3){
                        output += "That was trial " + tryNumber + " of 3 with your right hand.\n\n" +
                                "Switch to your left hand and tap to restart.";
                    } else {
                        output += "Tap again to restart.\n\n"
                                + "That was trial " + tryNumber + " of 3 with your right hand.";
                    }

                } else if(tryNumber == 6) {
                    DecimalFormat df = new DecimalFormat("#.#");

                    output += "\nAll tests complete!\n\n" +
                            "Right average: "+ df.format(rightSum/3) + " taps.\n" +
                            "Left average: " + df.format(leftSum/3) +" taps.";

                    //save averages using utils or something
                } else {
                    leftSum += previousTextNumberOfTaps;
                    output += "Tap again to restart.\n\n"
                            + "That was trial " + (tryNumber-3) + " of 3 with your left hand.";
                }
                tryNumber++;
                tv.setText(output);
                ImageView testBox = (ImageView)findViewById(R.id.test_box);
                testBox.setBackgroundColor(0xFFDDDDDD);
                testBox.setClickable(true);

                // Show the "?" again.
                findViewById(R.id.tutorial_button).setVisibility(View.VISIBLE);
            }
        };
    }

    private void sendToSheets() {
        Intent sheets = new Intent(this, SheetsLocal.class);
        String myUserId = "t10p01";
        float avg_tapping_time = 72.4f;

        sheets.putExtra(Sheets.EXTRA_TYPE, Sheets.UpdateType.LH_TAP.ordinal());
        sheets.putExtra(Sheets.EXTRA_USER, myUserId);
        sheets.putExtra(Sheets.EXTRA_VALUE, avg_tapping_time);

        startActivity(sheets);
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
        FrameLayout frame = (FrameLayout)findViewById(R.id.tappingFrame);
        tutorialView = (TextView) findViewById(R.id.tappingInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.tappingShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.tutorial_button);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the tapping test.\n\n" +
                    "It measures how many taps you can make on the screen in a 10-second period.\n\n" +
                    "This test will require you to perform three trials with each hand. " +
                    "First your right hand, then your left hand.\n\n" +
                    "Please only tap with one finger during the tests, preferably your index finger.\n\n" +
                    "Tap the screen to begin, then tap as many times as you can. " +
                    "The on-screen prompts will guide you throughout the test.");
            shader.setVisibility(View.VISIBLE);
            tutorialButton.setColorFilter(0xFFF6FF00);
        }
        else {
            frame.setVisibility(View.GONE);
            shader.setVisibility(View.GONE);
            tutorialButton.setColorFilter(0xFF000000);
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

            // Hide the "?" button
            findViewById(R.id.tutorial_button).setVisibility(View.GONE);

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
