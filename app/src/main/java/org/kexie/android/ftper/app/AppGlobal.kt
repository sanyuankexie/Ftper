package org.kexie.android.ftper.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
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
            if (!it.isEmpty()) {
                jumpToSystemSetting()
                Toasty.error(
                    this,
                    "请授予权限"
                )
                    .show()
                Handler(Looper.getMainLooper())
                    .postDelayed({
                        System.exit(1)
                    }, 1000)
            }
        }
        // 这看似不安全的操作，实际上却是安全的
        // use by lazy(LazyThreadSafetyMode.SYNCHRONIZED){ }
        Thread {
            Logger.d("database init : " + appDatabase.openHelper.databaseName)
        }.apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun jumpToSystemSetting() {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package",
                    this.packageName, null)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings",
                    "com.android.settings.InstalledAppDetails")
            intent.putExtra("com.android.settings.ApplicationPkgName",
                    this.packageName)
        }
        this.startActivity(intent)
    }
}
