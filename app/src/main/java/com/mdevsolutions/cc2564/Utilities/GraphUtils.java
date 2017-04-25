package com.mdevsolutions.cc2564.Utilities;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Michi on 16/04/2017.
 */

public class GraphUtils {

/*
    private ArrayList data;
    private String type;
    private String label;
    private String resource;
    private LineChart lineChart;*/



    public LineChart createLineChart(ArrayList data, String label, LineChart lineChart, String ref){
        LineDataSet setOfData = new LineDataSet(data, ""+label);
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setOfData);

        LineData lineD = new LineData(dataSets);
        lineChart.setData(lineD);
        setupXAxes(lineChart, ref);
        lineChart.invalidate();
        lineChart.setHighlightPerTapEnabled(true);

        return lineChart;

    }


    private void setupXAxes(LineChart lineChart, String ref) {
        Long reference;

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(8f);
        xAxis.setTextColor(Color.GREEN);
        xAxis.setDrawAxisLine(true);

        // Setup format for X Axes
        reference = convertDateToMs(ref);
        HourAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(reference);
        xAxis.setValueFormatter(xAxisFormatter);

    }

    /**
     * Convert a String timestamp to ms and use initial time as zero reference point
     * to reduce size of numbers to be graphed.
     * @param date String of date to be converted
     * @return Long of date after processing
     */
    public Long convertDateToMs(String date){
        java.text.DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date newDate=null;
        try {
            newDate = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate.getTime();
    }

    /**
     * Reduces the size of the timestamp to be stored in the data array.
     * @param value - datestamp to be reduced
     * @param ref - the zero value
     * @return new value to be used in activity
     */
    public Long reduceTimestampSize(Long value, String ref){

        Long reference = convertDateToMs(ref);

        return value - reference;
    }


}
