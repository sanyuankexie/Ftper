package org.kexie.android.ftper.app

import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.kexie.android.ftper.BuildConfig
import org.kexie.android.ftper.model.AppDatabase

class AppGlobal : MultiDexApplication() {

    val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
                this,
                AppDatabase::class.java,
                packageName)
                .fallbackToDestructiveMigration()
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Logger.addLogAdapter(AndroidLogAdapter())
        }
        Thread {
            appDatabase
        }.start()
    }
}
