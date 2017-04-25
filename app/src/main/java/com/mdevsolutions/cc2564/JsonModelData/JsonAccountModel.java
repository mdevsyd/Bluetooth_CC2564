package com.mdevsolutions.cc2564.JsonModelData;

import java.util.List;

/**
 * Created by Michi on 22/04/2017.
 */

public class JsonAccountModel {


    /**
     * account_sid : A3JHBLG1ZBYLK63Q
     * sites : [{"name":"53","hubs":[{"serial":"F000016C","name":null,"instruments":[{"serial":"AML1G204","name":null},{"serial":"WLM1H101","name":null},{"serial":"WLM1H102","name":null},{"serial":"WLM1H103","name":null},{"serial":"F000016C","name":null}]}]}]
     */

    private String account_sid;
    private List<SitesBean> sites;

    public String getAccount_sid() {
        return account_sid;
    }

    public void setAccount_sid(String account_sid) {
        this.account_sid = account_sid;
    }

    public List<SitesBean> getSites() {
        return sites;
    }

    public void setSites(List<SitesBean> sites) {
        this.sites = sites;
    }

    public static class SitesBean {
        /**
         * name : 53
         * hubs : [{"serial":"F000016C","name":null,"instruments":[{"serial":"AML1G204","name":null},{"serial":"WLM1H101","name":null},{"serial":"WLM1H102","name":null},{"serial":"WLM1H103","name":null},{"serial":"F000016C","name":null}]}]
         */

        private String name;
        private List<HubsBean> hubs;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<HubsBean> getHubs() {
            return hubs;
        }

        public void setHubs(List<HubsBean> hubs) {
            this.hubs = hubs;
        }

        public static class HubsBean {
            /**
             * serial : F000016C
             * name : null
             * instruments : [{"serial":"AML1G204","name":null},{"serial":"WLM1H101","name":null},{"serial":"WLM1H102","name":null},{"serial":"WLM1H103","name":null},{"serial":"F000016C","name":null}]
             */

            private String serial;
            private Object name;
            private List<InstrumentsBean> instruments;

            public String getSerial() {
                return serial;
            }

            public void setSerial(String serial) {
                this.serial = serial;
            }

            public Object getName() {
                return name;
            }

            public void setName(Object name) {
                this.name = name;
            }

            public List<InstrumentsBean> getInstruments() {
                return instruments;
            }

            public void setInstruments(List<InstrumentsBean> instruments) {
                this.instruments = instruments;
            }

            public static class InstrumentsBean {
                /**
                 * serial : AML1G204
                 * name : null
                 */

                private String serial;
                private Object name;

                public String getSerial() {
                    return serial;
                }

                public void setSerial(String serial) {
                    this.serial = serial;
                }

                public Object getName() {
                    return name;
                }

                public void setName(Object name) {
                    this.name = name;
                }
            }
        }
    }
}
