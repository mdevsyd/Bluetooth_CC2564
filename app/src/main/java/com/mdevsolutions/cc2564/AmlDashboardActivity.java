package com.mdevsolutions.cc2564;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.mdevsolutions.cc2564.JsonModelData.AMLDashboardModel;
import com.mdevsolutions.cc2564.Utilities.Constants;
import com.mdevsolutions.cc2564.Utilities.GraphUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AmlDashboardActivity extends AppCompatActivity {

    private String mKey ="";
    private Button mSearchBtn;
    private TextView mUploadDate;
    private TextView mBattV;
    private TextView mSerial;
    private TextView mSolarEt;
    private Gson mGson;
    private AMLDashboardModel mAmlObject;
    private ProgressDialog mDialog;
    private LineChart mLineChart;
    private ArrayList mTemperatures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aml_dashboard);

        Intent intent = getIntent();
        mKey=intent.getStringExtra("Key");
        Toast.makeText(AmlDashboardActivity.this, "your key is: "+mKey, Toast.LENGTH_SHORT).show();

        mBattV = (TextView) findViewById(R.id.battVEt);
        mSerial = (TextView) findViewById(R.id.serialEt);
        mSolarEt = (TextView) findViewById(R.id.solarEt);
        mUploadDate = (TextView) findViewById(R.id.uploadDate);
        mLineChart = (LineChart) findViewById(R.id.dataPlot);
        mTemperatures = new ArrayList();

        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setMessage("Retrieving Data...");

        mAmlObject = new AMLDashboardModel();

        mSearchBtn = (Button)findViewById(R.id.searchBtn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(AmlDashboardActivity.this, SearchActivity.class);
                startActivity(searchIntent);
            }
        });

        // TODO this is using a dummy URL with set date range and AML.
        new JsonAsyncTask().execute(Constants.amlDashboardUrl);
    }

    public class JsonAsyncTask extends AsyncTask<String, String, List<AMLDashboardModel.DataBean>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected List<AMLDashboardModel.DataBean> doInBackground(String... params) {

            HttpURLConnection mConnection = null;
            BufferedReader reader = null;


            try {
                URL url = new URL(params[0]);
                mConnection = (HttpURLConnection) url.openConnection();
                mConnection.connect();

                mGson = new Gson();

                InputStream stream =mConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";
                StringBuffer buffer = new StringBuffer();

                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                //return the buffer to display on UI

                String finalJson = buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                mAmlObject = mGson.fromJson(finalJson, AMLDashboardModel.class);
                List<AMLDashboardModel.DataBean> amlBean = mAmlObject.getData();


                return amlBean;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                if(mConnection != null) {
                    mConnection.disconnect();
                }
                try {
                    if(reader != null){
                        reader.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<AMLDashboardModel.DataBean> result) {
            super.onPostExecute(result);
            mDialog.dismiss();

            int listLength = result.size();

            //TODO fix string concatenation

            mBattV.setText(result.get(listLength-1).get_$BatteryVoltageV54()+ getString(R.string.volts));
            mSolarEt.setText(result.get(listLength-1).get_$ExternalSupplyVoltageV207() + getString(R.string.volts));
            mUploadDate.setText(result.get(listLength-1).getDateAndTime());

            List<Entry> temperatureEntries = new ArrayList<Entry>();
            List<Entry> battVEntries = new ArrayList<Entry>();
            List<Entry> externalVEntries = new ArrayList<Entry>();
            List<List<Entry>> allData = new ArrayList<>();


            // Separate data into Arrays of their own for graphing. - TODO currently using 'i' as temp x axis value

            for (int i = 0; i< result.size(); i++) {
                Float tempFloat = Float.parseFloat(String.valueOf(result.get(i).get_$Temperature_1DegC196()));
                temperatureEntries.add(new Entry(i, tempFloat));
                allData.add(temperatureEntries);

                Float battVFloat = Float.parseFloat(String.valueOf(result.get(i).get_$BatteryVoltageV54()));
                battVEntries.add(new Entry(i, battVFloat));
                allData.add(battVEntries);

                Float solarVFloat = Float.parseFloat(String.valueOf(result.get(i).get_$ExternalSupplyVoltageV207()));
                externalVEntries.add(new Entry(i, solarVFloat));
                allData.add(externalVEntries);
            }

            // create the line chart
            LineDataSet tempSet = new LineDataSet(temperatureEntries,"Temperature (deg.C)");
            tempSet.setColor(Color.RED);
            tempSet.setDrawCircles(false);
            LineDataSet battVset = new LineDataSet(battVEntries, "Battery Voltage (V)");
            battVset.setColor(Color.BLUE);
            battVset.setDrawCircles(false);
            LineDataSet extVSet = new LineDataSet(externalVEntries, "Supply Voltage (V)");
            extVSet.setColor(Color.GREEN);
            extVSet.setDrawCircles(false);


            List<ILineDataSet> allDataSets = new ArrayList<ILineDataSet>();
            allDataSets.add(tempSet);
            allDataSets.add(battVset);
            allDataSets.add(extVSet);

            LineData data = new LineData(allDataSets);
            mLineChart.setData(data);
            mLineChart.invalidate();
        }
    }

}
