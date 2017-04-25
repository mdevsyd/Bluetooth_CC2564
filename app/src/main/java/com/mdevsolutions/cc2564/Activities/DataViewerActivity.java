package com.mdevsolutions.cc2564.Activities;

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
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.Entry;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mdevsolutions.cc2564.Adapters.DataViewerAdapter;
import com.mdevsolutions.cc2564.JsonModelData.JsonResponse;
import com.mdevsolutions.cc2564.R;
import com.mdevsolutions.cc2564.Utilities.Constants;
import com.mdevsolutions.cc2564.Utilities.GraphUtils;

import java.util.ArrayList;
import java.util.List;

import Work.PopupMarker;
import cz.msebera.android.httpclient.Header;

public class DataViewerActivity extends AppCompatActivity {

    // Table data member fields
    ListView mDataList;
    JsonResponse mJsonResponseObj;
    DataViewerAdapter mDataViewerAdapter;
    String mUrl = "https://api.ictcommunity.org/v0/Accounts/A3JHBLG1ZBYLK63Q/D0000001/?channels=Weight%20(g),DateAndTime";
    Gson mGson;
    AsyncHttpClient mHttpClient;
    String refDateTime = "";
    String globalDateTimeRef="";


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
            GraphUtils graph = new GraphUtils();
            Long convertedTimestamp;
            Long reducedTimestamp;
            Long referenceDateTime =0L;

            ArrayList toConvert = new ArrayList();

            // Instance of the Gson object
            mGson = new Gson();

            // Obtain reference to the returned JSON string
            String response = params[0];

            mJsonResponseObj = mGson.fromJson(response, JsonResponse.class);
            List<JsonResponse.DataBean> jsonBean = mJsonResponseObj.getData();

            // TODO find better way to achieve this
            for (int i =0; i<1;i++){
                refDateTime =jsonBean.get(i).getDateAndTime();
            }

            // TODO the following simplifies testing by reducing amount of entries. Replace with --> for(JsonResponse.DataBean data : jsonBean) {...}
            //for (JsonResponse.DataBean data : jsonBean) {
            for (int i = 0; i < 10; i++) {
                //String dateTime = jsonBean.get(i).getDateAndTime();
                convertedTimestamp = graph.convertDateToMs(jsonBean.get(i).getDateAndTime());
                Log.d(Constants.DEBUG_TAG,"Converted timestamp is : " +convertedTimestamp);
                //reducedTimestamp = graph.reduceTimestampSize(convertedTimestamp, refDateTime);
                float dateTimeFloat = Float.parseFloat(String.valueOf(graph.reduceTimestampSize(convertedTimestamp, refDateTime)));
                float weightFloat = Float.parseFloat(jsonBean.get(i).get_$WeightG94());
                Entry entry = new Entry(dateTimeFloat, weightFloat);
                Log.d(Constants.DEBUG_TAG, "Entry is : "+ entry);
                mValues.add(entry);

            }

            return mValues;
        }

        @Override
        protected void onPostExecute(ArrayList mValues) {
            super.onPostExecute(mValues);

            // TODO currently testing with line chart only, test other charts too
            GraphUtils graph = new GraphUtils();
            graph.createLineChart(mValues,"This is a label", mLineChart, refDateTime);

           // MyMarkerView myMarkerView= new MyMarkerView(getApplicationContext(), R.layout.custom_marker, graph.convertDateToMs(refDateTime));
            IMarker marker = new PopupMarker(DataViewerActivity.this, R.layout.custom_marker);
            mLineChart.setMarker(marker);

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
