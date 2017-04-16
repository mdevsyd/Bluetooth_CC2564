package com.mdevsolutions.cc2564;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michi on 16/04/2017.
 */

public class Graph {


    private ArrayList data;
    private String type;
    private String label;
    private String resource;
    private LineChart lineChart;

    public LineChart createLineChart(ArrayList data, String label, LineChart lineChart){
        LineDataSet setOfData = new LineDataSet(data, ""+label);
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setOfData);

        LineData lineD = new LineData(dataSets);
        lineChart.setData(lineD);
        lineChart.invalidate();

        return lineChart;

    }
}
