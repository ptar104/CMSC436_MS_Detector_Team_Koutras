package com.capstone.petros.cmsc436msdetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;

import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by MarkCreamer on 2/16/17.
 */

public class BallView extends View {
    Display disp = ((WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    Paint paint3 = new Paint();
    Paint ball = new Paint();
    private int x = (disp.getWidth() / 2);
    private int y = (disp.getHeight() / 2);

    long samples = 0;
    float currRoll = 0, currPitch = 0;
    float prevRoll = 0, prevPitch = 0;
    double totalScore = 0;
    boolean testActive = false, first = true;

    static final double MAGTHRESHOLD = 45.0, ANGLETHRESHOLD = 72.0; // From experience
    double PITODEG = (360 / (2 * Math.PI));

    public BallView(Context context) {
        super(context);
    }

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public BallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void toggleTestActive(boolean active){
        testActive = active;
    }

    public void recieveSensorInput(float roll, float pitch, float azimuth){
        if(testActive) {
            // Roll would change x...
            // Pitch would change y...
            currRoll = roll;
            currPitch = pitch;

            if (currRoll > Math.PI / 2.0) {
                currRoll = (float) (Math.PI / 2.0);
            }
            if (currRoll < -1 * (Math.PI / 2.0)) {
                currRoll = -1 * (float) (Math.PI / 2.0);
            }
            if (currPitch > (Math.PI / 2.0)) {
                currPitch = (float) (Math.PI / 2.0);
            }
            if (currPitch < -1 * (Math.PI / 2.0)) {
                currPitch = -1 * (float) (Math.PI / 2.0);
            }

            // If roll/pitch escape -90 to 90, phone waaaay too tilted.
            invalidate();
        }
    }

    // Call when resetting the test.
    public void resetTest(){
        currRoll = 0;
        currPitch = 0;
        prevRoll = 0;
        prevPitch = 0;
        first = false;
        x = (disp.getWidth() / 2);
        y = (disp.getHeight() / 2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint1.setColor(Color.WHITE);
        paint1.setTextSize(30);
        paint2.setColor(Color.BLACK);
        paint3.setColor(Color.WHITE);
        ball.setColor(Color.RED);

        double xDeg = currRoll*PITODEG;
        double yDeg = -1*currPitch*PITODEG;

        // Parsing out slight degrees.
        if(xDeg < 1 && xDeg > -1){
            xDeg = 0;
        }
        if(yDeg < 1 && yDeg > -1){
            yDeg = 0;
        }

        x += xDeg;
        y += yDeg;

        if(x < 0){
            x = 0;
        }
        if(x > getWidth()){
            x = getWidth();
        }

        if(y < (getHeight() / 2) - (getWidth() / 2)){
            y = (getHeight() / 2) - (getWidth() / 2);
        }
        if(y > (getHeight() / 2) + (getWidth() / 2)){
            y = (getHeight() / 2) + (getWidth() / 2);
        }

        double midX = getWidth() / 2, midY = getHeight() / 2;
        double distance = Math.sqrt((1.0*(x-midX))*(1.0*(x-midX)) + (1.0*(y-midY))*(1.0*(y-midY)));
        if(testActive) {
            if (distance <= getWidth() / 6) {
                paint3.setColor(Color.GREEN);
            }
            else if (distance <= getWidth() / 3) {
                paint2.setColor(Color.GREEN);
            }
            else {
                paint1.setColor(Color.GREEN);
            }
        }

        if(distance >= getWidth()/2){
            // Outside last circle.
            double angle = Math.atan2(-1.0 * (y - (getHeight()/2.0)), x - (getWidth() / 2.0));
            x = (int)(midX + (getWidth()/2.0) * Math.cos(angle));
            y = (int)(midY - (getWidth()/2.0) * Math.sin(angle));
        }

        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2, paint1);
        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/3, paint2);
        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/6, paint3);
        canvas.drawCircle(x, y, getWidth()/11, ball);
        calculateJitteryness();
    }

    public void calculateJitteryness() {
        // Idea: to get jitteryness, get an average of angle and magnitue of orientation changes
        // over the points. Take the f-score (harmonic mean),
        // so BOTH have to be high for jitter score to be high.
        // Magnitude is between 0 and 90, based on how much the phone is tilted?
        // Angle is between 0 and 180, based on the angle of the phone?
        // May want the f-score much more based on magnitude than angle.
        // As at low magnitude, high flucuation of angle...
        if(!first) {
            samples++;
            double currAngle = Math.atan2(currPitch, currRoll);
            // My phone's roll is, like, backwards, but that should be ok?
            // It's about relative angles, anyway.
            // Angle goes from 0 to pi, jumps to negative pi, back to 0...
            // Want to smooth it so it can be between 0 and 360
            if (currAngle < 0) {
                currAngle += 2 * Math.PI; // Normalize
            }
            currAngle *= PITODEG; // Convert to 360.
            double currMagnitude = Math.sqrt(currRoll * PITODEG * currRoll * PITODEG +
                    currPitch * PITODEG * currPitch * PITODEG);

            double prevAngle = Math.atan2(prevPitch, prevRoll);
            if (prevAngle < 0) {
                prevAngle += 2 * Math.PI; // Normalize
            }
            prevAngle *= PITODEG; // Convert to 360.
            double prevMagnitude = Math.sqrt(prevRoll * PITODEG * prevRoll * PITODEG +
                    prevPitch * PITODEG * prevPitch * PITODEG); //roll * pitch

            double angleBetween = Math.min(Math.min(Math.abs(currAngle - prevAngle),
                    Math.abs((currAngle + 360) - prevAngle)),
                    Math.abs((currAngle - 360) - prevAngle));
            angleBetween /= ANGLETHRESHOLD;
            double averageMag = Math.abs(currMagnitude - prevMagnitude);
            averageMag /= MAGTHRESHOLD;
            double beta = 1;
            double fscore = (1 + beta * beta) * ((angleBetween * averageMag) / ((beta * beta * averageMag) + angleBetween));
            if(((beta * beta * averageMag) + angleBetween) == 0){
                fscore = 0;
            }
            totalScore += fscore;
        }

        prevRoll = currRoll;
        prevPitch = currPitch;
        first = false;
    }

    public double getAverageFscore(){
        return totalScore / (samples * 1.0);
    }
}
