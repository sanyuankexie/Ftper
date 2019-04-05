package org.kexie.android.ftper.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static org.kexie.android.ftper.model.FileType.*;

@IntDef({TYPE_IMAGE,
        TYPE_WORD,
        TYPE_XLS,
        TYPE_PPT,
        TYPE_PDF})
@Retention(RetentionPolicy.SOURCE)
public @interface FileType {
    int TYPE_IMAGE = 0;
    int TYPE_WORD = 1;
    int TYPE_XLS = 2;
    int TYPE_PPT = 3;
    int TYPE_PDF = 4;
}
