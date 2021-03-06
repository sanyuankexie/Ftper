package org.kexie.android.ftper.widget;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.model.FileType;
import org.kexie.android.ftper.viewmodel.bean.FileItem;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class FilePagerAdapter extends PagerAdapter {

    public FilePagerAdapter(LifecycleOwner lifecycleOwner,
                            BaseQuickAdapter.OnItemClickListener listener) {
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        this.mListener = RxWrapper
                .create(BaseQuickAdapter.OnItemClickListener.class)
                .lifecycle(lifecycle)
                .inner(listener)
                .build();
    }

    private final BaseQuickAdapter.OnItemClickListener mListener;

    private final Object[] mHolders = new Object[FileType.PDF + 1];

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container,
                                  int position) {
        Object holder = mHolders[position];
        RecyclerView view;
        if (holder instanceof RecyclerView) {
            view = (RecyclerView) holder;
        } else {
            view = new RecyclerView(container.getContext());
            mHolders[position] = view;
            view.setLayoutManager(new LinearLayoutManager(container.getContext()));
            GenericQuickAdapter<FileItem> adapter
                    = holder instanceof GenericQuickAdapter
                    ? (GenericQuickAdapter<FileItem>) holder
                    : createAdapter();
            adapter.setOnItemClickListener(mListener);
            adapter.setEmptyView(Utils.createEmptyView(container.getContext()));
            view.setAdapter(adapter);
        }
        container.addView(view);
        return view;
    }

    @SuppressWarnings("All")
    public void setData(@FileType int pos, List<FileItem> data) {
        Object holder = mHolders[pos];
        GenericQuickAdapter<FileItem> adapter;
        if (holder instanceof RecyclerView) {
            adapter = ((GenericQuickAdapter<FileItem>) ((RecyclerView) holder).getAdapter());
        } else {
            if (holder == null) {
                adapter = createAdapter();
                mHolders[pos] = adapter;
            } else {
                adapter = (GenericQuickAdapter<FileItem>) holder;
            }
        }
        adapter.setNewData(data);
    }

    private static GenericQuickAdapter<FileItem> createAdapter() {
        return new GenericQuickAdapter<>(R.layout.item_local_file, BR.file);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container,
                            int position,
                            @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mHolders.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view,
                                    @NonNull Object object) {
        return view == object;
    }
}