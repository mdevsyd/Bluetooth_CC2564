package com.mdevsolutions.cc2564;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;

import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class DataViewerActivity extends AppCompatActivity {

    ListView dataList;
    JsonResonse jsonResponseObj;
    DataViewerAdapter dataViewerAdapter;
    String url = "https://api.ictcommunity.org/v0/Accounts/A3JHBLG1ZBYLK63Q/D0000001/?channels=Weight%20(g),DateAndTime";
    Gson gson;
    AsyncHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_viewer);

        dataList = (ListView) findViewById(R.id.dataList);

        // Instance of the http client
        httpClient = new AsyncHttpClient();
        httpClient.get(DataViewerActivity.this, url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Convert byte response to String
                String response = new String(responseBody);

                // Instance of the Gson object
                gson = new Gson();

                jsonResponseObj = gson.fromJson(response, JsonResonse.class);

                // Instanciate adapter and set to listview
                dataViewerAdapter = new DataViewerAdapter(DataViewerActivity.this, jsonResponseObj.getData());
                dataList.setAdapter(dataViewerAdapter);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setTite("Data Plot");

    }

    private void setTite(String s) {
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
