package com.capstone.petros.cmsc436msdetector;

import android.content.ContentResolver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SpiralActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral);
    }

    public void saveImage(View view){
        ((SpiralView)findViewById(R.id.SpiralView)).saveTestToGallery(getContentResolver());
    }
}
