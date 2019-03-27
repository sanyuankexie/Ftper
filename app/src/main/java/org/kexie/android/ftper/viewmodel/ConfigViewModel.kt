package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ConfigViewModel(application: Application)
    : AndroidViewModel(application) {

    companion object {
        private const val sPortKey = "port"
        private const val sHostKey = "host"
        private const val sUsernameKey = "username"
        private const val sPasswordKey = "password"
    }

    val host = MutableLiveData<String>()

    val port = MutableLiveData<String>()

    val username = MutableLiveData<String>()

    val password = MutableLiveData<String>()

    /**
     * 用[SharedPreferences]来保存用户基本数据
     */
    private val mSharedPreference = PreferenceManager
        .getDefaultSharedPreferences(application)

}