package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class VibrationActivity extends Activity {

    CountDownTimer testTimer1, testTimer2, testTimer3, delay5;
    CountDownTimer vibrationTimer1, vibrationTimer2, vibrationTimer3;
    private int vibrationTime = 1000;
    private boolean blueBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);

        // The first one is the length, the second is the tick.
        testTimer1 = new CountDownTimer(1000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                findViewById(R.id.activity_vibration).setBackgroundColor(0xFFFF3333);
                testTimer2.start();
            }
        };

        testTimer2 = new CountDownTimer(1000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                findViewById(R.id.activity_vibration).setBackgroundColor(0xFF88AAE0);
                testTimer3.start();
            }
        };

        testTimer3 = new CountDownTimer(1000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                testTimer1.start();
            }
        };

        vibrationTimer1 = new CountDownTimer((1000+(1000-vibrationTime)/2), 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                //Vibrate for vibrationTime here:
                
                vibrationTimer2.start();
            }
        };

        vibrationTimer2 = new CountDownTimer(vibrationTime, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                vibrationTimer3.start();
            }
        };

        vibrationTimer3 = new CountDownTimer(3000-vibrationTime-(1000+((1000-vibrationTime)/2)), 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                vibrationTime = vibrationTime/2;
                vibrationTimer1.start();
            }
        };
    }

    public void startTest(View view) {
        //UI Change
        Button btn = (Button)findViewById(R.id.startTestBtn);
        TextView bottomText = (TextView)findViewById(R.id.vibrationBottomText);
        TextView topText = (TextView)findViewById(R.id.vibrationTopText);
        btn.setVisibility(View.GONE);
        bottomText.setVisibility(View.GONE);

        topText.setText("Place your knuckles on the screen.");

        delay5 = new CountDownTimer(5000, 6000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                testTimer1.start();
                vibrationTimer1.start();
            }
        }.start();
    }

    public void endTest(View view) {
    }

    public void showTutorial(View view) {
        ScrollView frame = (ScrollView)findViewById(R.id.vibrationFrame);
        TextView tutorialView = (TextView) findViewById(R.id.vibrationInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.vibrationShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.vibrationTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the vibration test.\n\n" +
                    "It measures if you can feel phone vibrations in shorts bursts.\n\n" +
                    "To start the test, hit the \"START TEST\" button. Then put your index and middle knuckles on the screen.\n\n" +
                    "The phone will vibrate at increasingly shorter bursts.\n\n" +
                    "The phone will only vibrate when the screen turns red. If the screen turns red, and then blue again " +
                    "without you feeling a vibration, indicate this by clicking the \"END TEST\" button.");
            shader.setVisibility(View.VISIBLE);
            tutorialButton.setColorFilter(0xFFF6FF00);
        }
        else {
            frame.setVisibility(View.GONE);
            shader.setVisibility(View.GONE);
            tutorialButton.setColorFilter(0xFF000000);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        testTimer1.cancel();
        testTimer2.cancel();
        testTimer3.cancel();
        delay5.cancel();
        finish();
    }
}
