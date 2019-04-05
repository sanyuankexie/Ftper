package org.kexie.android.ftper.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.orhanobut.logger.Logger;

import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentMainBinding;
import org.kexie.android.ftper.viewmodel.ConfigViewModel;
import org.kexie.android.ftper.viewmodel.RemoteViewModel;
import org.kexie.android.ftper.viewmodel.TransferViewModel;
import org.kexie.android.ftper.viewmodel.bean.TabItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;

import static androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener;

public class MainFragment extends Fragment {

    private FragmentMainBinding mBinding;

    private PagerAdapter mPagerAdapter;

    private ConfigFragment mConfigFragment = new ConfigFragment();

    private RemoteFragment mRemoteFragment = new RemoteFragment();

    private TransferFragment mTransferFragment = new TransferFragment();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {

            private Fragment[] fragments = new Fragment[]{
                    mConfigFragment,
                    mRemoteFragment,
                    mTransferFragment,
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
        ViewModelProviders.of(requireActivity()).get(TransferViewModel.class);
        ViewModelProviders.of(requireActivity()).get(ConfigViewModel.class);
        ViewModelProviders.of(requireActivity()).get(RemoteViewModel.class)
                .getFiles()
                .observe(this, item -> {
                    if (!item.isEmpty()) {
                        mBinding.pages.setCurrentItem(1);
                    }
                });

        mBinding.pages.setAdapter(mPagerAdapter);
        mBinding.pages.setOffscreenPageLimit(3);
        mBinding.pages.addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mBinding.tabs.setCurrentTab(position);
            }
        });

        mBinding.tabs.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mBinding.pages.setCurrentItem(MathUtils.clamp(
                        position,
                        0,
                        mPagerAdapter.getCount()));
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        ArrayList<CustomTabEntity> list = new ArrayList<>();
        list.add(new TabItem(getString(R.string.config), R.drawable.config_s, R.drawable.config));
        list.add(new TabItem(getString(R.string.files), R.drawable.files_s, R.drawable.files));
        list.add(new TabItem(getString(R.string.tf), R.drawable.tf_s, R.drawable.tf));
        mBinding.tabs.setTabData(list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
        mBinding = null;
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(data);
        if (requestCode == R.id.open_select_request_code) {
            mRemoteFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPagerAdapter = null;
    }
}