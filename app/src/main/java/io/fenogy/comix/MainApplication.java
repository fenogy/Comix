package io.fenogy.comix;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


public class MainApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getAppContext() {
        return mContext;
    }

    public static SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(Constants.SETTINGS_NAME, 0);
    }
}
