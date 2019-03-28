package org.kexie.android.ftper.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import es.dmoral.toasty.Toasty;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentConfigsBinding;
import org.kexie.android.ftper.databinding.ViewFooterConfigAddBinding;
import org.kexie.android.ftper.databinding.ViewHeadConfigBinding;
import org.kexie.android.ftper.viewmodel.ConfigsViewModel;
import org.kexie.android.ftper.viewmodel.bean.Config;
import org.kexie.android.ftper.widget.ConfigDialogBuilder;
import org.kexie.android.ftper.widget.FastUtils;
import org.kexie.android.ftper.widget.GenericQuickAdapter;
import org.kexie.android.ftper.widget.RxWrapper;

public class ConfigsFragment extends Fragment {

    private FragmentConfigsBinding mBinding;

    private ViewFooterConfigAddBinding mFooterBinding;

    private ViewHeadConfigBinding mHeadBinding;

    private ConfigsViewModel mViewModel;

    private GenericQuickAdapter<Config> mConfigAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfigAdapter = new GenericQuickAdapter<>(R.layout.item_config, BR.config);
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
        mViewModel = ViewModelProviders.of(this)
                .get(ConfigsViewModel.class);

        mViewModel.getConfigs().observe(this, mConfigAdapter::setNewData);

        mBinding.setAdapter(mConfigAdapter);

        mFooterBinding.setAddAction(RxWrapper
                .create(View.OnClickListener.class)
                .owner(this)
                .inner(v -> openConfigDialog(null))
                .build());

        mConfigAdapter.setOnItemClickListener(RxWrapper
                .create(BaseQuickAdapter.OnItemClickListener.class)
                .owner(this)
                .inner((adapter, view12, position) ->
                {
                })
                .build());
        mConfigAdapter.setOnItemLongClickListener((adapter, view1, position) ->
        {
            Config config = (Config) adapter.getItem(position);
            if (config != null) {
                openConfigDialog(config);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        FastUtils.subscribeToast(this,
                mViewModel.getOnError(),
                Toasty::error);

        FastUtils.subscribeToast(this,
                mViewModel.getOnSuccess(),
                Toasty::success);

        FastUtils.subscribeToast(this,
                mViewModel.getOnInfo(),
                Toasty::info);
    }

    private void openConfigDialog(Config config) {
        boolean isAdd = config == null;
        ConfigDialogBuilder builder
                = new ConfigDialogBuilder(requireContext());
        builder.setTitle("添加新服务器")
                .addAction("保存", (dialog, index) ->
                {
                    Config config2 = builder
                            .getBinding()
                            .getConfig();
                    if (isAdd) {
                        mViewModel.add(config2);
                    } else {
                        mViewModel.update(config2);
                    }
                    dialog.dismiss();
                }).addAction("取消", (dialog, index) -> dialog.dismiss())
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog)
                .show();
        QMUIKeyboardHelper.showKeyboard(builder.getBinding().host, true);
        if (isAdd) {
            builder.getBinding().setConfig(new Config());
        }
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
