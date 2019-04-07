package org.kexie.android.ftper.widget;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

@SuppressWarnings("WeakerAccess")
public class GenericQuickAdapter<X>
        extends BaseQuickAdapter<X, GenericQuickAdapter.GenericViewHolder> {

    protected final int mName;

    public GenericQuickAdapter(int layoutResId, int name) {
        super(layoutResId);
        mName = name;
        openLoadAnimation();
    }

    @Override
    protected void convert(GenericViewHolder helper, X item) {
        helper.getBinding().setVariable(mName, item);
    }

    public static class GenericViewHolder extends BaseViewHolder {
        private ViewDataBinding mBinding;
        public GenericViewHolder(View view) {
            super(view);
            mBinding = DataBindingUtil.bind(view);
        }

        @SuppressWarnings("unchecked")
        public <T extends ViewDataBinding> T getBinding() {
            return (T) mBinding;
        }
    }
}
