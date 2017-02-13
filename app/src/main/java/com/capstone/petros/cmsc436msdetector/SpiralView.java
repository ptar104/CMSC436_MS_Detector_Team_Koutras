package com.capstone.petros.cmsc436msdetector;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * Created by peterkoutras on 2/12/17.
 */

public class SpiralView extends View {

    private Canvas _offScreenCanvas = null, _reportCanvas = null;
    private Bitmap _offScreenBitmap = null, _reportBitmap = null;
    private Path currPath = new Path();
    private Paint touchPaint = new Paint();

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
        touchPaint.setColor(0xFFFF0000); // Red.

    }

    private void initSpiral(){

        // Make the background white
        Paint background = new Paint();
        background.setStrokeWidth(0);
        background.setAntiAlias(true);
        background.setColor(0xFFFFFFFF);
        _offScreenCanvas.drawRect(0,0,_offScreenCanvas.getWidth(),_offScreenCanvas.getHeight(), background);
        _reportCanvas.drawRect(0,0,_offScreenCanvas.getWidth(),_offScreenCanvas.getHeight()+500, background);

        // Draw spiral/touch lines here.
        Paint paint = new Paint();
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        Paint shadowPaint = new Paint();
        shadowPaint.setStrokeWidth(90);
        shadowPaint.setAntiAlias(true);
        shadowPaint.setColor(0x22DD0000);
        shadowPaint.setStyle(Paint.Style.STROKE);

        Path path = new Path();
        float middleX = this.getWidth()/2.0f;
        float middleY = this.getHeight()/2.0f;
        path.moveTo(middleX,middleY);


        for(int i = 0; i < 720; i++){
            double angle = 0.1 * i;
            double x=20*(0+angle)*Math.cos(angle) + middleX;
            double y=20*(0+angle)*Math.sin(angle) + middleY;

            path.lineTo((float)x, (float)y);
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

        boolean up = false;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                currPath.lineTo(x,y);
                break;
            case MotionEvent.ACTION_UP:
                up = true;
                currPath.lineTo(x,y);
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



    // The save feature
    // Taken from code in Jon Froehlich's CMSC434 class.
    public void saveTestToGallery(ContentResolver cr){

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
        paintText.setTextSize(30);

        // Here is where we would add the stats, such as score or lifts, to the report bitmap.
        // Just add _offScreenCanvas.getHeight() to every y value.
        SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        _reportCanvas.drawText("Time of test: "+date.format(new Date()), 0,_offScreenCanvas.getHeight()+200, paintText);

        String fName = UUID.randomUUID().toString() + ".png";
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Spiral Test Results");
        if(!folder.exists()){
            if(!folder.mkdirs()){
                System.out.println("MOTHER FUCK!!!");
            }
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Spiral Test Results/"+fName);
        try {
            _reportBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));

            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());

            getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
