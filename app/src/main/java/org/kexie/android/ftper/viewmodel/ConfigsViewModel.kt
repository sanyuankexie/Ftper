package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ConfigsViewModel(application: Application)
    : AndroidViewModel(application) {

    companion object {
        private const val sPortKey = "port"
        private const val sHostKey = "host"
        private const val sUsernameKey = "username"
        private const val sPasswordKey = "password"
    }

    /**
     * 用[SharedPreferences]来保存用户基本数据
     */
    private val mSharedPreference = PreferenceManager
            .getDefaultSharedPreferences(application)

    val host = MutableLiveData<String>()
            .loadPreferences(sHostKey)

    val port = MutableLiveData<String>()
            .loadPreferences(sPortKey)

    val username = MutableLiveData<String>()
            .loadPreferences(sUsernameKey)

    val password = MutableLiveData<String>()
            .loadPreferences(sPasswordKey)

    private fun MutableLiveData<String>.loadPreferences(key: String)
            : MutableLiveData<String> {
        val value = mSharedPreference.getString(key, null);
        if (!value.isNullOrEmpty()) {
            this.value = value
        }
        return this
    }

}