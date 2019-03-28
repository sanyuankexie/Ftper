package org.kexie.android.ftper.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;

import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentConfigsBinding;
import org.kexie.android.ftper.databinding.ViewFooterConfigAddBinding;
import org.kexie.android.ftper.databinding.ViewHeadConfigBinding;
import org.kexie.android.ftper.viewmodel.ConfigsViewModel;
import org.kexie.android.ftper.viewmodel.bean.Config;
import org.kexie.android.ftper.widget.GenericQuickAdapter;
import org.kexie.android.ftper.widget.ConfigDialogBuilder;
import org.kexie.android.ftper.widget.RxWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class ConfigsFragment extends Fragment
{

    private FragmentConfigsBinding mBinding;

    private ViewFooterConfigAddBinding mFooterBinding;

    private ViewHeadConfigBinding mHeadBinding;

    private ConfigsViewModel mViewModel;

    private GenericQuickAdapter<Object> mConfigAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mConfigAdapter = new GenericQuickAdapter<>(0,0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_configs,
                container,
                false);
        mFooterBinding = DataBindingUtil.inflate(inflater,
                R.layout.view_footer_config_add,
                mBinding.configs,
                false);
        mHeadBinding = DataBindingUtil.inflate(inflater,
                R.layout.view_head_config,
                mBinding.configs,
                false);
        mConfigAdapter.addFooterView(mFooterBinding.getRoot());
        mConfigAdapter.addHeaderView(mHeadBinding.getRoot());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders.of(this)
                .get(ConfigsViewModel.class);
        mBinding.setAdapter(mConfigAdapter);

        mFooterBinding.setAddAction(RxWrapper
                .create(View.OnClickListener.class)
                .owner(this)
                .inner(v -> openConfigDialog(null))
                .build());

        mConfigAdapter.setOnItemLongClickListener(RxWrapper
                .create(BaseQuickAdapter.OnItemLongClickListener.class)
                .owner(this)
                .inner((adapter, view1, position) ->
                {
                    Config config = (Config) adapter.getItem(position);
                    if (config != null)
                    {
                        openConfigDialog(config);
                        return true;
                    }
                    return false;
                })
                .build());
    }

    private void openConfigDialog(Config config)
    {
        ConfigDialogBuilder builder
                = new ConfigDialogBuilder(requireContext(), config);
        builder.setTitle("添加新服务器")
                .addAction("保存", (dialog, index) ->
                {

                    dialog.dismiss();
                }).addAction("取消", (dialog, index) -> dialog.dismiss())
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog)
                .show();
        QMUIKeyboardHelper.showKeyboard(builder.getBinding().host, true);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mConfigAdapter.removeHeaderView(mHeadBinding.getRoot());
        mConfigAdapter.removeFooterView(mFooterBinding.getRoot());
        mHeadBinding.unbind();
        mHeadBinding = null;
        mFooterBinding.unbind();
        mFooterBinding = null;
        mBinding.unbind();
        mBinding = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mConfigAdapter = null;
    }
}
