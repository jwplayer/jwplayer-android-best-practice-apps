package com.jwplayer.demo.cnxadserverbpa;

import android.app.Application;

import com.jwplayer.pub.api.license.LicenseUtil;

public class CnxApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // The license key comes from app/cnx.properties, like every other publisher value in this app.
        new LicenseUtil().setLicenseKey(this, BuildConfig.LICENSE_KEY);
    }
}
