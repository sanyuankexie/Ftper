package org.kexie.android.ftper.widget;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static org.kexie.android.ftper.widget.FilePos.*;

@IntDef({IMAGE_POS,
        WORD_POS,
        XLS_POS,
        PPT_POS,
        PDF_POS})
@Retention(RetentionPolicy.SOURCE)
public @interface FilePos {
    int IMAGE_POS = 0;
    int WORD_POS = 1;
    int XLS_POS = 2;
    int PPT_POS = 3;
    int PDF_POS = 4;
}
