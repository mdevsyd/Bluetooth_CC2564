package com.mdevsolutions.cc2564.JsonModelData;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Michi on 22/04/2017.
 */

public class AMLDashboardModel {


    /**
     * success : true
     * message :
     * data : [{"DateAndTime":"2017-04-11 09:07:07","Battery Voltage (V)":"4.200","Temperature_1 (deg.C)":"20.500","External Supply Voltage (V)":"21.700"}]
     */

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * DateAndTime : 2017-04-11 09:07:07
         * Battery Voltage (V) : 4.200
         * Temperature_1 (deg.C) : 20.500
         * External Supply Voltage (V) : 21.700
         */

        private String DateAndTime;
        @SerializedName("Battery Voltage (V)")
        private String _$BatteryVoltageV54; // FIXME check this code
        @SerializedName("Temperature_1 (deg.C)")
        private String _$Temperature_1DegC196; // FIXME check this code
        @SerializedName("External Supply Voltage (V)")
        private String _$ExternalSupplyVoltageV207; // FIXME check this code

        public String getDateAndTime() {
            return DateAndTime;
        }

        public void setDateAndTime(String DateAndTime) {
            this.DateAndTime = DateAndTime;
        }

        public String get_$BatteryVoltageV54() {
            return _$BatteryVoltageV54;
        }

        public void set_$BatteryVoltageV54(String _$BatteryVoltageV54) {
            this._$BatteryVoltageV54 = _$BatteryVoltageV54;
        }

        public String get_$Temperature_1DegC196() {
            return _$Temperature_1DegC196;
        }

        public void set_$Temperature_1DegC196(String _$Temperature_1DegC196) {
            this._$Temperature_1DegC196 = _$Temperature_1DegC196;
        }

        public String get_$ExternalSupplyVoltageV207() {
            return _$ExternalSupplyVoltageV207;
        }

        public void set_$ExternalSupplyVoltageV207(String _$ExternalSupplyVoltageV207) {
            this._$ExternalSupplyVoltageV207 = _$ExternalSupplyVoltageV207;
        }
    }
}
