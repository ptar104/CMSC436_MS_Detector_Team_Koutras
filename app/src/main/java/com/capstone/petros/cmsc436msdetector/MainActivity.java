package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        String tempFile = "tempFile";
        try {
            Utils.appendResultsToInternalStorage(this, tempFile, 1);
            Thread.sleep(100);
            Utils.appendResultsToInternalStorage(this, tempFile, 2.1);
            Thread.sleep(100);
            Utils.appendResultsToInternalStorage(this, tempFile, 3.2);
            Thread.sleep(100);
            Utils.appendResultsToInternalStorage(this, tempFile, 4.3);
        }
        catch (Exception e){
            // So?
        }
        GraphFragment gf = (GraphFragment) getFragmentManager().findFragmentById(R.id.graph);
        gf.fillWithData(tempFile,4);
        */
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

    public void startReactionTest(View view) {
        Intent intent = new Intent(this, ReactionActivity.class);
        startActivity(intent);
    }

    public void startReactionGraphDemo(View view){
        Intent intent = new Intent(this, ReactionGraphDemoActivity.class);
        startActivity(intent);
    }

    public void startFlexDemo(View view){
        Intent intent = new Intent(this, FlexActivity.class);
        startActivity(intent);
    }

    public void startSwayTest(View view) {
        Intent intent = new Intent(this, SwayActivity.class);
        startActivity(intent);
    }

    public void startStepTest(View view) {
        Intent intent = new Intent(this, StepActivity.class);
        startActivity(intent);
    }

    public void startWalkingTest(View view) {
        Intent intent = new Intent(this, WalkingActivity.class);
        startActivity(intent);
    }

    public void startSymbolTest(View view) {
        Intent intent = new Intent(this, SymbolActivity.class);
        startActivity(intent);
    }

    public void startVibrationTest(View view) {
        Intent intent = new Intent(this, VibrationActivity.class);
        startActivity(intent);
    }
}
