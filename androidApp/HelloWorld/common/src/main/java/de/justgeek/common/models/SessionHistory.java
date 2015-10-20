package de.justgeek.common.models;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SessionHistory {
    List<Session> sessions = new ArrayList<>();

    public static SessionHistory fromString(String data) {
        Gson gson = new GsonBuilder().create();
        SessionHistory sessionHistory = gson.fromJson(data, SessionHistory.class);
        return sessionHistory;
    }

    public static SessionHistory load() {
        try {
            File storedData = getStorageFile();
            int length = (int) storedData.length();
            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(storedData);
            in.read(bytes);
            in.close();
            String content = new String(bytes);

            Log.d("sd.sessionHistory", content);
            return fromString(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SessionHistory();
    }

    private static File getStorageFile() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = String.format("%s/swimdroidHistory.json", downloadDir.getAbsolutePath());
        return new File(fileName);
    }

    public void addSession(Session session) {
        sessions.add(session);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public void store() {
        String data = this.toString();
        try {
            FileOutputStream fos = new FileOutputStream(getStorageFile(), false);
            fos.write(data.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Session getLastSession() {
        Session session = null;
        if (sessions.size() > 0) {
            session = sessions.get(sessions.size() - 1);
        }

        return session;
    }
}
