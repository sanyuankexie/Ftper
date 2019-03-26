package org.kexie.android.ftper.widget;

import android.view.View;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.chad.library.adapter.base.BaseQuickAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

public final class RxOnItemClickWrapper<X>
        implements BaseQuickAdapter.OnItemClickListener {

    private final PublishSubject<Object[]> mSubject = PublishSubject.create();

    private static final Stack<Object[]> sCache = new Stack<>();

    @SuppressWarnings({"unchecked","WeakerAccess"})
    public RxOnItemClickWrapper(LifecycleOwner lifecycleOwner,
                                GenericQuickAdapter.OnItemClickListener<X> listener,
                                Lifecycle.Event event) {
        mSubject.throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(lifecycleOwner, event)))
                .subscribe((pack) -> {
                    listener.onItemClick(
                            (GenericQuickAdapter<X>) pack[0],
                            (View) pack[1],
                            (int) pack[2]);
                    Arrays.fill(pack, null);
                    sCache.push(pack);
                });
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Object[] message;
        if (sCache.isEmpty()) {
            message = new Object[3];
        } else {
            message = sCache.pop();
        }
        message[0] = adapter;
        message[1] = view;
        message[2] = position;
        mSubject.onNext(message);
    }
}
