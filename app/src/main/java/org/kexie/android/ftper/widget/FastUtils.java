package org.kexie.android.ftper.widget;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
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
    public static FragmentTransaction openBaseTransaction(Fragment root) {
        return root.requireFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    @SuppressWarnings("WeakerAccess")
    public static FragmentTransaction
    openTransactionWithBundle(Fragment root,
                              Class<? extends Fragment> type,
                              Bundle bundle) {
        try {
            Fragment fragment = type.newInstance();
            fragment.setArguments(bundle);
            return openBaseTransaction(root)
                    .add(root.getId(), type.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @SuppressWarnings("WeakerAccess")
    public static FragmentTransaction
    openTransactionNoBundle(Fragment root,
                            Class<? extends Fragment> type) {
        return openTransactionWithBundle(root, type, Bundle.EMPTY);
    }

    public static void startFragment(Fragment root,
                                     Class<? extends Fragment> type,
                                     Bundle bundle) {
        FragmentTransaction transaction = openTransactionWithBundle(root, type, bundle);
        if (transaction != null) {
            transaction.commit();
        }
    }

    public static void startFragment(Fragment root,
                                     Class<? extends Fragment> type) {
        FragmentTransaction transaction = openTransactionNoBundle(root, type);
        if (transaction != null) {
            transaction.commit();
        }
    }
}
