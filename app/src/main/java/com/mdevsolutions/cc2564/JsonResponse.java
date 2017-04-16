package com.mdevsolutions.cc2564;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Michi on 14/04/2017.
 */

public class JsonResponse {


    /**
     * success : true
     * message :
     * data : [{"Weight (g)":"28011.400","DateAndTime":"2016-09-12 15:41:15"},{"Weight (g)":"28022.400","DateAndTime":"2016-09-12 15:45:00"},{"Weight (g)":"28012.800","DateAndTime":"2016-09-12 16:00:16"},{"Weight (g)":"28029.800","DateAndTime":"2016-09-12 16:15:15"},{"Weight (g)":"28036.400","DateAndTime":"2016-09-12 16:30:15"},{"Weight (g)":"28028.699","DateAndTime":"2016-09-12 16:45:15"},{"Weight (g)":"28034.199","DateAndTime":"2016-09-12 17:00:15"},{"Weight (g)":"28025.199","DateAndTime":"2016-09-12 17:15:15"},{"Weight (g)":"28037.500","DateAndTime":"2016-09-12 17:30:15"},{"Weight (g)":"28036.800","DateAndTime":"2016-09-12 17:45:16"},{"Weight (g)":"28034.400","DateAndTime":"2016-09-12 18:00:15"},{"Weight (g)":"28031.699","DateAndTime":"2016-09-12 18:15:15"}]
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
