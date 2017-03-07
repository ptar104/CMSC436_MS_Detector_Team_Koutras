package com.capstone.petros.cmsc436msdetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by peterkoutras on 3/1/17.
 */

public class GraphView extends View {

    private SortedMap<Long, Double> data;
    private HashMap<Point, Tuple> pointsToData = new HashMap<>();
    private Point currPoint = null;

    Paint axisPaint; // For the X/Y axis
    Paint gridPaint; // For the grid lines
    Paint linePaint; // For the user trend lines
    Paint pointPaint; // For the user data points
    Paint dateTextPaint; // For the date text
    Paint dataTextPaint; // For the data text
    Paint popupPaint; // For the popup
    Paint popupTextPaint; // For the popup text
    Paint noDataTextPaint; // For the noData text

    public GraphView(Context context) {
        super(context);
        init(null, 0);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public void setData(SortedMap<Long, Double> data){
        this.data = data;
    }

    private void fakeData(){
        data = new TreeMap<>();
        data.put(12L, 0.3);
        data.put(11640012L, 0.3);
        data.put(42200012L, 11.3);
        data.put(63400012L, 5.0);
        data.put(84600012L, 2.1);
        data.put(105800012L, 2.1);
        data.put(127000012L, 10.9);
        data.put(148200012L, 8.8);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        axisPaint = new Paint();
        axisPaint.setAntiAlias(true);
        axisPaint.setStrokeWidth(5);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setColor(Color.BLACK);

        gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(2);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(0xFF777777);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(4);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.RED);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStrokeWidth(1);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(Color.RED);

        dateTextPaint = new Paint();
        dateTextPaint.setAntiAlias(true);
        dateTextPaint.setTextSize(100); // I hope it's px...
        dateTextPaint.setStyle(Paint.Style.STROKE);
        dateTextPaint.setColor(Color.BLACK);

        dataTextPaint = new Paint();
        dataTextPaint.setAntiAlias(true);
        dataTextPaint.setTextSize(100); // I hope it's px...
        dataTextPaint.setStyle(Paint.Style.STROKE);
        dataTextPaint.setColor(Color.BLACK);

        popupPaint = new Paint();
        popupPaint.setAntiAlias(true);
        popupPaint.setStrokeWidth(1);
        popupPaint.setStyle(Paint.Style.FILL);
        popupPaint.setColor(0xFF777777); // If transparent doesn't look good, change

        popupTextPaint = new Paint();
        popupTextPaint.setAntiAlias(true);
        popupTextPaint.setTextSize(100); // I hope it's px...
        popupTextPaint.setStyle(Paint.Style.STROKE);
        popupTextPaint.setColor(Color.BLACK);

        noDataTextPaint = new Paint();
        noDataTextPaint.setAntiAlias(true);
        noDataTextPaint.setTextSize(20); // I hope it's px...
        noDataTextPaint.setStyle(Paint.Style.STROKE);
        noDataTextPaint.setColor(Color.BLACK);
    }


    @Override
    public void onDraw(Canvas canvas) {
        int width = canvas.getWidth(), height = canvas.getHeight();

        if(data == null || data.isEmpty()){
            String noData = "[No data to show for this graph.]";
            canvas.drawText(noData,
                    getWidth()/2 - noDataTextPaint.measureText(noData)/2,
                    getHeight()/2 -10,
                    noDataTextPaint);
            canvas.drawRect(0,0,getWidth(),getHeight(),axisPaint);
            return;
        }

        // Want to draw a graph here, given certain X and Y data.
        // Graph will scale according to width and height necessary.
        long minX = 0, maxX = 0;
        double minY = 0, maxY = 0;
        if(!data.isEmpty()){
            long firstTuple = data.firstKey();
            minX = firstTuple;
            maxX = firstTuple;
            minY = data.get(firstTuple);
            maxY = data.get(firstTuple);
        }
        for(Long i : data.keySet()){
            if(i > maxX){
                maxX = i;
            }
            else if(i < minX){
                minX = i;
            }
            if(data.get(i) > maxY){
                maxY = data.get(i);
            }
            else if(data.get(i) < minY){
                minY = data.get(i);
            }
        }
        // Add some space...
        double graphRangeX = maxX - minX;
        double graphRangeY = maxY - minY;
        maxX += graphRangeX/20.0;
        minX -= graphRangeX/20.0;
        maxY += graphRangeY/20.0;
        minY -= graphRangeY/20.0;
        graphRangeX = maxX - minX;
        graphRangeY = maxY - minY;

        //Set the text size of the data
        String sampleData = "77.7";
        while(dataTextPaint.measureText(sampleData) > width/20.0){
            dataTextPaint.setTextSize(dataTextPaint.getTextSize() - 1);
        }

        // Want to calculate how much space is needed on the left and right of the graph.
        // Note: can also scale text size down here to a certain extent (ex: until size 10)

        DecimalFormat df = new DecimalFormat("#.0");
        int leftSpace = (int)(dataTextPaint.measureText(df.format(maxY)) + 6);

        double rangeX = width-(30+leftSpace), rangeY = height-27; // "left space" left, 20 below,
                                                                  // 30 to the right and 7 above

        //System.out.println("Date width: " + dateTextPaint.measureText("77/77"));
        //Rect r = new Rect();
        //dateTextPaint.getTextBounds("77/77", );
        //System.out.println("Graph width/10: " + graphRangeX/10.0);

        //Set up the text size.
        Rect bounds = new Rect();
        String sampleString = "77/77";
        dateTextPaint.getTextBounds(sampleString, 0, sampleString.length(), bounds);
        while((bounds.width() > rangeX/10.0 || bounds.height() > 17) &&
                dateTextPaint.getTextSize() > 5){
            dateTextPaint.setTextSize(dateTextPaint.getTextSize() - 1);
            dateTextPaint.getTextBounds(sampleString, 0, sampleString.length(), bounds);
        }
        sampleString = "Date: 77:77 AM, 77/77/7777";
        popupTextPaint.getTextBounds(sampleString, 0, sampleString.length(), bounds);
        while (bounds.width() > 4.0*rangeX/10.0){
            popupTextPaint.setTextSize(popupTextPaint.getTextSize() - 1);
            popupTextPaint.getTextBounds(sampleString, 0, sampleString.length(), bounds);
        }


        //draw the Y grid lines.
        SimpleDateFormat date = new SimpleDateFormat("MM/dd");
        String dateString = date.format(new Date(minX));
        canvas.drawText(dateString,leftSpace - dateTextPaint.measureText(dateString)/2,height - 1, dateTextPaint);
        for(int i = 1; i <= 10; i++){
            dateString = date.format(new Date((long)(minX + i * graphRangeX/10.0)));
            int xLine = leftSpace + (int)Math.round(i * rangeX/10.0);
            canvas.drawLine(xLine, height-20, xLine, 7, gridPaint);
            if(i%2 == 0) {
                canvas.drawText(dateString, xLine - dateTextPaint.measureText(dateString)/2,
                        height - 1, dateTextPaint);
            }
        }

        // Here, if there isn't enough space, probably want to increase left X space...
        df = new DecimalFormat("#.#");
        // Draw the X grid lines
        for(int i = 1; i <= 10; i++){
            int yLine = 20 + (int)Math.round(i * rangeY/10.0);
            canvas.drawLine(leftSpace, height-yLine, width-30, height-yLine, gridPaint);
            if(i%2 == 0){
                String dataPoint = df.format(minY + i * graphRangeY/10.0);
                int textLength = (int)dataTextPaint.measureText(dataPoint);
                canvas.drawText(dataPoint, leftSpace - textLength - 6,
                        height-yLine + (int)(dataTextPaint.getTextSize()/2), dataTextPaint);
            }
        }

        //draw the X and Y lines, so they appear on top of the grid lines.
        canvas.drawLine(leftSpace, height-20, width-30, height-20, axisPaint);
        canvas.drawLine(leftSpace, height-20, leftSpace, 7, axisPaint);

        //Now draw the the data points. They should be sorted in time order now.
        int prevX = -1, prevY = -1;
        for(Long l : data.keySet()){
            double percentAcrossX = (l - minX) / graphRangeX;
            int pixelX = (int)Math.round(rangeX * percentAcrossX);
            double percentAcrossY = (data.get(l) - minY) / graphRangeY;
            int pixelY = (int)Math.round(rangeY * percentAcrossY);

            // Draw the dot.
            canvas.drawCircle(leftSpace+pixelX, height - (pixelY + 20), 5, pointPaint);
            if(prevX != -1){
                canvas.drawLine(prevX, prevY, leftSpace+pixelX, height - (pixelY + 20), linePaint);
            }
            Point p = new Point(leftSpace+pixelX, height - (pixelY + 20));
            pointsToData.put(p, new Tuple(l, data.get(l)));

            prevX = leftSpace+pixelX;
            prevY = height - (pixelY + 20);
        }

        // If this is all too cumbersome to draw in one go, save to bitmap instead.
        if(currPoint != null) {
            // Draw the popup
            // First, is it up or down, left or right?
            boolean up = currPoint.y >= height - 20 - (rangeY / 2);
            boolean right = currPoint.x <= leftSpace + (rangeX / 2);

            Tuple dataTuple = pointsToData.get(currPoint);
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a, MM/dd/yyyy");
            DecimalFormat decimalFormat = new DecimalFormat("#.###");
            String dateText = "Date: " + dateFormat.format(dataTuple.x);
            String valueText = "Value: " + decimalFormat.format(dataTuple.y);
            Rect dateBounds = new Rect();
            popupTextPaint.getTextBounds(dateText, 0, dateText.length(), dateBounds);
            Rect valueBounds = new Rect();
            popupTextPaint.getTextBounds(valueText, 0, valueText.length(), valueBounds);
            int textHeight = dateBounds.height() + 4 + valueBounds.height();
            int textWidth = Math.max(dateBounds.width(), valueBounds.width());


            // The box will be 20 pixels away from point in either direction,
            // and the text will have a 5-pixel margin around it.
            int textRightOffset = right ? 25 : -25 - textWidth;
            int textUpOffset = up ? 25 + textHeight : -25;
            int boxRightOffset = right ? 20 : -20 - textWidth - 10;
            int boxUpOffset = up ? 20 + textHeight + 10 : -20;

            // Draw the text, box, and triangle nub
            int x = currPoint.x, y = currPoint.y;
            canvas.drawRect(x + boxRightOffset, y - boxUpOffset,
                    x + boxRightOffset + textWidth + 10,
                    y - boxUpOffset + textHeight + 10,
                    popupPaint); // Left, up, right, down, paint
            Path path = new Path();
            path.moveTo(x + (right ? 25 : -25), y - (up ? 20 : -20));
            path.lineTo(x, y);
            path.lineTo(x + (right ? 20 : -20), y - (up ? 25 : -25));
            canvas.drawPath(path, popupPaint);
            canvas.drawText(dateText, x + textRightOffset, y - textUpOffset + dateBounds.height(), popupTextPaint);
            canvas.drawText(valueText, x + textRightOffset, y - textUpOffset + textHeight, popupTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        float x = motionEvent.getX(), y = motionEvent.getY();
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            // Find the closest data point.
            Point closestPoint = null;
            double minDistance = Integer.MAX_VALUE;
            for (Point p : pointsToData.keySet()) {
                double distance = Math.sqrt(Math.pow(y - p.y, 2) + Math.pow(x - p.x, 2));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPoint = p;
                }
            }
            // Is the distance reasonable close?
            if (minDistance < getWidth() / 10.0 && closestPoint != currPoint) {
                currPoint = closestPoint;
            }
            else{
                currPoint = null;
            }
            invalidate();
        }
        return true;
    }

    private class Tuple{
        long x;
        double y;

        public Tuple(long x, double y){
            this.x = x;
            this.y = y;
        }
    }


}
