package org.kexie.android.ftper.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import org.kexie.android.ftper.viewmodel.bean.FileItem;
import org.kexie.android.ftper.widget.FilePos;

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

    public SelectorViewModel(@NonNull Application application) {
        super(application);
    }

    private final PublishSubject<File> mOnSelect = PublishSubject.create();

    private final MutableLiveData<List<FileItem>> mImage = new MutableLiveData<>();

    private final MutableLiveData<List<FileItem>> mWord = new MutableLiveData<>();

    private final MutableLiveData<List<FileItem>> mXls = new MutableLiveData<>();

    private final MutableLiveData<List<FileItem>> mPdf = new MutableLiveData<>();

    private final MutableLiveData<List<FileItem>> mPpt = new MutableLiveData<>();

    public MutableLiveData<List<FileItem>> getWord() {
        return mWord;
    }

    public LiveData<List<FileItem>> getImage() {
        return mImage;
    }

    public LiveData<List<FileItem>> getPdf() {
        return mPdf;
    }

    public LiveData<List<FileItem>> getPpt() {
        return mPpt;
    }

    public LiveData<List<FileItem>> getXls() {
        return mXls;
    }

    public Observable<File> getOnSelect() {
        return mOnSelect;
    }

    public void select(FileItem fileItem) {

    }

    private void loadDataInternal(int selectType) {

        String[] columns = new String[]{MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED, MediaStore.Files.FileColumns.DATA};

        String select = "";

        switch (selectType) {
            //word
            case FilePos.WORD_POS:
                select = "(" + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.doc'"
                        + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.docx'" + ")";
                break;
            //xls
            case FilePos.XLS_POS:
                select = "(" + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.xls'"
                        + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.xlsx'" + ")";
                break;
            //ppt
            case FilePos.PPT_POS:
                select = "(" + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.ppt'"
                        + " or "
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.pptx'" + ")";
                break;
            //pdf
            case FilePos.PDF_POS:
                select = "("
                        + MediaStore.Files.FileColumns.DATA
                        + " LIKE '%.pdf'"
                        + ")";
                break;
        }

        ContentResolver contentResolver = getApplication().getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                columns, select,
                null,
                null);

        if (cursor != null) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            List<FileItem> fileItems = new ArrayList<>();
            while (cursor.moveToNext()) {
                String path = cursor.getString(index);

            }
            cursor.close();
            switch (selectType) {
                //word
                case FilePos.WORD_POS: {
                    mWord.setValue(fileItems);
                }
                break;
                //xls
                case FilePos.XLS_POS: {
                    mXls.setValue(fileItems);
                }
                break;
                //ppt
                case FilePos.PPT_POS: {
                    mPpt.setValue(fileItems);
                }
                break;
                //pdf
                case FilePos.PDF_POS: {
                    mPdf.setValue(fileItems);
                }
                break;
            }
        }
    }
}
