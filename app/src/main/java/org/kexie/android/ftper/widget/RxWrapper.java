package org.kexie.android.ftper.widget;

import com.uber.autodispose.AutoDispose;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.collection.ArrayMap;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.subjects.PublishSubject;

import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public final class RxWrapper<X>
{

    private static final int MINI_TIME = 200;

    private final Class<X> mInterface;

    private X mInner;

    private Lifecycle mLifecycle;

    private int mTime;

    private Lifecycle.Event mEvent;

    private RxWrapper(Class<X> type)
    {
        mInterface = type;
    }

    public static <X> RxWrapper<X> create(Class<X> type)
    {
        return new RxWrapper<>(type);
    }

    public RxWrapper<X> inner(X inner)
    {
        mInner = inner;
        return this;
    }

    public RxWrapper<X> lifecycle(Lifecycle lifecycle)
    {
        this.mLifecycle = lifecycle;
        return this;
    }

    public RxWrapper<X> owner(LifecycleOwner owner)
    {
        return lifecycle(owner.getLifecycle());
    }

    public RxWrapper<X> throttleFirst(int time)
    {
        mTime = time;
        return this;
    }

    public RxWrapper<X> releaseOn(Lifecycle.Event event)
    {
        mEvent = event;
        return this;
    }

    @SuppressWarnings("unchecked")
    public X build()
    {
        if (mInterface == null || !mInterface.isInterface())
        {
            throw new IllegalArgumentException();
        }
        if (mTime < MINI_TIME)
        {
            mTime = MINI_TIME;
        }
        if (mEvent == null)
        {
            mEvent = Lifecycle.Event.ON_DESTROY;
        }
        if (mLifecycle == null || mInner == null)
        {
            throw new IllegalStateException();
        }
        Map<Method, PublishSubject<Object[]>> subjectMap = new ArrayMap<>();
        for (Method method : mInterface.getDeclaredMethods())
        {
            PublishSubject<Object[]> subject = PublishSubject.create();
            subject.throttleFirst(mTime, TimeUnit.MILLISECONDS)
                    .observeOn(mainThread())
                    .as(AutoDispose.autoDisposable(from(mLifecycle, mEvent)))
                    .subscribe(args -> method.invoke(mInner, args));
            subjectMap.put(method, subject);
        }
        return (X) Proxy.newProxyInstance(mInterface.getClassLoader(),
                new Class[]{mInterface},
                (proxy, method, args) -> {
                    if (Object.class.equals(method.getDeclaringClass()))
                    {
                        return method.invoke(proxy, args);
                    }
                    PublishSubject<Object[]> subject = subjectMap.get(method);
                    if (subject != null)
                    {
                        subject.onNext(args);
                    }
                    return null;
                });
    }
}
