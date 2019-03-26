package org.kexie.android.ftper.widget;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

public final class FastUtils {

    private FastUtils() {
        throw new AssertionError();
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> void subscribe(LifecycleOwner lifecycleOwner,
                                     Observable<T> observable,
                                     Lifecycle.Event event,
                                     Consumer<T> consumer) {
        observable.observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider
                        .from(lifecycleOwner, event)))
                .subscribe(consumer);
    }

    public static void subscribeToast(Fragment fragment,
                                      Observable<String> observable,
                                      BiFunction<Context, String, Toast> function) {
        subscribe(fragment,
                observable,
                Lifecycle.Event.ON_PAUSE,
                text -> function.apply(fragment.requireContext(), text).show());
    }

    @SuppressWarnings("WeakerAccess")
    public static FragmentTransaction openTransaction(Fragment root) {
        return root.requireFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    @SuppressWarnings("WeakerAccess")
    public static FragmentTransaction openTransaction2(Fragment root,
                                                       Class<? extends Fragment> type) {
        try {
            return openTransaction(root)
                    .add(root.getId(), type.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void startFragment(Fragment root,
                                     Class<? extends Fragment> type) {
        FragmentTransaction transaction = openTransaction2(root, type);
        if (transaction != null) {
            transaction.commit();
        }
    }

    public static View.OnClickListener toRxListener(
            LifecycleOwner lifecycleOwner,
            View.OnClickListener listener) {
        return new RxOnClickWrapper(
                lifecycleOwner,
                listener,
                Lifecycle.Event.ON_DESTROY);
    }

    public static <X> BaseQuickAdapter.OnItemClickListener
    toRxListener(LifecycleOwner lifecycleOwner,
                 GenericQuickAdapter.OnItemClickListener<X> listener) {
        return new RxOnItemClickWrapper<>(
                lifecycleOwner,
                listener,
                Lifecycle.Event.ON_DESTROY);
    }
}
