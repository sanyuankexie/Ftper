package org.kexie.android.ftper.widget;

import android.view.View;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

import java.util.concurrent.TimeUnit;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

public final class RxOnClickWrapper implements View.OnClickListener {
    private final PublishSubject<View> mSubject = PublishSubject.create();

    @SuppressWarnings("WeakerAccess")
    public RxOnClickWrapper(LifecycleOwner lifecycleOwner,
                            View.OnClickListener inner,
                            Lifecycle.Event event) {
        mSubject.throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(autoDisposable(from(lifecycleOwner, event)))
                .subscribe(inner::onClick);
    }

    @Override
    public void onClick(View v) {
        mSubject.onNext(v);
    }
}
