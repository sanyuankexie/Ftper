package org.kexie.android.ftper.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

import org.kexie.android.ftper.R;
import org.kexie.android.ftper.viewmodel.bean.FileItem;
import org.kexie.android.ftper.widget.FileType;
import org.kexie.android.ftper.widget.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public final class SelectorViewModel extends AndroidViewModel {

    private final HandlerThread mWorkerThread;

    private final Handler mHandler;

    private final PublishSubject<File> mOnSelect = PublishSubject.create();

    private final List<MutableLiveData<List<FileItem>>> mLiveData = new ArrayList<>(5);

    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>(false);

    public SelectorViewModel(@NonNull Application application) {
        super(application);
        for (int i = 0; i < 5; i++) {
            mLiveData.add(new MutableLiveData<>());
        }
        mWorkerThread = new HandlerThread(toString());
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());
    }

    public LiveData<List<FileItem>> getPagerData(int position) {
        return mLiveData.get(position);
    }

    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    public Observable<File> getOnSelect() {
        return mOnSelect;
    }

    public void select(FileItem fileItem) {

    }

    public void loadData(int position) {
        if (mLiveData.get(position).getValue() == null) {
            mIsLoading.setValue(true);
            mHandler.post(() -> {
                loadDataInternal(position);
                mIsLoading.postValue(false);
            });
        }
    }

    private void loadDataInternal(int position) {
        if (position == FileType.TYPE_IMAGE) {
            loadImageData();
        } else {
            loadDocData(position);
        }
    }

    private void loadImageData() {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DISPLAY_NAME
        };

        //asc 按升序排列
        //desc 按降序排列
        //projection 是定义返回的数据，selection 通常的sql 语句
        // 例如  selection=MediaStore.Images.ImageColumns.MIME_TYPE+"=? "
        // 那么 selectionArgs=new String[]{"jpg"};
        ContentResolver mContentResolver = getApplication().getContentResolver();
        Cursor cursor = mContentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.ImageColumns.DATE_MODIFIED + "  desc");
        if (cursor == null) {
            return;
        }
        List<FileItem> fileItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(
                    MediaStore.Images.ImageColumns.DATA
            ));
            FileItem fileItem = loadItem(path, FileType.TYPE_IMAGE);
            fileItems.add(fileItem);
        }
        cursor.close();
        mLiveData.get(FileType.TYPE_IMAGE).postValue(fileItems);
    }

    private FileItem loadItem(String path, int type) {
        File file = new File(path);
        String name = file.getName();
        String rawPath = file.getAbsolutePath();
        String size = Utils.sizeToString(file.length());
        String time = Utils.getFileLastModifiedTime(file);
        String iconRes;
        if (type == FileType.TYPE_IMAGE) {
            iconRes = rawPath;
        } else {
            iconRes = getIconName(type);
        }
        return new FileItem(name, rawPath, size, time, iconRes);
    }

    private String getIconName(int type) {
        String select = "";
        Resources resources = getApplication().getResources();
        switch (type) {
            //word
            case FileType.TYPE_WORD:
                select = resources.getResourceName(R.drawable.word);
                break;
            //xls
            case FileType.TYPE_XLS:
                select = resources.getResourceName(R.drawable.xls);
                break;
            //ppt
            case FileType.TYPE_PPT:
                select = resources.getResourceName(R.drawable.ppt);
                break;
            //pdf
            case FileType.TYPE_PDF:
                select = resources.getResourceName(R.drawable.pdf);
                break;
        }
        return select;
    }

    private void loadDocData(int selectType) {
        String[] columns = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.DATA
        };

        ContentResolver contentResolver = getApplication().getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                columns,
                getSelectText(selectType),
                null,
                null);

        if (cursor != null) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            List<FileItem> fileItems = new ArrayList<>();
            while (cursor.moveToNext()) {
                String path = cursor.getString(index);
                FileItem fileItem = loadItem(path, selectType);
                fileItems.add(fileItem);
            }
            cursor.close();
            mLiveData.get(selectType).postValue(fileItems);
        }
    }

    private static String getSelectText(int selectType) {
        String select = "";

        switch (selectType) {
            //word
            case FileType.TYPE_WORD:
                select = "(" + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.doc'"
                        + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.docx'" + ")";
                break;
            //xls
            case FileType.TYPE_XLS:
                select = "(" + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.xls'"
                        + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.xlsx'" + ")";
                break;
            //ppt
            case FileType.TYPE_PPT:
                select = "(" + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.ppt'"
                        + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.pptx'" + ")";
                break;
            //pdf
            case FileType.TYPE_PDF:
                select = "("
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.pdf'"
                        + ")";
                break;
        }
        return select;
    }

    @Override
    protected void onCleared() {
        mWorkerThread.quit();
    }
}