package org.kexie.android.ftper.widget;

import android.content.Context;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.DialogMultiInputBinding;

import java.util.Arrays;

import androidx.databinding.DataBindingUtil;

public final class ConfigDialogBuilder
        extends QMUIDialog.AutoResizeDialogBuilder {


    private DialogMultiInputBinding mBinding;

    public ConfigDialogBuilder(Context context) {
        super(context);
    }

    public DialogMultiInputBinding getBinding() {
        return mBinding;
    }

    @Override
    public View onBuildContent(QMUIDialog dialog, ScrollView parent) {
        mBinding = DataBindingUtil.inflate(dialog.getLayoutInflater(),
                R.layout.dialog_multi_input, parent,
                false);
        for (EditText editText : Arrays.asList(
                mBinding.name,
                mBinding.host,
                mBinding.port,
                mBinding.username,
                mBinding.password)) {
            QMUIViewHelper.setBackgroundKeepingPadding(editText,
                    QMUIResHelper.getAttrDrawable(dialog.getContext(),
                            R.attr.qmui_list_item_bg_with_border_bottom));
        }
        mBinding.password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        return mBinding.getRoot();
    }
}
