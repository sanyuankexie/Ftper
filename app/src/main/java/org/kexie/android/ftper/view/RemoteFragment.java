package org.kexie.android.ftper.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.qmuiteam.qmui.widget.QMUIEmptyView;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet.BottomGridSheetBuilder;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.EditTextDialogBuilder;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import es.dmoral.toasty.Toasty;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentFilesBinding;
import org.kexie.android.ftper.viewmodel.ConfigViewModel;
import org.kexie.android.ftper.viewmodel.RemoteViewModel;
import org.kexie.android.ftper.viewmodel.bean.RemoteItem;
import org.kexie.android.ftper.widget.GenericQuickAdapter;
import org.kexie.android.ftper.widget.RxWrapper;
import org.kexie.android.ftper.widget.Utils;

import java.io.File;
import java.util.Objects;

import static com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener;

public class RemoteFragment extends Fragment {

    private FragmentFilesBinding mBinding;

    private QMUIEmptyView mEmptyView;

    private RemoteViewModel mRemoteViewModel;

    private GenericQuickAdapter<RemoteItem> mItemAdapter;

    private QMUITipDialog dialog = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemAdapter = new GenericQuickAdapter<>(R.layout.item_remote_file, BR.file);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_files,
                container,
                false);
        mEmptyView = new QMUIEmptyView(inflater.getContext());
        mEmptyView.setTitleText(getString(R.string.this_is_empty));
        mItemAdapter.setEmptyView(mEmptyView);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRemoteViewModel = ViewModelProviders.of(requireActivity())
                .get(RemoteViewModel.class);

        ConfigViewModel configViewModel = ViewModelProviders.of(requireActivity())
                .get(ConfigViewModel.class);

        configViewModel.getSelect().observe(this, mRemoteViewModel::connect);

        mBinding.refresh.setOnRefreshListener(() -> mRemoteViewModel.refresh());
        mRemoteViewModel.isLoading().observe(this, isLoading -> {
            if (!isLoading) {
                mBinding.refresh.setRefreshing(false);
            }
            if (isLoading && dialog == null) {
                dialog = new QMUITipDialog
                        .Builder(requireContext())
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord(getString(R.string.loading))
                        .create();
                dialog.setCancelable(false);
                dialog.show();
            } else {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        });

        mEmptyView.setButton(getString(R.string.refresh), RxWrapper.create(View.OnClickListener.class)
                .owner(this)
                .inner(v -> mRemoteViewModel.refresh())
                .build());

        mBinding.setOptions(RxWrapper.create(View.OnClickListener.class)
                .owner(this)
                .inner(v -> new BottomGridSheetBuilder(requireContext())
                        .addItem(R.drawable.upload,
                                getString(R.string.upload_current_dir),
                                R.drawable.upload,
                                BottomGridSheetBuilder.FIRST_LINE)
                        .addItem(R.drawable.new_dir,
                                getString(R.string.new_dir),
                                R.drawable.new_dir,
                                BottomGridSheetBuilder.FIRST_LINE)
                        .setOnSheetItemClickListener((dialog, itemView) -> {
                            dialog.dismiss();
                            int tag = (int) itemView.getTag();
                            switch (tag) {
                                case R.drawable.upload: {
                                    Utils.startFragmentForResult(requireParentFragment(),
                                            SelectorFragment.class,
                                            Bundle.EMPTY,
                                            R.id.open_select_request_code);
                                }
                                break;
                                case R.drawable.new_dir: {
                                    EditTextDialogBuilder builder
                                            = new EditTextDialogBuilder(requireContext());
                                    builder.setTitle(R.string.new_dir)
                                            .setPlaceholder(R.string.input_dir_name)
                                            .setInputType(InputType.TYPE_CLASS_TEXT)
                                            .addAction(R.string.submit,
                                                    (dialog1, index) -> dialog1.dismiss())
                                            .addAction(R.string.cancel, (dialog12, index) -> {
                                                @SuppressWarnings("deprecation")
                                                CharSequence text = builder.getEditText().getText();
                                                if (!TextUtils.isEmpty(text)) {
                                                    mRemoteViewModel.mkdir(text.toString());
                                                    dialog12.dismiss();
                                                } else {
                                                    Toasty.warning(requireContext(),
                                                            getString(R.string.dir_no_empty))
                                                            .show();
                                                }
                                            })
                                            .create(com.qmuiteam.qmui.R.style.QMUI_Dialog)
                                            .show();
                                }
                                break;
                            }
                        })
                        .build().show())
                .build());

        mBinding.setAdpater(mItemAdapter);

        mRemoteViewModel.getCurrentDir().observe(this, mBinding::setPath);

        mItemAdapter.setOnItemClickListener(RxWrapper
                .create(OnItemClickListener.class)
                .owner(this)
                .inner((adapter, view1, position) ->
                {
                    RemoteItem remoteItem = (RemoteItem) adapter.getItem(position);
                    if (remoteItem == null) {
                        return;
                    }
                    if (remoteItem.isDirectory()) {
                        mRemoteViewModel.changeDir(remoteItem.getName());
                    } else if (remoteItem.isFile()) {
                        openFileBottomSheet(remoteItem);
                    }
                })
                .build());
        mItemAdapter.setOnItemLongClickListener((adapter, view12, position) -> {
            RemoteItem remoteItem = (RemoteItem) adapter.getItem(position);
            if (remoteItem == null || getString(R.string.uplayer_dir)
                    .equals(remoteItem.getName())) {
                return false;
            }
            openFileBottomSheet(remoteItem);
            return true;
        });
        mRemoteViewModel.getFiles().observe(this, mItemAdapter::setNewData);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == R.id.open_select_request_code
                && resultCode == Activity.RESULT_OK
                && data != null) {
            File file = (File) data.getSerializableExtra(getString(R.string.file));
            if (file != null) {
                mRemoteViewModel.upload(file);
                QMUITipDialog dialog = new QMUITipDialog
                        .Builder(requireContext())
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                        .setTipWord(getString(R.string.start_upload))
                        .create();
                dialog.setCancelable(false);
                dialog.show();
                Objects.requireNonNull(dialog.getWindow()).getDecorView()
                        .postDelayed(dialog::dismiss, 1000);
            }
        }
    }

    private void openFileBottomSheet(RemoteItem remoteItem) {
        BottomGridSheetBuilder builder
                = new BottomGridSheetBuilder(requireContext())
                .addItem(R.drawable.delete,
                        getString(R.string.delete),
                        R.drawable.delete,
                        BottomGridSheetBuilder.FIRST_LINE);
        if (remoteItem.isFile()) {
            builder.addItem(R.drawable.dl,
                    getString(R.string.download),
                    R.drawable.dl,
                    BottomGridSheetBuilder.FIRST_LINE);
        }
        builder.setOnSheetItemClickListener((dialog, itemView) ->
        {
            dialog.dismiss();
            int tag = (int) itemView.getTag();
            switch (tag) {
                case R.drawable.delete: {
                    new QMUIDialog.MessageDialogBuilder(requireContext())
                            .setTitle(R.string.tip)
                            .setMessage(R.string.submit_delete)
                            .addAction(getString(R.string.cancel),
                                    (dialog1, index) -> dialog1.dismiss())
                            .addAction(0, getString(R.string.delete),
                                    QMUIDialogAction.ACTION_PROP_NEGATIVE,
                                    (dialog12, index) ->
                                    {
                                        dialog12.dismiss();
                                        mRemoteViewModel.delete(remoteItem);
                                    })
                            .create(com.qmuiteam.qmui.R.style.QMUI_Dialog)
                            .show();
                }
                break;
                case R.drawable.dl: {
                    mRemoteViewModel.download(remoteItem);
                }
                break;
            }
        }).build().show();
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.subscribeDialog(this,
                mRemoteViewModel.getOnError(),
                QMUITipDialog.Builder.ICON_TYPE_FAIL);

        Utils.subscribeDialog(this,
                mRemoteViewModel.getOnSuccess(),
                QMUITipDialog.Builder.ICON_TYPE_SUCCESS);

        Utils.subscribeDialog(this,
                mRemoteViewModel.getOnInfo(),
                QMUITipDialog.Builder.ICON_TYPE_INFO);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        mEmptyView = null;
        mBinding.unbind();
        mBinding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItemAdapter = null;
    }
}