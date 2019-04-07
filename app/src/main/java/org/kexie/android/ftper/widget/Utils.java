package org.kexie.android.ftper.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import org.kexie.android.ftper.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
import static com.bumptech.glide.Priority.IMMEDIATE;
import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public final class Utils {

    private Utils() {
        throw new AssertionError();
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> void subscribe(LifecycleOwner lifecycleOwner,
                                     Observable<T> observable,
                                     Lifecycle.Event event,
                                     Consumer<T> consumer) {
        observable.observeOn(mainThread())
                .as(autoDisposable(from(lifecycleOwner, event)))
                .subscribe(consumer);
    }

    public static void subscribeDialog(Fragment fragment,
                                       Observable<String> observable,
                                       @QMUITipDialog.Builder.IconType int iconType) {
        subscribe(fragment, observable, Lifecycle.Event.ON_PAUSE,
                text -> {
                    QMUITipDialog dialog = new QMUITipDialog
                            .Builder(fragment.requireContext())
                            .setIconType(iconType)
                            .setTipWord(text)
                            .create();
                    dialog.setCancelable(false);
                    dialog.show();
                    Objects.requireNonNull(dialog.getWindow()).getDecorView()
                            .postDelayed(dialog::dismiss, 1000);
                });
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
                .setTransition(TRANSIT_FRAGMENT_OPEN);
    }

    @SuppressWarnings("WeakerAccess")
    public static FragmentTransaction
    openTransactionWithCode(Fragment root,
                            Class<? extends Fragment> type,
                            Bundle bundle,
                            int requestCode) {
        try {
            Fragment fragment = type.newInstance();
            fragment.setTargetFragment(root, requestCode);
            fragment.setArguments(bundle);
            return openBaseTransaction(root)
                    .add(root.getId(), fragment);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static FragmentTransaction
    openTransaction(Fragment root,
                    Class<? extends Fragment> type,
                    Bundle bundle) {
        try {
            Fragment fragment = type.newInstance();
            fragment.setArguments(bundle);
            return openBaseTransaction(root)
                    .add(root.getId(), fragment);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void startFragment(Fragment root,
                                     Class<? extends Fragment> type,
                                     Bundle bundle) {
        FragmentTransaction transaction = openTransaction(root, type, bundle);
        if (transaction != null) {
            transaction.commit();
        }
    }

    public static void startFragmentForResult(Fragment root,
                                              Class<? extends Fragment> type,
                                              Bundle bundle,
                                              int requestCode) {
        FragmentTransaction transaction = openTransactionWithCode(root, type, bundle, requestCode);
        if (transaction != null) {
            transaction.commit();
        }
    }

    @BindingAdapter("load_async")
    public static void loadAsync(ImageView imageView, String path) {
        String packName = imageView.getContext().getPackageName();
        Resources resources = imageView.getResources();
        int id = resources.getIdentifier(path, imageView
                .getContext()
                .getString(R.string.mipmap), packName);
        if (id == 0) {
            id = resources.getIdentifier(path, imageView
                    .getContext()
                    .getString(R.string.drawable), packName);
        }
        RequestManager manager = Glide.with(imageView);
        RequestBuilder<Drawable> builder;
        if (id == 0) {
            builder = manager.load(path);
        } else {
            builder = manager.load(id);
        }
        builder.apply(RequestOptions.priorityOf(IMMEDIATE))
                .into(imageView);
    }


    public static String sizeToString(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        String wrongSize = "0B";
        if (size == 0) {
            return wrongSize;
        }
        if (size < 1024) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    public static String getFileLastModifiedTime(File f) {
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        SimpleDateFormat formatter = new
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        cal.setTimeInMillis(time);
        return formatter.format(cal.getTime());
    }

    public static View createEmptyView(Context context) {
        AppCompatTextView textView = new AppCompatTextView(context);
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(context.getResources().getColor(R.color.colorBlackAlpha26));
        textView.setText(R.string.this_is_empty);
        return textView;
    }
}
