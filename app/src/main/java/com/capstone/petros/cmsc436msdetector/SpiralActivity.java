package com.capstone.petros.cmsc436msdetector;

import android.content.ContentResolver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SpiralActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral);

        final TextView instruction = (TextView) findViewById(R.id.Instructions);
        SpiralView spiralView = (SpiralView)findViewById(R.id.SpiralView);
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
    }

    public void saveImage(View view){
        ((SpiralView)findViewById(R.id.SpiralView)).saveTestToGallery(getContentResolver());
    }
}
