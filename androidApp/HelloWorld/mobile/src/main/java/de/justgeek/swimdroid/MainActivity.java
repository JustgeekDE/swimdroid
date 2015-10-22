package de.justgeek.swimdroid;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.justgeek.common.models.PoolLength;
import de.justgeek.common.models.Session;
import de.justgeek.common.models.SessionHistory;
import de.justgeek.common.util.BroadcastCallback;
import de.justgeek.common.util.BroadcastHelper;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends Activity implements BroadcastCallback {

    private static final String TAG = "swimdroid.phone.main";
    private BroadcastHelper broadcastHelper = new BroadcastHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastHelper.create(this, this);
        setContentView(R.layout.activity_main);

        SessionHistory sessionHistory = SessionHistory.load();
        updateDisplay(sessionHistory);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        broadcastHelper.connect(this);
    }

    @Override
    protected void onStop() {
        broadcastHelper.disconnect(this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void handleBroadcast(String type, String data) {
        log("Got new broadcast: " + type);
        switch (type) {
            case "history":
                SessionHistory sessionHistory = SessionHistory.fromString(data);
                updateDisplay(sessionHistory);
                sessionHistory.store();
                break;
        }
    }

    private void updateDisplay(SessionHistory sessionHistory) {
        Session session = sessionHistory.getLastSession();
        if (session != null) {
            setTextFieldValue(R.id.sessionDate, toDateTimeString(session.getStart()));

            setTextFieldValue(R.id.sessionDuration, String.format("%.1f min", session.activeTime() / 60000.0f));
            setTextFieldValue(R.id.sessionBreak, String.format("%.1f min", ((session.getEnd() - session.getStart()) - session.activeTime()) / 60000.0f));
            setTextFieldValue(R.id.sessionCalories, String.format("%.0f kcal", session.getCalories()));

            setTextFieldValue(R.id.sessionStrokes, String.format("%d strokes", session.strokes()));
            setTextFieldValue(R.id.sessionLengths, String.format("%d lengths", session.getLengthCount()));
            setTextFieldValue(R.id.sessionDistance, String.format("%d m", session.distance()));

            setTextFieldValue(R.id.fastestDuration, String.format("%.1f s", session.fastestLength().activeTime() / 1000.0f));
            setTextFieldValue(R.id.fastestSpeed, String.format("%.2f km/h", session.fastestLength().getSpeed(session.getPoolLength())));
            setTextFieldValue(R.id.fastestStrokes, String.format("%d strokes", session.fastestLength().getStrokes()));


            setTextFieldValue(R.id.averageDuration, String.format("%.1f s", session.averageDuration() / 1000.0f));
            setTextFieldValue(R.id.averageSpeed, String.format("%.2f km/h", session.averageSpeed()));
            setTextFieldValue(R.id.averageStrokes, String.format("%.0f strokes", session.averageStrokes()));

            setTextFieldValue(R.id.slowestDuration, String.format("%.1f s", session.slowestLength().activeTime() / 1000.0f));
            setTextFieldValue(R.id.slowestSpeed, String.format("%.2f km/h", session.slowestLength().getSpeed(session.getPoolLength())));
            setTextFieldValue(R.id.slowestStrokes, String.format("%d strokes", session.slowestLength().getStrokes()));

            updateSpeedChart(session);
        }
    }

    private void updateSpeedChart(Session session) {
        List<PointValue> values = new ArrayList<PointValue>();

        for(PoolLength length: session.getLengths()) {
            float speed = length.getSpeed(session.getPoolLength());
            values.add(new PointValue(length.getStartTime(), speed));
            values.add(new PointValue(length.getEndTime(), speed));
        }

        List<PointValue> zeroValues = new ArrayList<PointValue>();
        zeroValues.add(new PointValue(0.0f, 0.0f));

        Line line = new Line(values).setColor(Color.parseColor("#FF8800")).setCubic(false).setHasPoints(false);
        List<Line> lines = new ArrayList<Line>();

        lines.add(line);
        lines.add(new Line(zeroValues).setColor(Color.TRANSPARENT).setCubic(false).setHasPoints(false));

        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setAxisXBottom(new Axis().setAutoGenerated(false));
        data.setAxisYLeft(new Axis().setHasLines(true));

        LineChartView chart = (LineChartView) findViewById(R.id.speedGraph);
        chart.setInteractive(false);
        chart.setLineChartData(data);
    }

    private void setTextFieldValue(int id, String newValue) {
        TextView textField = (TextView) findViewById(id);
        textField.setText(newValue);
    }

    private String toDateTimeString(long timestamp) {
        DateFormat formater = DateFormat.getDateTimeInstance();
        return formater.format(new Date(timestamp));
    }

    private String toTimeString(long timestamp) {
        DateFormat formater = DateFormat.getTimeInstance();
        return formater.format(new Date(timestamp));
    }

    private void log(String data) {
        Log.d(TAG, data);
    }

}
