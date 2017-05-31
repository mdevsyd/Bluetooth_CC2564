package com.mdevsolutions.cc2564.Activities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mdevsolutions.cc2564.Adapters.HubsViewAdapter;
import com.mdevsolutions.cc2564.Adapters.InstrumentsViewAdapter;
import com.mdevsolutions.cc2564.Adapters.SearchViewAdapter;
import Work.JsonAccountModel;
import Work.JsonDataModel;
import com.mdevsolutions.cc2564.R;
import com.mdevsolutions.cc2564.Utilities.Constants;
import com.mdevsolutions.cc2564.Utilities.HttpUtils;

import org.json.JSONArray;
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

public class TestSearchActivity extends AppCompatActivity {

    private ListView mSitesLv;
    private ListView mHubsLv;
    private ListView mInstrumentsLv;
    private SearchViewAdapter mSearchAdapter;
    private JsonDataModel mSitesObj;
    private JsonAccountModel mHubsObj;
    private Gson mGson;
    private HttpUtils mHttpUtil;
    private ProgressDialog mDialog;

    private JsonDataModel sitesJson;
    private JsonDataModel hubsJson;
    private JsonDataModel instJson;
    private JsonDataModel channelsJson;

    private Boolean mHttpSuccess;
    private Boolean mSiteDataSuccess;

    private String mResponse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSitesLv = (ListView) findViewById(R.id.siteLv);
        mHubsLv = (ListView) findViewById(R.id.hubLv);
        mInstrumentsLv = (ListView) findViewById(R.id.instrumentLv);

        //mSitesObj = new JsonAccountModel();
        mHubsObj = new JsonAccountModel();
        mHttpUtil = new HttpUtils();

        sitesJson = new JsonDataModel();
        hubsJson = new JsonDataModel();
        instJson = new JsonDataModel();
        channelsJson = new JsonDataModel();


        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setMessage("Contacting Site, Please Wait...");

        mHttpSuccess = false;
        mSiteDataSuccess = false;
        mResponse = "";


        // mJsonResponseObj = mGson.fromJson(response, JsonResponse.class);
        // List<JsonResponse.DataBean> jsonBean = mJsonResponseObj.getData();
        String baseUrl = mHttpUtil.buildUrl();
        try {
            URL url = new URL(baseUrl);
            new RetrieveJsonDataTask().execute(url);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.url_unrecognised, Toast.LENGTH_SHORT).show();
        }

        /*if (mHttpSuccess) {
            Log.d(Constants.DEBUG_TAG, "mHttpSuccess is: "+mHttpSuccess);
            new GetSiteDataTask().execute();
        }*/
        //new JsonAsyncTask().execute();
    }


    /**
     * Query API and return the JsonString of response.
     * Show and dismiss progress dialog while obtaining response.
     */
    public class RetrieveJsonDataTask extends AsyncTask<URL, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        HttpURLConnection mConnection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(URL... params) {

            URL url = params[0];

            try {

                mConnection = (HttpURLConnection) url.openConnection();
                mConnection.connect();

                InputStream stream = mConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";
                StringBuffer buffer = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();


            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (mConnection != null) {
                    mConnection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                mDialog.dismiss();
                mHttpSuccess = true;
                mResponse = result;
                new GetSiteDataTask().execute(mResponse);
            } else {
                Toast.makeText(getApplicationContext(), R.string.http_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class GetSiteDataTask extends AsyncTask<String, String, List>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Retrieving Your Data...");

        }

        @Override
        protected List doInBackground(String... params) {

            String response = params[0];
            Gson mGson = new Gson();
            sitesJson = mGson.fromJson(response, JsonDataModel.class);

            List<JsonDataModel.DataBean.SitesBean> sitesBean = sitesJson.getData().getSites();

            return sitesBean;

        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

            if (list != null) {
                SearchViewAdapter siteAdapter = new SearchViewAdapter(TestSearchActivity.this, list);
                mSitesLv.setAdapter(siteAdapter);
                mSitesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = ((TextView) view.findViewById(R.id.searchListItem)).getText().toString();
                        Log.d(Constants.DEBUG_TAG, "Selected listview item position is (Site): "+((String.valueOf(position))));
                        new GetHubDataTask().execute(mResponse, (String.valueOf(position)));
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_sites, Toast.LENGTH_SHORT).show();
            }

        }
    }



    public class GetHubDataTask extends AsyncTask<String, String, List>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Retrieving Your Data...");

        }

        @Override
        protected List doInBackground(String... params) {

            int position = (Integer.valueOf(params[1]));
            String selectedSite = params[1];
            Log.d(Constants.DEBUG_TAG, "Site is: " + position);
            String response = params[0];
            Gson mGson = new Gson();

            JSONObject parentObject = null;

            /*try {

                parentObject = new JSONObject(mResponse);
                JSONObject parentObj = parentObject.getJSONObject("data");
                JSONObject siteObj = parentObj[2];
                Log.d(Constants.DEBUG_TAG, "in try - array lenght "+ parentObj.length());

                List<JsonDataModel> dataList = new ArrayList<>();

                for (int i = 0; i < parentObj.length(); i++) {
                    JSONObject finalObject = parentObj.getJSONObject(i);
                    JSONArray sitesArray = finalObject.getJSONArray("name");
                    Log.d(Constants.DEBUG_TAG, "sitesArray entry " +sitesArray.getString(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }*/

            List<String> sitesListArray = new ArrayList<>();
            //hubsJson = mGson.fromJson(response, JsonDataModel.class);
            //mHubsObj = mGson.fromJson(finalJson, JsonAccountModel.class);*/

//            List<JsonDataModel.DataBean.SitesBean.HubsBean> hubsBean = hubsJson.getData().getSites().get(0).getHubs();

            return sitesListArray;

        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

            if (list != null) {

                HubsViewAdapter hubsViewAdapter = new HubsViewAdapter(getApplicationContext(), list);
                mHubsLv.setAdapter(hubsViewAdapter);
                //new GetInstrumentataTask().execute(mResponse);

            } else {
                Toast.makeText(getApplicationContext(), R.string.no_hubs, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class GetInstrumentataTask extends AsyncTask<String, String, List>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Retrieving Your Data...");

        }

        @Override
        protected List doInBackground(String... params) {

            String response = params[0];
            Gson mGson = new Gson();

            //List<String> sitesListArray = new ArrayList<>();
            instJson = mGson.fromJson(response, JsonDataModel.class);
            //mHubsObj = mGson.fromJson(finalJson, JsonAccountModel.class);

            // List<JsonDataModel.DataBean.SitesBean.HubsBean.InstrumentsBean> instBean = instJson.getData().getSites().get(0).getHubs().get(0).getInstruments();

            // TODO testing

            JSONObject parentObject = null;
            try {
                parentObject = new JSONObject(mResponse);
                JSONArray jsonArray = parentObject.getJSONArray("sites");

                List<String> instSerialArray = new ArrayList<>();

                for(int i=0; i<jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONArray hubArray = jsonObject.getJSONArray("hubs");
                    JSONArray instummentArray = hubArray.getJSONArray(0);
                    JSONArray serialArray = instummentArray.getJSONArray(1);
                    instSerialArray.add(serialArray.toString());

                }
                return instSerialArray;


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
            /*JSONObject parentObject = null;
            try {
                parentObject = new JSONObject(mResponse);
                JSONArray parentArray = parentObject.getJSONArray("hubs");

                List<JsonDataModel> siteList = new ArrayList<>();
                for(int i=0; i<parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);

                    JsonDataModel hubsObj = mGson.fromJson(finalObject.toString(), JsonDataModel.class);
                    siteList.add(hubsObj);
                }
                return siteList;


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;*/
          /*  List<JsonDataModel.DataBean.SitesBean> instBean = instJson.getData().getSites();

            List<JsonDataModel.DataBean.SitesBean.HubsBean.InstrumentsBean> instSerialsArray = new ArrayList<>();
            int x = 0;
           *//* for (JsonDataModel.DataBean.SitesBean.HubsBean.InstrumentsBean site : instBean){
                instSerialsArray.add(site.getSerial();
                x++;
                Log.d(Constants.DEBUG_TAG, "adding : "+ site.getHubs().get(x).getInstruments().get(x).toString());
            }*//*
            //for(int i = 0; i<50; i++){
              //  instSerialsArray.add(instJson.getData().getSites().get(i).getHubs().get(i).getInstruments().get(i));
           // }
            Log.d(Constants.DEBUG_TAG, "instbEan is :"+instBean.toString());
            return instBean;*/

        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

            if (list != null) {

                InstrumentsViewAdapter instrumentsViewAdapter = new InstrumentsViewAdapter(getApplicationContext(), list);
                mInstrumentsLv.setAdapter(instrumentsViewAdapter);

            } else {
                Toast.makeText(getApplicationContext(), R.string.no_hubs, Toast.LENGTH_SHORT).show();
            }

        }
    }




    public class JsonAsyncTask extends AsyncTask<String, String, List> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List doInBackground(String... params) {

            HttpURLConnection mConnection = null;
            BufferedReader reader = null;

            try {
                // TODO - this is a hacked URL build.

                String url = mHttpUtil.buildUrl();
                //url = url+ Constants.extra_string_ict;
                Log.d(Constants.DEBUG_TAG, "URL is: " + url);

                URL url2 = new URL(url);
                mConnection = (HttpURLConnection) url2.openConnection();
                mConnection.connect();

                // mSitesObj = new JsonAccountModel();

                InputStream stream = mConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";
                StringBuffer buffer = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();
                Log.d(Constants.DEBUG_TAG, "finalJson is: " + finalJson);


                Gson mGson = new Gson();

                List<String> sitesListArray = new ArrayList<>();
                sitesJson = mGson.fromJson(finalJson, JsonDataModel.class);
                //mHubsObj = mGson.fromJson(finalJson, JsonAccountModel.class);

                List<JsonDataModel.DataBean.SitesBean> sitesBean = sitesJson.getData().getSites();

                for (int i = 0; i < sitesBean.size(); i++) {
                    sitesListArray.add(sitesBean.get(i).getName());
                }

                return sitesBean;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mConnection != null) {
                    mConnection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List result) {
            super.onPostExecute(result);

            // TODO uncomment when figured it out
            /*mSearchAdapter = new SearchViewAdapter(SearchActivity.this, s);
            mSitesLv.setAdapter(mSearchAdapter);*/

            if (result != null) {
                SearchViewAdapter siteAdapter = new SearchViewAdapter(getApplicationContext(), result);
                mSitesLv.setAdapter(siteAdapter);

                /*HubsViewAdapter hubsViewAdapter = new HubsViewAdapter(SearchActivity.this, result);
                mHubsLv.setAdapter(hubsViewAdapter);*/

                /*HubsViewAdapter hubsViewAdapter = new HubsViewAdapter(SearchActivity.this,result);
                mHubsLv.setAdapter(hubsViewAdapter);*/
            } else {
                Toast.makeText(getApplicationContext(), R.string.http_error, Toast.LENGTH_SHORT).show();
            }

        }
    }
}
