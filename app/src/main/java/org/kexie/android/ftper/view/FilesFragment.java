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
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import es.dmoral.toasty.Toasty;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentFilesBinding;
import org.kexie.android.ftper.viewmodel.ClientViewModel;
import org.kexie.android.ftper.viewmodel.MainViewModel;
import org.kexie.android.ftper.viewmodel.bean.FileItem;
import org.kexie.android.ftper.widget.GenericQuickAdapter;
import org.kexie.android.ftper.widget.RxWrapper;

import static org.kexie.android.ftper.widget.FastUtils.subscribeToast;

public class FilesFragment extends Fragment
{

    private FragmentFilesBinding mBinding;

    private ClientViewModel mViewModel;

    private GenericQuickAdapter<FileItem> mItemAdapter;

    private MainViewModel mMainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
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
        mItemAdapter.setEmptyView(inflater.inflate(
                R.layout.view_empty,
                mBinding.files,
                false));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = ViewModelProviders.of(this)
                .get(ClientViewModel.class);
        mMainViewModel = ViewModelProviders.of(requireParentFragment())
                .get(MainViewModel.class);

        mMainViewModel.getCurrent().observe(this, mViewModel::connect);

        mBinding.refresh.setOnRefreshListener(() ->
        {

        });
        mBinding.setAdpater(mItemAdapter);

        mItemAdapter.setOnItemClickListener(RxWrapper
                .create(BaseQuickAdapter.OnItemClickListener.class)
                .owner(this)
                .inner((adapter, view1, position) ->
                {
                    FileItem fileItem = (FileItem) adapter.getItem(position);
                    if (fileItem == null) {
                        return;
                    }
                    if (fileItem.isDirectory()) {
                        mViewModel.changeDir(fileItem.getName());
                    } else if (fileItem.isFile()) {
                        openFileBottomSheet(fileItem);
                    }
                })
                .build());


        mViewModel.getFiles().observe(this, mItemAdapter::setNewData);
    }

    private void openFileBottomSheet(FileItem fileItem)
    {
        new QMUIBottomSheet.BottomGridSheetBuilder(requireContext())
                .addItem(R.drawable.delete,
                        "删除",
                        R.drawable.delete,
                        QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.drawable.download,
                        "下载",
                        R.drawable.download,
                        QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .setOnSheetItemClickListener((dialog, itemView) ->
                {
                    dialog.dismiss();
                    int tag = (int) itemView.getTag();
                    switch (tag)
                    {
                        case R.drawable.delete:
                        {
                            new QMUIDialog.MessageDialogBuilder(requireContext())
                                    .setTitle("提示")
                                    .setMessage("确定要删除吗？")
                                    .addAction("取消",
                                            (dialog1, index) -> dialog1.dismiss())
                                    .addAction(0, "删除",
                                            QMUIDialogAction.ACTION_PROP_NEGATIVE,
                                            (dialog12, index) ->
                                            {
                                                dialog12.dismiss();
                                                mViewModel.delete(fileItem);
                                            })
                                    .create(com.qmuiteam.qmui.R.style.QMUI_Dialog)
                                    .show();
                        }
                        break;
                        case R.drawable.download:
                        {
                            mViewModel.download(fileItem);
                        }
                        break;
                    }
                })
                .build()
                .show();
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

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mBinding.unbind();
        mBinding = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mItemAdapter = null;
    }
}