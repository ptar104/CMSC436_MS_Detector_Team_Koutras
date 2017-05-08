package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class VibrationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);
    }

    public void startTest(View view) {
        //UI Change
        Button btn = (Button)findViewById(R.id.startTestBtn);
        TextView bottomText = (TextView)findViewById(R.id.vibrationBottomText);
        TextView topText = (TextView)findViewById(R.id.vibrationTopText);
        btn.setVisibility(View.GONE);
        bottomText.setVisibility(View.GONE);

        topText.setText("Place your knuckles on the screen.");
    }

    public void endTest(View view) {
    }

    public void showTutorial(View view) {
    }
}
