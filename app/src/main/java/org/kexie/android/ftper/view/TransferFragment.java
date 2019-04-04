package org.kexie.android.ftper.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentTransferBinding;
import org.kexie.android.ftper.widget.GenericQuickAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class TransferFragment extends Fragment {

    private FragmentTransferBinding mBinding;

    private GenericQuickAdapter<Object> mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new GenericQuickAdapter<>(0, 0);
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
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setAdapter(mAdapter);
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