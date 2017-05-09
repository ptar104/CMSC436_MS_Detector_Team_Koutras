package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.capstone.petros.cmsc436msdetector.Sheets.Sheets;

public class VibrationActivity extends Activity implements Sheets.Host {
    private Sheets sheet;
    public static final int LIB_ACCOUNT_NAME_REQUEST_CODE = 1001;
    public static final int LIB_AUTHORIZATION_REQUEST_CODE = 1002;
    public static final int LIB_PERMISSION_REQUEST_CODE = 1003;
    public static final int LIB_PLAY_SERVICES_REQUEST_CODE = 1004;
    public static final int LIB_CONNECTION_REQUEST_CODE = 1005;
    CountDownTimer testTimer1, testTimer2, testTimer3, delay5;
    CountDownTimer vibrationTimer1, vibrationTimer2, vibrationTimer3;
    Vibrator vibrator;
    private int vibrationTime = 1000;

    private CountDownTimer returnVibrationTimer1() {
        return new CountDownTimer((1000+(1000-vibrationTime)/2), 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                //Vibrate for vibrationTime here:
                vibrator.vibrate(vibrationTime);
                vibrationTimer2 = returnVibrationTimer2();
                vibrationTimer2.start();
            }
        };
    }

    private CountDownTimer returnVibrationTimer2() {
        return new CountDownTimer(vibrationTime, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                vibrationTimer3 = returnVibrationTimer3();
                vibrationTimer3.start();
            }
        };
    }

    private CountDownTimer returnVibrationTimer3() {
        return new CountDownTimer(3000-vibrationTime-(1000+((1000-vibrationTime)/2)), 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                vibrationTime = (3*vibrationTime)/4;
                if(vibrationTime > 10) {
                    vibrationTimer1 = returnVibrationTimer1();
                    vibrationTimer1.start();
                }
                else {
                    endTest(null);
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);

        sheet = new Sheets(this, this, getString(R.string.app_name));
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

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

        testTimer2 = new CountDownTimer(1000, 5000) {
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

        delay5 = new CountDownTimer(5000, 6000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                testTimer1.start();
                vibrationTimer1.start();
            }
        };

        vibrationTimer1 = returnVibrationTimer1();

        vibrationTimer2 = returnVibrationTimer2();

        vibrationTimer3 = returnVibrationTimer3();
    }

    public void startTest(View view) {
        //UI Change
        Button startBtn = (Button)findViewById(R.id.startTestBtn);
        Button endBtn = (Button)findViewById(R.id.endTestBtn);
        TextView bottomText = (TextView)findViewById(R.id.vibrationBottomText);
        TextView topText = (TextView)findViewById(R.id.vibrationTopText);
        startBtn.setVisibility(View.GONE);
        bottomText.setVisibility(View.GONE);
        endBtn.setVisibility(View.VISIBLE);

        topText.setText("Place your knuckles on the screen.");

        delay5.start();
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        this.sheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.sheet.onActivityResult(requestCode, resultCode, data);
    }

    public void endTest(View view) {
        findViewById(R.id.activity_vibration).setBackgroundColor(0xFF88AAE0);
        testTimer1.cancel();
        testTimer2.cancel();
        testTimer3.cancel();
        delay5.cancel();
        vibrationTimer1.cancel();
        vibrationTimer2.cancel();
        vibrationTimer3.cancel();

        Button endBtn = (Button) findViewById(R.id.endTestBtn);
        endBtn.setVisibility(View.GONE);

        TextView vibrationTopText = (TextView) findViewById(R.id.vibrationTopText);
        vibrationTopText.setVisibility(View.INVISIBLE);
        TextView resultsText = (TextView) findViewById(R.id.results_text);
        resultsText.setVisibility(View.VISIBLE);
        resultsText.setText("RESULTS: Your threshold is "+vibrationTime+"ms.");
        sendToSheets(Sheets.TestType.VIBRATION, vibrationTime);
        System.out.println(vibrationTime);
        // The score is vibrationTime - The lower, the better.
    }

    private void sendToSheets(Sheets.TestType sheetType, int result) {
        //sheet.writeData(sheetType, getString(R.string.patientID), (float)result);
        sheet.writeTrials(sheetType, getString(R.string.patientID), (float)result);
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
        vibrationTimer1.cancel();
        vibrationTimer2.cancel();
        vibrationTimer3.cancel();
        finish();
    }

    @Override
    public int getRequestCode(Sheets.Action action) {
        switch (action) {
            case REQUEST_ACCOUNT_NAME:
                return LIB_ACCOUNT_NAME_REQUEST_CODE;
            case REQUEST_AUTHORIZATION:
                return LIB_AUTHORIZATION_REQUEST_CODE;
            case REQUEST_PERMISSIONS:
                return LIB_PERMISSION_REQUEST_CODE;
            case REQUEST_PLAY_SERVICES:
                return LIB_PLAY_SERVICES_REQUEST_CODE;
            case REQUEST_CONNECTION_RESOLUTION:
                return LIB_CONNECTION_REQUEST_CODE;
            default:
                return -1;
        }
    }

    @Override
    public void notifyFinished(Exception e) {

    }
}
