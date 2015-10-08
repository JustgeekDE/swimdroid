package de.justgeek.swimdroid.util;

import android.hardware.SensorEvent;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ppeter on 06/10/2015.
 */
public class DataLogger {

    FileOutputStream outputStream;

    public DataLogger(String filename) {
        Long timestamp = System.currentTimeMillis() / 1000;
        outputStream = openFile(filename, timestamp);
    }

    public DataLogger(String filename, long timestamp) {
        outputStream = openFile(filename, timestamp);
    }

    private FileOutputStream openFile(String name, Long timestamp) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        try {
            String fileName = String.format("%s/%d.-.%s.csv", downloadDir.getAbsolutePath(), timestamp, name);
            fileName = fileName.replace(" ", ".");
            FileOutputStream fos = new FileOutputStream(new File(fileName), false);
            return fos;
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public void closeFile() {
        try {
            outputStream.close();
        } catch (IOException e) {
        }
    }

    public void storeSensorEvent(SensorEvent event, long timeOffset) {
        long timestamp = (event.timestamp - timeOffset) / 1000000l;
        String dataString = timestamp + ", " + Arrays.toString(event.values).replace("[", "").replace("]", "") + "\n";
        try {
            outputStream.write(dataString.getBytes());
        } catch (IOException e) {
        }
    }

    public void store(String data) {
        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
        }
    }
}
