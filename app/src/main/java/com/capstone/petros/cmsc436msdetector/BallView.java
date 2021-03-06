package com.capstone.petros.cmsc436msdetector;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.AttributeSet;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by MarkCreamer on 2/16/17.
 */

public class BallView extends View {
    private Canvas _reportCanvas = null;
    public static Bitmap _reportBitmap = null;
    private Path currPath = new Path();
    private Paint touchPaint = new Paint();
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    Paint paint3 = new Paint();
    Paint ball = new Paint();
    private int x = -1;
    private int y = -1;
    double fScore;
    String grade = "N/A";
    public static String fName;

    // these variables represent the amount of time that the user spent in each particular circle for the duration of the test
    // CircleOne is the innermost, CircleTwo is the middle circle, CircleThree is the outermost
    long timeInCircleOne = 0;
    long timeInCircleTwo = 0;
    long timeInCircleThree = 0;
    private long prevTime = 0;
    private long currTime = 0;

    long samples = 0;
    float currRoll = 0, currPitch = 0;
    float prevRoll = 0, prevPitch = 0;
    double totalScore = 0;
    double leftHandScore = -1, rightHandScore = -1;
    boolean testActive = false, first = true;

    static final double MAGTHRESHOLD = 45.0, ANGLETHRESHOLD = 72.0; // From experience
    static final double PITODEG = (360 / (2 * Math.PI));
    static final double MILISACROSSSCREEN = 100; // Takes 1/3 of a second to get across the screen
                                                    // at full tilt.

    public BallView(Context context) {
        super(context);
        init(null, 0);
    }

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        touchPaint.setStrokeWidth(5);
        touchPaint.setAntiAlias(true);
        touchPaint.setStyle(Paint.Style.STROKE);
        touchPaint.setColor(Color.RED);
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
    public void resetTest() {
        currRoll = 0;
        currPitch = 0;
        prevRoll = 0;
        prevPitch = 0;
        first = false;
        x = -1;
        y = -1;
        samples = 0;
        totalScore = 0;
        currPath = new Path();
        _reportCanvas = null;
        prevTime = 0;
        currTime = 0;
        timeInCircleOne = 0;
        timeInCircleTwo = 0;
        timeInCircleThree = 0;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint1.setColor(Color.WHITE);
        paint2.setColor(Color.BLACK);
        paint3.setColor(Color.WHITE);
        ball.setColor(Color.RED);
        
        double xDeg = currRoll*PITODEG;
        double yDeg = -1*currPitch*PITODEG;

        if (x == -1 && y == -1) {
            x = getWidth()/2;
            y = getHeight()/2;
            currPath.moveTo(x, y);
        }

        // Parsing out slight degrees.
        if(xDeg < 1 && xDeg > -1){
            xDeg = 0;
        }
        if(yDeg < 1 && yDeg > -1){
            yDeg = 0;
        }

        //Adding slope to x/y

        //Units: Screen distance / miliseconds
        double ellapsedMillis = 10; // Just something small for the first nudge.
        if(prevTime != 0)
            ellapsedMillis = currTime - prevTime;
        double unit = getWidth() / MILISACROSSSCREEN;

        double scale = ellapsedMillis * unit;


        x += (xDeg/90.0) * scale;
        y += (yDeg/90.0) * scale;

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
            prevTime = currTime;
            currTime = System.currentTimeMillis();

            if (distance <= getWidth() / 8) {
                paint3.setColor(Color.GREEN);

                if (prevTime != 0)
                    timeInCircleOne += currTime - prevTime;
            }
            else if (distance <= getWidth() / 3) {
                paint2.setColor(Color.GREEN);

                if (prevTime != 0)
                    timeInCircleTwo += currTime - prevTime;
            }
            else {
                paint1.setColor(Color.GREEN);

                if (prevTime != 0)
                    timeInCircleThree += currTime - prevTime;
            }
        }

        if(distance >= getWidth()/2){
            // Outside last circle.
            double angle = Math.atan2(-1.0 * (y - (getHeight()/2.0)), x - (getWidth() / 2.0));
            x = (int)(midX + (getWidth()/2.0) * Math.cos(angle));
            y = (int)(midY - (getWidth()/2.0) * Math.sin(angle));
        }

        if (_reportCanvas == null) {
            Paint background = new Paint();
            background.setStrokeWidth(0);
            background.setAntiAlias(true);
            background.setColor(0xFF88AAE0);
            _reportBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight()+500, Bitmap.Config.ARGB_8888);
            _reportCanvas = new Canvas(_reportBitmap);
            _reportCanvas.drawRect(0,0,getWidth(),getHeight()+500, background);
            Paint reportPaint1 = new Paint();
            Paint reportPaint2 = new Paint();
            Paint reportPaint3 = new Paint();
            reportPaint1.setColor(Color.WHITE);
            reportPaint2.setColor(Color.BLACK);
            reportPaint3.setColor(Color.WHITE);
            _reportCanvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2, reportPaint1);
            _reportCanvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/3, reportPaint2);
            _reportCanvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/8, reportPaint3);
        }

        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2, paint1);
        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/3, paint2);
        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/8, paint3);

        canvas.drawCircle(x, y, getWidth()/11, ball);
        currPath.lineTo(x,y);
        _reportCanvas.drawPath(currPath, touchPaint);

        calculateJitteryness();
    }

    public void calculateJitteryness() {
        // Idea: to get jitteryness, get an average of angle and magnitude of orientation changes
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

    public void saveLeftHandFscore(){
        leftHandScore = getAverageFscore();
    }

    public void saveRightHandFscore(){
        rightHandScore = getAverageFscore();
    }

    public void savePathToGallery(boolean leftHand) {
        //Dividing line
        Paint dividingLinePaint = new Paint();
        dividingLinePaint.setStrokeWidth(6);
        dividingLinePaint.setColor(0xFF00FF00);
        dividingLinePaint.setStyle(Paint.Style.STROKE);
        Path dividingLinePath = new Path();
        dividingLinePath.moveTo(0,_reportCanvas.getHeight()*2/3);
        dividingLinePath.lineTo(_reportCanvas.getWidth(),_reportCanvas.getHeight()*2/3);
        _reportCanvas.drawPath(dividingLinePath,dividingLinePaint);

        Paint paintText = new Paint();
        paintText.setTextSize(30);
        if(leftHand)
            _reportCanvas.drawText("Test Report (left hand):", 10,_reportCanvas.getHeight()*2/3+80, paintText);
        else _reportCanvas.drawText("Test Report (right hand):", 10,_reportCanvas.getHeight()*2/3+80, paintText);
        SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        _reportCanvas.drawText("Time of test: "+date.format(new Date()), 20,_reportCanvas.getHeight()*2/3+180, paintText);
        _reportCanvas.drawText("Time spent: ", 20,_reportCanvas.getHeight()*2/3+230, paintText);
        DecimalFormat df = new DecimalFormat("#.000");
        _reportCanvas.drawText("    Innermost circle: "+ df.format(timeInCircleOne/1000.0) + " seconds", 20,_reportCanvas.getHeight()*2/3+280, paintText);
        _reportCanvas.drawText("    Middle circle: "+ df.format(timeInCircleTwo/1000.0) + " seconds", 20,_reportCanvas.getHeight()*2/3+330, paintText);
        _reportCanvas.drawText("    Outside circle: "+ df.format(timeInCircleThree/1000.0) + " seconds", 20,_reportCanvas.getHeight()*2/3+380, paintText);

        fScore = leftHand ? leftHandScore : rightHandScore;
        if(fScore <= 0.025){
            grade = "A";
        }
        else if(fScore <= 0.05){
            grade = "B";
        }
        else if(fScore <= 0.1){
            grade = "C";
        }
        else if(fScore <= 0.25){
            grade = "D";
        }
        else grade = "E";

        _reportCanvas.drawText("Jitteryness Score: "+ df.format(fScore) + " (Grade: " + grade + ")", 20,_reportCanvas.getHeight()*2/3+460, paintText);

        fName = UUID.randomUUID().toString();

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Ball Test Results");
        if(!folder.exists()){
            if(!folder.mkdirs()){
                System.out.println("Folder creation failed...");
                Toast.makeText(getContext(), "Error saving test results.", Toast.LENGTH_LONG).show();
            }
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Ball Test Results/"+fName + ".png");
        try {
            _reportBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));

            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());

            getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Toast.makeText(getContext(), "Saved Test results to gallery.", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error saving test results.", Toast.LENGTH_LONG).show();
        }
    }
}
