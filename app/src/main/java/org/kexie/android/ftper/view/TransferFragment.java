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
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentTransferBinding;
import org.kexie.android.ftper.viewmodel.TransferViewModel;
import org.kexie.android.ftper.viewmodel.bean.TransferItem;
import org.kexie.android.ftper.widget.GenericQuickAdapter;
import org.kexie.android.ftper.widget.TransferItemAdapter;
import org.kexie.android.ftper.widget.Utils;

public class TransferFragment extends Fragment {

    private TransferViewModel mViewModel;

    private FragmentTransferBinding mBinding;

    private GenericQuickAdapter<TransferItem> mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TransferItemAdapter();
    }

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
        mAdapter.setEmptyView(Utils.createEmptyView(inflater.getContext()));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(TransferViewModel.class);
        mBinding.setAdapter(mAdapter);
        mAdapter.setOnItemLongClickListener((adapter, view1, position) -> {

            return true;
        });

        mViewModel.getItem().observe(this, mAdapter::setNewData);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mViewModel != null) {
            mViewModel.setActive(isVisibleToUser);
        }
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