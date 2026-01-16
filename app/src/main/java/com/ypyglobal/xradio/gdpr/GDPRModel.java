package com.ypyglobal.xradio.gdpr;

class GDPRModel {

    private final String appId;
    private final String testId;

    GDPRModel(String publisherId,String testId) {
        this.appId = publisherId;
        this.testId = testId;
    }

    String getAppId() {
        return appId;
    }

    String getTestId() {
        return testId;
    }

}
