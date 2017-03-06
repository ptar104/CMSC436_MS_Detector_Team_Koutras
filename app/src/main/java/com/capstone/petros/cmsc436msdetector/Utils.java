package com.capstone.petros.cmsc436msdetector;
import android.app.Activity;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by peterkoutras on 3/5/17.
 */

public class Utils extends Activity {

    public static boolean appendResultsToInternalStorage(Context context, String filename, double data) {
        Long date = System.currentTimeMillis();

        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            String dataString = date + " " + data+"\n";
            outputStream.write(dataString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static TreeMap getResultsFromInternalStorage(Context context, String fileName) {

        TreeMap<Long, Double> map = new TreeMap<>();

        try {
            FileInputStream fis = context.openFileInput(fileName);
            Scanner s = new Scanner(fis);
            while(s.hasNext()){
                map.put(s.nextLong(), s.nextDouble());
                //String []line = s.nextLine().split(" ");
                //map.put(Long.getLong(line[0]), );
            }
            s.close();
        }catch(Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}
