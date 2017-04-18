package com.capstone.petros.cmsc436msdetector;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.capstone.petros.cmsc436msdetector.Sheets.Sheets;

public class ReactionActivity extends AppCompatActivity implements Sheets.Host {
    private Sheets sheet;
    public static final int LIB_ACCOUNT_NAME_REQUEST_CODE = 1001;
    public static final int LIB_AUTHORIZATION_REQUEST_CODE = 1002;
    public static final int LIB_PERMISSION_REQUEST_CODE = 1003;
    public static final int LIB_PLAY_SERVICES_REQUEST_CODE = 1004;
    public static final int LIB_CONNECTION_REQUEST_CODE = 1005;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction);
        sheet = new Sheets(this, this, getString(R.string.app_name));
    }

    public void showTutorial(View v) {
        FrameLayout frame = (FrameLayout)findViewById(R.id.reactionFrame);
        TextView tutorialView = (TextView) findViewById(R.id.reactionInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.reactionShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.reactionTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the reaction test.\n\n" +
                    "It measures how quickly you can click a moving red dot.\n\n" +
                    "Each time you click the dot, it will disappear and reappear in a new location.\n\n" +
                    "The ball will move 10 times in total. When it reappears, click it as quick as you can.\n\n" +
                    "To begin the test, simply click the red dot in the center of the screen for the first time.");
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
    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        this.sheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.sheet.onActivityResult(requestCode, resultCode, data);
    }

    public void sendToSheetsLeft(View v) {
        sendToSheets(Sheets.TestType.LH_POP);
    }

    public void sendToSheetsRight(View v) {
        sendToSheets(Sheets.TestType.RH_POP);
    }

    private void sendToSheets(Sheets.TestType sheetType) {
        ReactionView reactionView = (ReactionView) findViewById(R.id.reactionView);
        sheet.writeData(sheetType, getString(R.string.patientID), (float)reactionView.average);
        sheet.writeTrials(sheetType, getString(R.string.patientID), (float)reactionView.average);
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
