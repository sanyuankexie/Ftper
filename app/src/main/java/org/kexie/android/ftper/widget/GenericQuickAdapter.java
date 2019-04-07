package org.kexie.android.ftper.widget;

import androidx.databinding.ViewDataBinding;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

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

        public GenericViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @SuppressWarnings("unchecked")
        public <T extends ViewDataBinding> T getBinding() {
            return (T) mBinding;
        }
    }
}
