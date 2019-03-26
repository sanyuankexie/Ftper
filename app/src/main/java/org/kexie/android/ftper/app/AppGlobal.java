package org.kexie.android.ftper.app;

import androidx.multidex.MultiDexApplication;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import org.kexie.android.ftper.BuildConfig;

public class AppGlobal extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Logger.addLogAdapter(new AndroidLogAdapter());
        }
    }
}
