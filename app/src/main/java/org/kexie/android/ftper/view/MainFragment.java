package org.kexie.android.ftper.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import es.dmoral.toasty.Toasty;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentMainBinding;
import org.kexie.android.ftper.viewmodel.MainViewModel;
import org.kexie.android.ftper.viewmodel.bean.RemoteFile;
import org.kexie.android.ftper.widget.FastUtils;
import org.kexie.android.ftper.widget.GenericQuickAdapter;

public class MainFragment extends Fragment {


    private MainViewModel mViewModel;

    private FragmentMainBinding mBinding;

    private PagerAdapter mPagerAdapter;

    private GenericQuickAdapter<RemoteFile> mItemAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {

            private Fragment[] fragments = new Fragment[]{
                    new FilesFragment(),
                    new ToolsFragment(),
                    new ConfigFragment()
            };



            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }
        };
        mItemAdapter = new GenericQuickAdapter<>(0, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_main,
                container,
                false);
        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders.of(this)
                .get(MainViewModel.class);
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
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
        mBinding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPagerAdapter = null;
        mItemAdapter = null;
    }
}