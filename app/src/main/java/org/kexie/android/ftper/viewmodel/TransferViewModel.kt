package org.kexie.android.ftper.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.kexie.android.ftper.app.AppGlobal

class TransferViewModel(application: Application)
    : AndroidViewModel(application) {

    private val transferDao = getApplication<AppGlobal>()
            .appDatabase
            .transferDao



}

