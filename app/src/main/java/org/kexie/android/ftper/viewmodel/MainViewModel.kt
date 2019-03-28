package org.kexie.android.ftper.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.kexie.android.ftper.viewmodel.bean.ConfigItem

class MainViewModel(application: Application)
    : AndroidViewModel(application) {

    val current = MutableLiveData<ConfigItem>();

}
