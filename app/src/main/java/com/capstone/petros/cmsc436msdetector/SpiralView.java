package com.capstone.petros.cmsc436msdetector;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by peterkoutras on 2/12/17.
 */

public class SpiralView extends View {
    private Canvas _offScreenCanvas = null, _reportCanvas = null;
    private Bitmap _offScreenBitmap = null;
    public static Bitmap _reportBitmap = null;
    private Path currPath = new Path();
    private Paint touchPaint = new Paint();

    boolean firstTouchRecorded = false;
    long startTime;
    long endTime;
    public static double testDuration;
    public static String grade;
    public static double score;
    public static String fName;

    int numCycles;
    Point closestPoint;

    double spiralScale;

    CountDownTimer countdown;

    boolean up = false;


    List<ArrayList<Point>> userTrace;

    public SpiralView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpiralView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpiralView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        numCycles = 4;

        closestPoint = new Point(0,0);

        userTrace = new ArrayList<>();
    }

    private void initSpiral() {
        // Make the background white
        Paint background = new Paint();
        background.setStrokeWidth(0);
        background.setAntiAlias(true);
        background.setColor(0xFFFFFFFF);
        _offScreenCanvas.drawRect(0,0,_offScreenCanvas.getWidth(),_offScreenCanvas.getHeight(), background);
        _reportCanvas.drawRect(0,0,_offScreenCanvas.getWidth(),_offScreenCanvas.getHeight()+500, background);

        // Margin for spiral
        int margin = this.getWidth()/16;

        // Draw spiral/touch lines here.
        Paint paint = new Paint();
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        Paint shadowPaint = new Paint();
        shadowPaint.setStrokeWidth((this.getWidth()-2*margin)/12);
        shadowPaint.setStrokeCap(Paint.Cap.ROUND);
        shadowPaint.setAntiAlias(true);
        shadowPaint.setColor(0x22DD0000);
        shadowPaint.setStyle(Paint.Style.STROKE);

        float width = this.getWidth();
        float height = this.getHeight();
        float middleX = width/2.0f;
        float middleY = height/2.0f;

        Path path = new Path();
        path.moveTo(middleX,middleY);

        // Calculate the spiralScale
        spiralScale = (Math.min(middleX,middleY)-margin)/(numCycles*Math.PI*2.0);

        // Draw the spiral
        for(float t = 0; t < 2 * Math.PI * numCycles; t += 0.1) {
            double x = spiralScale*t*Math.cos(t) + middleX;
            double y = spiralScale*t*Math.sin(t) + middleY;
            path.lineTo((float)x,(float)y);
        }

        _offScreenCanvas.drawPath(path,shadowPaint);
        _offScreenCanvas.drawPath(path,paint);
        _reportCanvas.drawPath(path,shadowPaint);
        _reportCanvas.drawPath(path,paint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap == null){
            _offScreenBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            _reportBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight()+500, Bitmap.Config.ARGB_8888);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
            _reportCanvas = new Canvas(_reportBitmap);
            initSpiral();
        }

        canvas.drawBitmap(_offScreenBitmap, 0, 0, new Paint());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        float x = motionEvent.getX(), y = motionEvent.getY();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // recording how long it takes for them to complete the test
                if (firstTouchRecorded == false) {
                    startTime = System.currentTimeMillis();
                    firstTouchRecorded = true;
                }
                else{
                    countdown.cancel();
                }
                currPath.moveTo(x, y);
                up = false;
                // Keep track of the user path
                ArrayList<Point> newPath = new ArrayList<>();
                newPath.add(new Point((int)x,(int)y));
                userTrace.add(newPath);

                //closestPoint = getClosestPointToTouch((int)x,(int)y);

                break;
            case MotionEvent.ACTION_MOVE:
                currPath.lineTo(x,y);

                ArrayList<Point> lastPath = userTrace.get(userTrace.size()-1);
                Point lastPoint = lastPath.get(lastPath.size()-1);

                if(dist(lastPoint.x,lastPoint.y,(int)x,(int)y) > 5) {  // Make sure the point is far enough from the last for meaningful data and to conserve space
                    lastPath.add(new Point((int) x, (int) y));
                }

                //closestPoint = getClosestPointToTouch((int)x,(int)y);
                break;
            case MotionEvent.ACTION_UP:
                up = true;
                endTime = System.currentTimeMillis();
                currPath.lineTo(x,y);

                countdown = new CountDownTimer(5000, 100) {
                    public void onTick(long millisUntilFinished) {
                        if(!up){
                            this.cancel();
                        }
                    }

                    public void onFinish() {
                        if(up){
                            TextView instruction = (TextView) getRootView().findViewById(R.id.Instructions);
                            instruction.setText("Test halted...");

                            AlertDialog builder;
                            builder = new AlertDialog.Builder((SpiralActivity)getContext()).create();
                            builder.setTitle("Test Ended");
                            builder.setMessage("No movement detected in last 5 seconds.");
                            builder.setButton(AlertDialog.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    saveTestToGallery();
                                }
                            });
                            builder.setButton(AlertDialog.BUTTON_NEGATIVE, "Reset", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    resetSpiralTest();
                                }
                            });

                            builder.show();
                        }
                    }
                };
                countdown.start();
                break;
        }

        _offScreenCanvas.drawPath(currPath, touchPaint);
        _reportCanvas.drawPath(currPath, touchPaint);
        if(up){
            currPath = new Path();
        }

        invalidate();

        return true;
    }

    // Computes the letterGrade using average radial slope difference
    public String evaluateTrace() {
        // Examine the slope of the trace at each point
        // Compute the difference of the slope of the userTrace with the expected slope

        double width = this.getWidth();
        double height = this.getHeight();

        int numSamples = 0;
        double sum = 0;
        double userRadialSlope, expectedRadialSlope = spiralScale;

        double subSlope1, subSlope2;

        Point p1, p2, p3;
        double r1, r2, r3;
        double t1, t2, t3;

        for(ArrayList<Point> traceSegment: userTrace) {
            for (int i = 1; i < traceSegment.size() - 1; i++) {
                p1 = traceSegment.get(i - 1);
                p2 = traceSegment.get(i);
                p3 = traceSegment.get(i + 1);

                r1 = Math.sqrt(Math.pow(p1.x - width/2, 2) + Math.pow(p1.y - height/2, 2));
                r2 = Math.sqrt(Math.pow(p2.x - width/2, 2) + Math.pow(p2.y - height/2, 2));
                r3 = Math.sqrt(Math.pow(p3.x - width/2, 2) + Math.pow(p3.y - height/2, 2));

                t1 = getAngleAtPoint(p1);
                t2 = getAngleAtPoint(p2);
                t3 = getAngleAtPoint(p3);

                subSlope1 = (r1 - r2) / (t1 - t2);
                subSlope2 = (r2 - r3) / (t2 - t3);

                userRadialSlope = (subSlope1 + subSlope2) / 2;

                double difference = Math.abs(expectedRadialSlope - userRadialSlope);

                sum += difference;
                numSamples++;
            }
        }
        double average = sum / numSamples;

        Log.d("Mark","Number of points: "+numSamples);
        Log.d("Mark","Average radial slope difference: "+average);
        score = average;
        if(average < 100) {
            return "A";
        }
        else if (average < 120) {
            return "B";
        }
        else if (average < 150) {
            return "C";
        }
        else if (average < 200) {
            return "D";
        }
        else {
            return "F";
        }
    }

    // Converts pixel units to Millimeters. Found on Stack Overflow
    public static float pxToMm(final float px, final Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, dm);
    }

    public double dist(int x, int y, int x2, int y2) {
        return Math.sqrt(Math.pow(x-x2,2)+Math.pow(y-y2,2));
    }

    public double getAngleAtPoint(Point p) {
        double width = this.getWidth();
        double height = this.getHeight();

        // Center the point
        int centeredX = (int)(p.x - width/2.0);
        int centeredY = (int)(p.y - height/2.0);

        // Get the angle
        double angle;
        if(centeredX < 0) {
            angle = Math.atan((double)(centeredY)/centeredX)+Math.PI;
        }
        else if(centeredY > 0) {
            angle = Math.atan((double)(centeredY)/centeredX);
        }
        else {
            angle = Math.atan((double)(centeredY)/centeredX)+2*Math.PI;
        }

        return angle;
    }


    // Returns the closest point on the spiral from the point (px,py)
    // We are not using this metric right now, but are saving it in case we do need it.
    /*
    public Point getClosestPointToTouch(int px, int py) {
        double width = this.getWidth();
        double height = this.getHeight();

        // Get the angle
        double angle = getAngleAtPoint(new Point(px, py));

        Point result = new Point();
        result.set((int)(width/2.0+spiralScale*angle*Math.cos(angle)),(int)(height/2.0+spiralScale*angle*Math.sin(angle)));

        double tempAngle = angle;

        while(dist(result.x,result.y,px,py) >= spiralScale*Math.PI) {
            result.set((int)(width/2.0+spiralScale*tempAngle*Math.cos(tempAngle)),(int)(height/2.0+spiralScale*tempAngle*Math.sin(tempAngle)));

            tempAngle += 2*Math.PI;
        }

        return result;
    }
    */

    // The save feature
    public void saveTestToGallery(){
        countdown.cancel();
        // recording how much time the test took
        testDuration = endTime - startTime;
        grade = evaluateTrace();
        //Set up the report.

        //Dividing line
        Paint dividingLinePaint = new Paint();
        dividingLinePaint.setStrokeWidth(6);
        dividingLinePaint.setColor(0xFF00FF00);
        dividingLinePaint.setStyle(Paint.Style.STROKE);
        Path dividingLinePath = new Path();
        dividingLinePath.moveTo(0,_offScreenCanvas.getHeight());
        dividingLinePath.lineTo(_offScreenCanvas.getWidth(),_offScreenCanvas.getHeight());
        _reportCanvas.drawPath(dividingLinePath,dividingLinePaint);

        //Rectangle
        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(0xFFFFFFFF);
        _reportCanvas.drawRect(0,_offScreenCanvas.getHeight(),_offScreenCanvas.getWidth(),
                _offScreenCanvas.getHeight()+500,rectPaint);

        Paint paintText = new Paint();
        paintText.setTextSize(40);

        // Here is where we would add the stats, such as grade or lifts, to the report bitmap.
        // Just add _offScreenCanvas.getHeight() to every y value.
        _reportCanvas.drawText("Test report:", 10,_offScreenCanvas.getHeight()+100, paintText);
        paintText.setTextSize(30);
        SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        _reportCanvas.drawText("Time of test: "+date.format(new Date()), 20,_offScreenCanvas.getHeight()+200, paintText);
        _reportCanvas.drawText("Duration of test: "+ testDuration + " seconds", 20,_offScreenCanvas.getHeight()+250, paintText);
        _reportCanvas.drawText("Test grade: "+ grade, 20,_offScreenCanvas.getHeight()+300, paintText);

        // Taken from code in Jon Froehlich's CMSC434 class.
        fName = UUID.randomUUID().toString();
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Spiral Test Results");
        if(!folder.exists()){
            if(!folder.mkdirs()){
                System.out.println("Folder creation failed...");
                Toast.makeText(getContext(), "Error saving test results.", Toast.LENGTH_LONG).show();
            }
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Spiral Test Results/"+fName + ".png");
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

        TextView instruction = (TextView) getRootView().findViewById(R.id.Instructions);
        instruction.setText("Test Completed! \n Check gallery for image");
    }

    public void resetSpiralTest(){
        _offScreenBitmap = null;
        firstTouchRecorded = false;
        countdown.cancel();
        userTrace.clear();
        TextView instruction = (TextView) getRootView().findViewById(R.id.Instructions);
        instruction.setText("Test reset. Put finger in center of spiral to begin again.");
        invalidate();
    }

    public void stopTimer() {
        if(countdown != null) {
            countdown.cancel();
            countdown = null;
        }
    }
}
