package org.kexie.android.ftper.widget

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

//ViewModel拥有数据的控制权
class GenericQuickAdapter<X>(layoutId: Int,
                             private val mName: Int)
    : BaseQuickAdapter<X, GenericQuickAdapter.GenericViewHolder>(layoutId) {

    init {
        openLoadAnimation()
    }

    override fun convert(helper: GenericViewHolder, item: X) {
        helper.mBinding.setVariable(mName, item)
    }
    class GenericViewHolder(view: View) : BaseViewHolder(view) {
        val mBinding: ViewDataBinding = DataBindingUtil.bind(view)!!
    }
}