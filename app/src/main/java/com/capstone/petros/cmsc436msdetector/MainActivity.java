package com.capstone.petros.cmsc436msdetector;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void startTapTest(View view){
        /* Launch Tapping Activity */
        Intent intent = new Intent(this, TappingActivity.class);
        startActivity(intent);
    }

    public void startSpiralTest(View view){
        Intent intent = new Intent(this, SpiralActivity.class);
        startActivity(intent);
    }

    public void startBallTest(View view) {
        Intent intent = new Intent(this, BallActivity.class);
        startActivity(intent);
    }
}
