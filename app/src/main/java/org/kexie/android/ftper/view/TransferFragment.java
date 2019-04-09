package org.kexie.android.ftper.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import es.dmoral.toasty.Toasty;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentTransferBinding;
import org.kexie.android.ftper.viewmodel.RemoteViewModel;
import org.kexie.android.ftper.viewmodel.TransferViewModel;
import org.kexie.android.ftper.viewmodel.bean.TaskItem;
import org.kexie.android.ftper.widget.GenericQuickAdapter;
import org.kexie.android.ftper.widget.Utils;

public class TransferFragment extends Fragment {

    private TransferViewModel mTransferViewModel;

    private RemoteViewModel mRemoteViewModel;

    private FragmentTransferBinding mBinding;

    private GenericQuickAdapter<TaskItem> mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_transfer,
                container,
                false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTransferViewModel = ViewModelProviders.of(requireActivity())
                .get(TransferViewModel.class);
        mRemoteViewModel = ViewModelProviders.of(requireActivity())
                .get(RemoteViewModel.class);
        mAdapter = mTransferViewModel.getAdapter();
        mAdapter.setOnItemLongClickListener((adapter, view1, position) -> {
            new QMUIBottomSheet.BottomGridSheetBuilder(requireContext())
                    .addItem(R.drawable.delete,
                            getString(R.string.delete),
                            R.drawable.delete,
                            QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                    .addItem(R.drawable.delete,
                            getString(R.string.delete),
                            R.drawable.delete,
                            QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                    .addItem(R.drawable.delete,
                            getString(R.string.delete),
                            R.drawable.delete,
                            QMUIBottomSheet.BottomGridSheetBuilder.FIRST_LINE)
                    .setOnSheetItemClickListener((dialog, itemView) -> {

                    })
                    .build()
                    .show();
            return true;
        });
        mBinding.setAdapter(mAdapter);
        Utils.subscribe(this,
                mRemoteViewModel.getOnNewTask(),
                Lifecycle.Event.ON_DESTROY,
                mTransferViewModel::start);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.subscribeToast(this,
                mTransferViewModel.getOnError(),
                Toasty::error);
        Utils.subscribeToast(this,
                mTransferViewModel.getOnInfo(),
                Toasty::info);
        Utils.subscribeToast(this,
                mTransferViewModel.getOnSuccess(),
                Toasty::success);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
        mBinding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;
    }
}