package org.kexie.android.ftper.view;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qmuiteam.qmui.widget.QMUIEmptyView;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet.BottomGridSheetBuilder;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.EditTextDialogBuilder;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentFilesBinding;
import org.kexie.android.ftper.viewmodel.ClientViewModel;
import org.kexie.android.ftper.viewmodel.ConfigsViewModel;
import org.kexie.android.ftper.viewmodel.bean.FileItem;
import org.kexie.android.ftper.widget.GenericQuickAdapter;
import org.kexie.android.ftper.widget.RxWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;

import static com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener;
import static org.kexie.android.ftper.widget.FastUtils.subscribeToast;

public class FilesFragment extends Fragment {

    private FragmentFilesBinding mBinding;

    private QMUIEmptyView mEmptyView;

    private ClientViewModel mClientViewModel;

    private ConfigsViewModel mConfigsViewModel;

    private GenericQuickAdapter<FileItem> mItemAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemAdapter = new GenericQuickAdapter<>(R.layout.item_file, BR.file);
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

        mClientViewModel = ViewModelProviders.of(this)
                .get(ClientViewModel.class);

        mConfigsViewModel = ViewModelProviders.of(requireActivity())
                .get(ConfigsViewModel.class);

        mConfigsViewModel.getSelect().observe(this, mClientViewModel::connect);

        mBinding.refresh.setOnRefreshListener(() -> mClientViewModel.refresh());
        mClientViewModel.isLoading().observe(this, bool -> {
            if (!bool) {
                mBinding.refresh.setRefreshing(false);
            }
        });

        mEmptyView.setButton(getString(R.string.refresh), RxWrapper.create(View.OnClickListener.class)
                .owner(this)
                .inner(v -> mClientViewModel.refresh())
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
                                                    mClientViewModel.mkdir(text.toString());
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

        mClientViewModel.getCurrentDir().observe(this, mBinding::setPath);

        mItemAdapter.setOnItemClickListener(RxWrapper
                .create(OnItemClickListener.class)
                .owner(this)
                .inner((adapter, view1, position) ->
                {
                    FileItem fileItem = (FileItem) adapter.getItem(position);
                    if (fileItem == null) {
                        return;
                    }
                    if (fileItem.isDirectory()) {
                        mClientViewModel.changeDir(fileItem.getName());
                    } else if (fileItem.isFile()) {
                        openFileBottomSheet(fileItem);
                    }
                })
                .build());
        mItemAdapter.setOnItemLongClickListener((adapter, view12, position) -> {
            FileItem fileItem = (FileItem) adapter.getItem(position);
            if (fileItem == null || getString(R.string.uplayer_dir)
                    .equals(fileItem.getName())) {
                return false;
            }
            openFileBottomSheet(fileItem);
            return true;
        });
        mClientViewModel.getFiles().observe(this, mItemAdapter::setNewData);
    }

    private void openFileBottomSheet(FileItem fileItem) {
        BottomGridSheetBuilder builder
                = new BottomGridSheetBuilder(requireContext())
                .addItem(R.drawable.delete,
                        getString(R.string.delete),
                        R.drawable.delete,
                        BottomGridSheetBuilder.FIRST_LINE);
        if (fileItem.isFile()) {
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
                                        mClientViewModel.delete(fileItem);
                                    })
                            .create(com.qmuiteam.qmui.R.style.QMUI_Dialog)
                            .show();
                }
                break;
                case R.drawable.dl: {
                    mClientViewModel.download(fileItem);
                }
                break;
            }
        }).build().show();
    }

    @Override
    public void onResume() {
        super.onResume();

        subscribeToast(this,
                mClientViewModel.getOnError(),
                Toasty::error);

        subscribeToast(this,
                mClientViewModel.getOnSuccess(),
                Toasty::success);

        subscribeToast(this,
                mClientViewModel.getOnInfo(),
                Toasty::info);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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