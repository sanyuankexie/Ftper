package org.kexie.android.ftper.widget;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Unit.MSEC, Unit.SEC, Unit.MIN, Unit.HOUR, Unit.DAY})
@Retention(RetentionPolicy.SOURCE)
public @interface Unit {
    int MSEC = 1;
    int SEC  = 1000;
    int MIN  = 60000;
    int HOUR = 3600000;
    int DAY  = 86400000;
}
