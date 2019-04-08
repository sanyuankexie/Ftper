package org.kexie.android.ftper.widget;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.core.math.MathUtils;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.ItemTaskBinding;
import org.kexie.android.ftper.viewmodel.bean.TaskItem;

public class TaskItemQuickAdapter extends GenericQuickAdapter<TaskItem> {

    public TaskItemQuickAdapter() {
        super(R.layout.item_task, BR.task);
    }

    @Override
    protected void convert(GenericViewHolder helper, TaskItem item) {
        super.convert(helper, item);
        ItemTaskBinding binding = helper.getBinding();
        //除以100，得到百分比
        float percentFloat = MathUtils.clamp(item.getPercent() / 100.0f, 0, 1);
        int width = binding.progressWrapper.getWidth();
        ImageView imageView = binding.progress;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                imageView.getLayoutParams();
        //获取剩下的长度
        layoutParams.rightMargin = (int) ((1 - percentFloat) * width);
        imageView.setLayoutParams(layoutParams);
        imageView.postInvalidate();
    }
}
