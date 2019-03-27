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
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import es.dmoral.toasty.Toasty;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentFilesBinding;
import org.kexie.android.ftper.viewmodel.RemoteViewModel;
import org.kexie.android.ftper.viewmodel.bean.FileItem;
import org.kexie.android.ftper.widget.FastUtils;
import org.kexie.android.ftper.widget.GenericQuickAdapter;

public class FilesFragment extends Fragment
{

    private FragmentFilesBinding mBinding;

    private RemoteViewModel mViewModel;

    private GenericQuickAdapter<FileItem> mItemAdapter;

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
                             @Nullable Bundle savedInstanceState)
    {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_files,
                container,
                false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = ViewModelProviders.of(this)
                .get(RemoteViewModel.class);

        mBinding.setAdpater(mItemAdapter);

        mItemAdapter.setOnItemClickListener(FastUtils.toRxListener(this,
                (GenericQuickAdapter.OnItemClickListener<FileItem>)
                        (adapter, view1, position) ->
                        {
                            FileItem fileItem = adapter.getItem(position);
                            if (fileItem == null) {
                                return;
                            }
                            if (fileItem.isDirectory()) {
                                mViewModel.changeDir(fileItem.getName());
                            } else if (fileItem.isFile()) {
                                openFileBottomSheet(fileItem);
                            }
                        }));

        mViewModel.getFiles().observe(this, mItemAdapter::setNewData);
    }

    private void openFileBottomSheet(FileItem fileItem) {
        new QMUIBottomSheet.BottomGridSheetBuilder(requireContext())
                .addItem(R.drawable.delete,
                        "删除",
                        R.drawable.delete,
                        QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .addItem(R.drawable.download,
                        "下载",
                        R.drawable.download,
                        QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                .setOnSheetItemClickListener((dialog, itemView) -> {
                    dialog.dismiss();
                    int tag = (int) itemView.getTag();
                    switch (tag) {
                        case R.drawable.delete: {
                            mViewModel.delete(fileItem);
                        }
                        break;
                        case R.drawable.download: {
                            mViewModel.download(fileItem);
                        }
                        break;
                    }
                }).build().show();
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
