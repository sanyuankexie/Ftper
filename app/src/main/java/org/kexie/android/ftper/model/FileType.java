package org.kexie.android.ftper.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.kexie.android.ftper.model.FileType.*;

@IntDef({IMAGE, WORD, XLS, PPT, PDF})
@Retention(RetentionPolicy.SOURCE)
public @interface FileType {
    int IMAGE = 0;
    int WORD = 1;
    int XLS = 2;
    int PPT = 3;
    int PDF = 4;
}
