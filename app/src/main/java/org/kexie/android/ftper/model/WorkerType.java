package org.kexie.android.ftper.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static org.kexie.android.ftper.model.WorkerType.*;

@IntDef({DOWNLOAD, UPLOAD})
@Retention(RetentionPolicy.SOURCE)
public @interface WorkerType {
    int DOWNLOAD = 0;
    int UPLOAD = 1;
}
