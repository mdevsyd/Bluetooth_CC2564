package com.mdevsolutions.cc2564;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class DataViewerActivity extends AppCompatActivity {

    // Table data member fields
    ListView mDataList;
    JsonResponse mJsonResponseObj;
    DataViewerAdapter mDataViewerAdapter;
    String mUrl = "https://api.ictcommunity.org/v0/Accounts/A3JHBLG1ZBYLK63Q/D0000001/?channels=Weight%20(g),DateAndTime";
    Gson mGson;
    AsyncHttpClient mHttpClient;

    // Graphed data member fields
    private LineChart mLineChart;
    private ProgressDialog mPDialog;
    private ArrayList mValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_viewer);

        mDataList = (ListView) findViewById(R.id.dataList);
        mLineChart = (LineChart) findViewById(R.id.dataPlot);
        mJsonResponseObj = new JsonResponse();
        mValues = new ArrayList<>();
        mHttpClient = new AsyncHttpClient();

        mHttpClient.get(DataViewerActivity.this, mUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Convert byte response to String
                String response = new String(responseBody);

                // Commence populating table and graph using returned values from API
                new populateDataAsyncTask().execute(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(DataViewerActivity.this, R.string.http_fail, Toast.LENGTH_SHORT).show();
            }
        });

        //Setup the activity toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setTitle("Data Plot");

    }

    public class populateDataAsyncTask extends AsyncTask<String, Void, ArrayList> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList doInBackground(String... params) {
            Log.d(Constants.DEBUG_TAG, "Inside the AsyncTask");
            // Instance of the http client

            // Instance of the Gson object
            mGson = new Gson();

            String response = params[0];

            mJsonResponseObj = mGson.fromJson(response, JsonResponse.class);
            List<JsonResponse.DataBean> jsonBean = mJsonResponseObj.getData();
            int temp = 0;

            // TODO the following simplifies testing to get 100 entries. Replace with
            // TODO cont.. for (JsonResponse.DataBean data : jsonBean) {...}
            //for (JsonResponse.DataBean data : jsonBean) {
            for (int i = 0; i < 100; i++) {

                //Log.d(Constants.DEBUG_TAG, "Weight is:  " + data.get_$WeightG94());
                float weightFloat = Float.parseFloat(jsonBean.get(i).get_$WeightG94());
                Entry entry = new Entry(temp, weightFloat);
                mValues.add(entry);
                temp++;
            }

            return mValues;
        }

        @Override
        protected void onPostExecute(ArrayList mValues) {
            super.onPostExecute(mValues);

            // TODO this currently only generates Line Graph, need a Graph.java class to handle diff types of graphs
            LineDataSet setOfData = new LineDataSet(mValues, "Weight (g)");
            List<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setOfData);

            LineData lineD = new LineData(dataSets);
            mLineChart.setData(lineD);
            mLineChart.invalidate();

            // Instanciate adapter and set to listview
            mDataViewerAdapter = new DataViewerAdapter(DataViewerActivity.this, mJsonResponseObj.getData());
            mDataList.setAdapter(mDataViewerAdapter);

        }
    }


    private void setTitle(String s) {
        ActionBar actionBar = getSupportActionBar();
        //ensure there is an actionbar in the activity
        if (null == actionBar) {
            return;
        }
        actionBar.setTitle(s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.data_plot_menu, menu);
        return true;
    }


}
