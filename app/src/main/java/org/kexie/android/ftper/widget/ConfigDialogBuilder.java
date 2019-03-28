package org.kexie.android.ftper.widget;

import android.content.Context;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ScrollView;

import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.DialogMultiInputBinding;
import org.kexie.android.ftper.viewmodel.bean.Config;

import androidx.databinding.DataBindingUtil;

public final class ConfigDialogBuilder extends QMUIDialog.AutoResizeDialogBuilder
{
    private DialogMultiInputBinding mBinding;

    private Config mConfig;

    public ConfigDialogBuilder(Context context, Config config)
    {
        super(context);
        mConfig = config;
    }

    @Override
    public View onBuildContent(QMUIDialog dialog, ScrollView parent)
    {
        mBinding = DataBindingUtil.inflate(dialog.getLayoutInflater(),
                R.layout.dialog_multi_input, parent,
                false);
        if (mConfig == null)
        {
            mBinding.setConfig(new Config(
                    null,
                    null,
                    null,
                    null));
        }
        QMUIViewHelper.setBackgroundKeepingPadding(mBinding.host,
                QMUIResHelper.getAttrDrawable(dialog.getContext(),
                        R.attr.qmui_list_item_bg_with_border_bottom));
        QMUIViewHelper.setBackgroundKeepingPadding(mBinding.port,
                QMUIResHelper.getAttrDrawable(dialog.getContext(),
                        R.attr.qmui_list_item_bg_with_border_bottom));
        QMUIViewHelper.setBackgroundKeepingPadding(mBinding.username,
                QMUIResHelper.getAttrDrawable(dialog.getContext(),
                        R.attr.qmui_list_item_bg_with_border_bottom));
        QMUIViewHelper.setBackgroundKeepingPadding(mBinding.password,
                QMUIResHelper.getAttrDrawable(dialog.getContext(),
                        R.attr.qmui_list_item_bg_with_border_bottom));
        mBinding.password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        return mBinding.getRoot();
    }

    public DialogMultiInputBinding getBinding()
    {
        return mBinding;
    }
}
