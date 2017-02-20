package com.capstone.petros.cmsc436msdetector;

import android.content.Context;
import android.graphics.Color;
import android.app.FragmentManager;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class BallActivity extends Activity {

    SensorEventListener sel;
    SensorManager sensorManager;
    Sensor accelerometer, magnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball);

        FragmentManager fragmentManager = getFragmentManager();
        InstructionFragment frag = new InstructionFragment();
        frag.show(fragmentManager, null);

        sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sel = new SensorEventListener() {
            float[] mGravity;
            float[] mGeomagnetic;
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    mGravity = event.values;
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    mGeomagnetic = event.values;
                if (mGravity != null && mGeomagnetic != null) {
                    float Ro[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(Ro, I, mGravity, mGeomagnetic);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(Ro, orientation);
                        // Azimuth = orientation[0], pitch = [1], roll = [2]
                        ((BallView)findViewById(R.id.ballView)).recieveSensorInput(
                                orientation[2], orientation[1], orientation[0]);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {/*cough*/}
        };

    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(sel, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sel, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop(){
        super.onStop();
        sensorManager.unregisterListener(sel);
    }


    /*public void startTest(View v){
        findViewById(R.id.instructionsText).setVisibility(View.GONE);
        findViewById(R.id.ballView).setBackgroundColor(Color.GREEN);

    }*/
}
