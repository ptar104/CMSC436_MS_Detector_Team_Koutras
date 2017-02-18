package com.capstone.petros.cmsc436msdetector;

import android.content.Context;
import android.graphics.Color;
import android.app.FragmentManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class BallActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball);

        FragmentManager fragmentManager = getFragmentManager();
        InstructionFragment frag = new InstructionFragment();
        frag.show(fragmentManager, null);
        

    }


    /*public void startTest(View v){
        findViewById(R.id.instructionsText).setVisibility(View.GONE);
        findViewById(R.id.ballView).setBackgroundColor(Color.GREEN);

    }*/
}
