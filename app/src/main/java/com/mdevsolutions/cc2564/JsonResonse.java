package com.mdevsolutions.cc2564;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Michi on 14/04/2017.
 */

public class JsonResonse {


    /**
     * success : true
     * message :
     */

    private boolean success;
    private String message;
    private List<DataBean> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        @SerializedName("Weight (g)")
        private String _$WeightG94; // FIXME check this code
        private String DateAndTime;

        public String get_$WeightG94() {
            return _$WeightG94;
        }

        public void set_$WeightG94(String _$WeightG94) {
            this._$WeightG94 = _$WeightG94;
        }

        public String getDateAndTime() {
            return DateAndTime;
        }

        public void setDateAndTime(String DateAndTime) {
            this.DateAndTime = DateAndTime;
        }
    }
}