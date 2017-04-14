package com.mdevsolutions.cc2564;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Michi on 14/04/2017.
 */

public class DataViewerAdapter extends BaseAdapter{

    private List<JsonResonse.DataBean> mDataItem;
    private Context mContext;
    private LayoutInflater mInflator;

    // Constructor
    public DataViewerAdapter(Context mContext, List<JsonResonse.DataBean> mDataItem) {
        this.mContext = mContext;
        this.mDataItem = mDataItem;
    }

    @Override
    public int getCount() {
        return mDataItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.data_row, parent,false);

        // Create a local reference of the JSON response
        JsonResonse.DataBean item = (JsonResonse.DataBean) getItem(position);

        // Obtain reference to the two text fields for data
        TextView dateAndTime = (TextView) rowView.findViewById(R.id.dateTime);
        TextView weight = (TextView) rowView.findViewById(R.id.weight);

        // Set the data to each textview
        dateAndTime.setText(item.getDateAndTime());
        weight.setText(item.get_$WeightG94());

        return rowView;
    }
}
