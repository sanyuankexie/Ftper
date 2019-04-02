package org.kexie.android.ftper.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.util.QMUIKeyboardHelper;

import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentConfigsBinding;
import org.kexie.android.ftper.databinding.ViewFooterConfigAddBinding;
import org.kexie.android.ftper.databinding.ViewHeadConfigBinding;
import org.kexie.android.ftper.viewmodel.ConfigsViewModel;
import org.kexie.android.ftper.viewmodel.bean.ConfigItem;
import org.kexie.android.ftper.widget.ConfigDialogBuilder;
import org.kexie.android.ftper.widget.GenericQuickAdapter;
import org.kexie.android.ftper.widget.RxWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;

import static android.view.View.*;
import static com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener;
import static org.kexie.android.ftper.widget.FastUtils.subscribeToast;

public class ConfigsFragment extends Fragment {

    private FragmentConfigsBinding mBinding;

    private ViewFooterConfigAddBinding mFooterBinding;

    private ViewHeadConfigBinding mHeadBinding;

    private ConfigsViewModel mViewModel;

    private GenericQuickAdapter<ConfigItem> mConfigAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfigAdapter = new GenericQuickAdapter<>(R.layout.item_config, BR.configItem);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders.of(requireActivity())
                .get(ConfigsViewModel.class);

        mViewModel.getConfigs().observe(this, mConfigAdapter::setNewData);

        mBinding.setAdapter(mConfigAdapter);

        mFooterBinding.setAddAction(RxWrapper
                .create(OnClickListener.class)
                .owner(this)
                .inner(v -> openConfigDialog(null))
                .build());

        mConfigAdapter.setOnItemClickListener(RxWrapper
                .create(OnItemClickListener.class)
                .owner(this)
                .inner((adapter, view12, position) ->
                {
                    ConfigItem configItem = (ConfigItem) adapter.getItem(position);
                    if (configItem != null) {
                        int last = -1;
                        for (int i = 0; i < adapter.getData().size(); i++) {
                            ConfigItem item = (ConfigItem) adapter.getItem(i);
                            if (item != null && item.isSelect()) {
                                last = i;
                            }
                        }
                        mViewModel.select(configItem);
                        if (last != -1) {
                            adapter.notifyItemChanged(
                                    adapter.getHeaderLayoutCount() + last);
                        }
                        adapter.notifyItemChanged(
                                adapter.getHeaderLayoutCount() + position);
                    }
                })
                .build());

        mConfigAdapter.setOnItemLongClickListener((adapter, view1, position) ->
        {
            ConfigItem configItem = (ConfigItem) adapter.getItem(position);
            if (configItem != null) {
                openConfigDialog(configItem);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        subscribeToast(this,
                mViewModel.getOnError(),
                Toasty::error);

        subscribeToast(this,
                mViewModel.getOnSuccess(),
                Toasty::success);

        subscribeToast(this,
                mViewModel.getOnInfo(),
                Toasty::info);
    }

    private void openConfigDialog(ConfigItem configItem)
    {
        boolean isAdd = configItem == null;
        ConfigDialogBuilder builder = new ConfigDialogBuilder(requireContext());
        builder.addAction("保存", (dialog, index) ->
        {
            ConfigItem configItem2 = builder
                    .getBinding()
                    .getConfigItem();
            if (isAdd)
            {
                mViewModel.add(configItem2);
            } else
            {
                mViewModel.update(configItem2);
            }
            dialog.dismiss();
        });
        if (!isAdd)
        {
            builder.addAction("删除",
                    (dialog, index) -> {
                        ConfigItem configItem2 = builder
                                .getBinding()
                                .getConfigItem();
                        mViewModel.remove(configItem2);
                        dialog.dismiss();
                    }).setTitle("修改服务器信息");
        } else
        {
            builder.setTitle("添加服务器");
        }
        builder.addAction("取消", (dialog, index) -> dialog.dismiss())
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog)
                .show();
        QMUIKeyboardHelper.showKeyboard(builder.getBinding().host, true);
        builder.getBinding().setConfigItem(isAdd ? new ConfigItem() : configItem);
    }

    @Override
    public void onDestroyView() {
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
    public void onDestroy() {
        super.onDestroy();
        mConfigAdapter = null;
    }
}
