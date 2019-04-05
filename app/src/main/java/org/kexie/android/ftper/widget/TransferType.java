package org.kexie.android.ftper.widget;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static org.kexie.android.ftper.widget.TransferType.*;

@IntDef({TYPE_ERROR,TYPE_UPLOAD,TYPE_DOWNLOAD,TYPE_UPLOAD_FINISH,TYPE_DOWNLOAD_FINISH})
@Retention(RetentionPolicy.SOURCE)
public @interface TransferType {
    int TYPE_ERROR = 0;
    int TYPE_UPLOAD = 1;
    int TYPE_DOWNLOAD = 2;
    int TYPE_UPLOAD_FINISH = 3;
    int TYPE_DOWNLOAD_FINISH = 4;
}
