package com.mdevsolutions.cc2564.Activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mdevsolutions.cc2564.Adapters.SearchViewAdapter;
import com.mdevsolutions.cc2564.JsonModelData.JsonAccountModel;
import com.mdevsolutions.cc2564.JsonModelData.JsonDataModel;
import com.mdevsolutions.cc2564.R;
import com.mdevsolutions.cc2564.Utilities.Constants;
import com.mdevsolutions.cc2564.Utilities.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    HttpURLConnection mConnection;
    BufferedReader reader;
    private ListView mSitesLv;
    private ListView mHubsLv;
    private ListView mInstrumentsLv;
    private SearchViewAdapter mSearchAdapter;
    private JsonDataModel mSitesObj;
    private JsonAccountModel mHubsObj;
    private Gson mGson;
    private HttpUtils mHttpUtil;

    private JsonDataModel testJson;


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

        testJson = new JsonDataModel();


        // mJsonResponseObj = mGson.fromJson(response, JsonResponse.class);
        // List<JsonResponse.DataBean> jsonBean = mJsonResponseObj.getData();

        new JsonAsyncTask().execute();
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
                Log.d(Constants.DEBUG_TAG,"URL is: "+url);

                URL url2 = new URL(url);
                mConnection = (HttpURLConnection) url2.openConnection();
                mConnection.connect();

               // mSitesObj = new JsonAccountModel();

                InputStream stream = mConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";
                StringBuffer buffer = new StringBuffer();

                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJson = buffer.toString();
                Log.d(Constants.DEBUG_TAG, "finalJson is: "+finalJson);



               /* JSONObject parentObject = new JSONObject(finalJson);

                JSONArray parentArray = parentObject.getJSONArray("sites");

                List<JsonDataModel> accountList = new ArrayList<>();
                mGson = new Gson();

                for (int i = 0; i<parentArray.length(); i++){

                    JSONObject finalObject = parentArray.getJSONObject(i);

                    JsonDataModel accoutnModel = mGson.fromJson(finalObject.toString(), JsonDataModel.class);

                    accountList.add(accoutnModel);

                }
*/

                /*JSONArray parentArray = parentObject.getJSONArray("hubs");

                List<JsonAccountModel> accountList = new ArrayList<>();

                mGson = new Gson();

                for (int i = 0; i<parentArray.length(); i++){

                    JSONObject finalObject = parentArray.getJSONObject(i);

                    JsonAccountModel accoutnModel = mGson.fromJson(finalObject.toString(), JsonAccountModel.class);

                    accountList.add(accoutnModel);

                }
*/
                Gson mGson = new Gson();

                List<String> sitesListArray = new ArrayList<>();
                testJson = mGson.fromJson(finalJson, JsonDataModel.class);
                //mHubsObj = mGson.fromJson(finalJson, JsonAccountModel.class);

                List<JsonDataModel.DataBean.SitesBean> sitesBean = testJson.getData().getSites();

                for (int i =0 ; i<sitesBean.size(); i++){
                    sitesListArray.add(sitesBean.get(i).getName());
                }

                //List<JsonAccountModel.SitesBean> hubsBean = mHubsObj.getSites().get(0).g;

                /*List<String> sitesList = new ArrayList<>();
                sitesList.add(sitesBean);
                Log.d(Constants.DEBUG_TAG, "Siteslist : "+ sitesList + "SitesBean (getName()) is: " +sitesBean);*/

                return sitesBean;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        protected void onPostExecute(List result) {
            super.onPostExecute(result);

            // TODO uncomment when figured it out
            /*mSearchAdapter = new SearchViewAdapter(SearchActivity.this, s);
            mSitesLv.setAdapter(mSearchAdapter);*/

            if (result != null){
                SearchViewAdapter siteAdapter = new SearchViewAdapter(SearchActivity.this, result);
                mSitesLv.setAdapter(siteAdapter);

                /*HubsViewAdapter hubsViewAdapter = new HubsViewAdapter(SearchActivity.this, result);
                mHubsLv.setAdapter(hubsViewAdapter);*/

                /*HubsViewAdapter hubsViewAdapter = new HubsViewAdapter(SearchActivity.this,result);
                mHubsLv.setAdapter(hubsViewAdapter);*/
            }
            else{
                Toast.makeText(getApplicationContext(), R.string.http_error, Toast.LENGTH_SHORT).show();
            }

        }
    }
}
