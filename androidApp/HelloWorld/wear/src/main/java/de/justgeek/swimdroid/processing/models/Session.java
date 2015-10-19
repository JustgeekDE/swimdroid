package de.justgeek.swimdroid.processing.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class Session {
    long start = 0;
    long end = 0;
    int lengthCount = 0;
    List<PoolLength> lengths;

    public Session() {
        lengths = new ArrayList<>();
    }

    public void addLength(PoolLength length) {
        lengths.add(length);

        if ((start == 0) || (length.getStartTime() < start)) {
            start = length.getStartTime();
        }

        if (length.getEndTime() > end) {
            end = length.getEndTime();
        }
        lengthCount = lengths.size() + 1;
    }

    public int getLengthCount() {
        return lengthCount;
    }

    public PoolLength getLastLength() {
        if(lengths.size() > 0) {
            return lengths.get(lengths.size()-1);
        }
        return null;
    }

    public long getStart() {
        return start;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static Session fromString(String data) {
        Gson gson = new GsonBuilder().create();
        Session session = gson.fromJson(data, Session.class);
        return session;
    }
}
