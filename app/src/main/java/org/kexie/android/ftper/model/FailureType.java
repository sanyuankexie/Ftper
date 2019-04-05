package org.kexie.android.ftper.model;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static org.kexie.android.ftper.model.FailureType.*;

@IntDef({UNKNOWN_ERROR,FILE_EXIT})
@Retention(RetentionPolicy.SOURCE)
public @interface FailureType {
    int UNKNOWN_ERROR = 0;
    int FILE_EXIT = 1;
}
