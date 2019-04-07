package org.kexie.android.ftper.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.kexie.android.ftper.R
import org.kexie.android.ftper.model.FileType
import org.kexie.android.ftper.viewmodel.bean.FileItem
import org.kexie.android.ftper.widget.Utils
import java.io.File
import java.util.*

class SelectorViewModel(application: Application)
    : AndroidViewModel(application) {

    private val mWorkerThread = HandlerThread(toString())
            .apply {
                start()
            }

    private val mHandler = Handler(mWorkerThread.looper)

    private val mOnSelect = PublishSubject.create<File>()

    private val mLiveData = ArrayList<MutableLiveData<List<FileItem>>>(5)
            .apply {
                for (i in 0..4) {
                    add(MutableLiveData())
                }
            }

    private val mIsLoading = MutableLiveData(false)

    val isLoading: LiveData<Boolean>
        get() = mIsLoading

    val onSelect: Observable<File>
        get() = mOnSelect

    fun getPagerData(position: Int): LiveData<List<FileItem>> {
        return mLiveData[position]
    }

    fun select(fileItem: FileItem) {
        mOnSelect.onNext(File(fileItem.path))
    }

    fun loadData(position: Int) {
        if (mLiveData[position].value == null) {
            mIsLoading.value = true
            mHandler.post {
                loadDataInternal(position)
                mIsLoading.postValue(false)
            }
        }
    }

    private fun loadDataInternal(position: Int) {
        if (position == FileType.IMAGE) {
            loadImageData()
        } else {
            loadDocData(position)
        }
    }

    private fun loadImageData() {
        val projection = arrayOf(
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DISPLAY_NAME)

        //asc 按升序排列
        //desc 按降序排列
        //projection 是定义返回的数据，selection 通常的sql 语句
        // 例如  selection=MediaStore.Images.ImageColumns.MIME_TYPE+"=? "
        // 那么 selectionArgs=new String[]{"jpg"};
        val mContentResolver = getApplication<Application>().contentResolver
        @SuppressLint("Recycle")
        val cursor = mContentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                MediaStore.Images.ImageColumns.DATE_MODIFIED + "  desc") ?: return
        val fileItems = ArrayList<FileItem>()
        while (cursor.moveToNext()) {
            val path = cursor.getString(cursor.getColumnIndex(
                    MediaStore.Images.ImageColumns.DATA
            ))
            val fileItem = loadItem(path, FileType.IMAGE)
            fileItems.add(fileItem)
        }
        cursor.close()
        mLiveData[FileType.IMAGE].postValue(fileItems)
    }

    private fun loadItem(path: String, type: Int): FileItem {
        val file = File(path)
        val name = file.name
        val rawPath = file.absolutePath
        val size = Utils.sizeToString(file.length())
        val time = Utils.getFileLastModifiedTime(file)
        val iconRes: String
        if (type == FileType.IMAGE) {
            iconRes = rawPath
        } else {
            iconRes = getIconName(type)
        }
        return FileItem(name, rawPath, size, time, iconRes)
    }

    private fun getIconName(type: Int): String {
        var select = ""
        val resources = getApplication<Application>().resources
        when (type) {
            //word
            FileType.WORD -> select = resources.getResourceName(R.drawable.word)
            //xls
            FileType.XLS -> select = resources.getResourceName(R.drawable.xls)
            //ppt
            FileType.PPT -> select = resources.getResourceName(R.drawable.ppt)
            //pdf
            FileType.PDF -> select = resources.getResourceName(R.drawable.pdf)
        }
        return select
    }

    private fun loadDocData(selectType: Int) {
        val columns = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.DATA)

        val contentResolver = getApplication<Application>().contentResolver
        val cursor = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                columns,
                getSelectText(selectType), null, null)

        if (cursor != null) {
            val index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val fileItems = ArrayList<FileItem>()
            while (cursor.moveToNext()) {
                val path = cursor.getString(index)
                val fileItem = loadItem(path, selectType)
                fileItems.add(fileItem)
            }
            cursor.close()
            mLiveData[selectType].postValue(fileItems)
        }
    }

    private fun getSelectText(selectType: Int): String {
        var select = ""
        when (selectType) {
            //word
            FileType.WORD -> select = ("(" + MediaStore.Files.FileColumns.DATA
                    + " LIKE '%.doc'"
                    + " or "
                    + MediaStore.Files.FileColumns.DATA
                    + " LIKE '%.docx'" + ")")
            //xls
            FileType.XLS -> select = ("(" + MediaStore.Files.FileColumns.DATA
                    + " LIKE '%.xls'"
                    + " or "
                    + MediaStore.Files.FileColumns.DATA
                    + " LIKE '%.xlsx'" + ")")
            //ppt
            FileType.PPT -> select = ("(" + MediaStore.Files.FileColumns.DATA
                    + " LIKE '%.ppt'"
                    + " or "
                    + MediaStore.Files.FileColumns.DATA
                    + " LIKE '%.pptx'" + ")")
            //pdf
            FileType.PDF -> select = ("("
                    + MediaStore.Files.FileColumns.DATA
                    + " LIKE '%.pdf'"
                    + ")")
        }
        return select
    }

    override fun onCleared() {
        mWorkerThread.quit()
    }
}