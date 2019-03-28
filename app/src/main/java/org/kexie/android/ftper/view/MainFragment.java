package org.kexie.android.ftper.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.tablayout.listener.OnTabSelectListener;

import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.FragmentMainBinding;
import org.kexie.android.ftper.viewmodel.bean.Tab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MainFragment extends Fragment {

    private FragmentMainBinding mBinding;

    private PagerAdapter mPagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {

            private Fragment[] fragments = new Fragment[]{
                    new FilesFragment(),
                    new ConfigsFragment()
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
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mBinding.pages.setAdapter(mPagerAdapter);

        mBinding.pages.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                mBinding.tabs.setCurrentTab(position);
            }
        });

        mBinding.tabs.setOnTabSelectListener(new OnTabSelectListener()
        {
            @Override
            public void onTabSelect(int position)
            {
                mBinding.pages.setCurrentItem(MathUtils.clamp(
                        position,
                        0,
                        mPagerAdapter.getCount()));
            }

            @Override
            public void onTabReselect(int position)
            {

            }
        });

        List<Tab> tabEntities = Arrays.asList(
                new Tab("文件", R.drawable.files_s, R.drawable.files),
                new Tab("配置", R.drawable.config_s, R.drawable.config)
        );

        mBinding.tabs.setTabData(new ArrayList<>(tabEntities));
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
    }
}