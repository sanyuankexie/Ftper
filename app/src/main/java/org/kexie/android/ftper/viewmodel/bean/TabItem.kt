package org.kexie.android.ftper.viewmodel.bean

import com.flyco.tablayout.listener.CustomTabEntity

class TabItem(private val title: String,
              private val selectedIcon: Int,
              private val unSelectedIcon: Int)
    : CustomTabEntity {
    override fun getTabTitle(): String {
        return title
    }

    override fun getTabSelectedIcon(): Int {
        return selectedIcon
    }

    override fun getTabUnselectedIcon(): Int {
        return unSelectedIcon
    }
}
