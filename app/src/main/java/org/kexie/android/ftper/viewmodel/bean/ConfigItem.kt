package org.kexie.android.ftper.viewmodel.bean


data class ConfigItem(
    var id: Int = 0,
    var name: String? = null,
    var host: String? = null,
    var port: String? = null,
    var username: String? = null,
    var password: String? = null,
    var date: String? = null,
    var isSelect:Boolean = false
):Cloneable {
    override fun clone(): ConfigItem {
        return copy()
    }
}

