package com.capstone.petros.cmsc436msdetector;

import android.content.ContentResolver;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.petros.cmsc436msdetector.Sheets.Sheets;

public class SpiralActivity extends AppCompatActivity implements Sheets.Host {

    private SpiralView spiralView;
    private Sheets sheet;
    public static final int LIB_ACCOUNT_NAME_REQUEST_CODE = 1001;
    public static final int LIB_AUTHORIZATION_REQUEST_CODE = 1002;
    public static final int LIB_PERMISSION_REQUEST_CODE = 1003;
    public static final int LIB_PLAY_SERVICES_REQUEST_CODE = 1004;
    public static final int LIB_CONNECTION_REQUEST_CODE = 1005;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral);
        sheet = new Sheets(this, this, getString(R.string.app_name));
        final TextView instruction = (TextView) findViewById(R.id.Instructions);
        spiralView = (SpiralView)findViewById(R.id.SpiralView);
        spiralView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_MOVE:
                        instruction.setText("Follow the path to trace the Spiral");
                        break;
                    case MotionEvent.ACTION_UP:
                        instruction.setText("Put your finger back");
                        break;
                }
                return false;
            }
        });

        spiralView = (SpiralView)findViewById(R.id.SpiralView);
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

    private void sendToSheets(Sheets.TestType sheetType, Sheets.TestType timeSheet, String hand) {
        //sheet.writeData(sheetType, getString(R.string.patientID), (float)SpiralView.score);
        sheet.writeTrials(sheetType, getString(R.string.patientID), (float)SpiralView.score);
        sheet.writeTrials(timeSheet, getString(R.string.patientID), (float)SpiralView.testDuration);
        sheet.uploadToDrive("0B_ZudzTKOJeEUUY2bnhjd3JNbVk", SpiralView.fName + "-" + hand + ".png", SpiralView._reportBitmap);
    }

    public void saveImageLeft(View view){
        ((SpiralView)findViewById(R.id.SpiralView)).saveTestToGallery();
        sendToSheets(Sheets.TestType.LH_SPIRAL, Sheets.TestType.LH_SPIRAL_TIME, "Left");
    }

    public void saveImageRight(View view){
        ((SpiralView)findViewById(R.id.SpiralView)).saveTestToGallery();
        sendToSheets(Sheets.TestType.RH_SPIRAL, Sheets.TestType.RH_SPIRAL_TIME, "Right");
    }

    public void resetTest(View view){
        ((SpiralView)findViewById(R.id.SpiralView)).resetSpiralTest();
    }

    public void showTutorial(View v) {
        ScrollView frame = (ScrollView)findViewById(R.id.spiralFrame);
        TextView tutorialView = (TextView) findViewById(R.id.spiralInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.spiralShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.spiralTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the spiral test.\n\n" +
                    "It measures how closely you can trace a spiral.\n\n" +
                    "To do the spiral test, simply place your finger in the middle of the spiral, " +
                    "and trace until you reach the end of the spiral.\n\n" +
                    "You are free to lift your finger and put it back down on the screen while " +
                    "drawing the spiral, but doing so for more than 10 seconds will end the test.\n\n" +
                    "The \"SAVE TEST\" button will finish the test, and the \"RESET TEST\" button " +
                    "will restart the test from the beginning.\n\n" +
                    "Once the test finishes, a report will be saved to your gallery.");
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
    protected void onStop() {
        super.onStop();
        if(spiralView != null) {
            spiralView.stopTimer();
        }
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
