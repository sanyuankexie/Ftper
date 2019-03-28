package org.kexie.android.ftper.widget;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

//ViewModel拥有数据的控制权
public class GenericQuickAdapter<X>
        extends BaseQuickAdapter<X, GenericQuickAdapter.GenericViewHolder> {

    private final int mName;

    @SuppressWarnings("WeakerAccess")
    public GenericQuickAdapter(int layoutId, int name) {
        super(layoutId);
        openLoadAnimation();
        this.mName = name;
    }

    @Override
    protected void convert(GenericViewHolder helper, X item) {
        helper.mBinding.setVariable(mName, item);
    }

    protected static final class GenericViewHolder
            extends BaseViewHolder {
        private ViewDataBinding mBinding;

        public GenericViewHolder(View view) {
            super(view);
            mBinding = DataBindingUtil.bind(view);
        }
    }
}