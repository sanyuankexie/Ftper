package org.kexie.android.ftper.app

import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import es.dmoral.toasty.Toasty
import org.kexie.android.autopermissions.AutoPermissions
import org.kexie.android.ftper.BuildConfig
import org.kexie.android.ftper.model.AppDatabase

class AppGlobal : MultiDexApplication() {

    val appDatabase: AppDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
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
        AutoPermissions.addCallback {
            Toasty.error(this,"").show()
        }
        //这看似不安全的操作，实际上却是安全的use by lazy(LazyThreadSafetyMode.SYNCHRONIZED){ }
        Thread {
            appDatabase
        }.apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }
}
