package org.kexie.android.ftper.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.core.math.MathUtils;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.ItemTaskBinding;
import org.kexie.android.ftper.viewmodel.bean.TaskItem;

public class TaskItemAdapter extends GenericQuickAdapter<TaskItem> {

    public TaskItemAdapter() {
        super(R.layout.item_task, BR.task);
    }

    @Override
    protected void convert(GenericViewHolder helper, TaskItem item) {
        super.convert(helper, item);
        ItemTaskBinding binding = helper.getBinding();
        //除以100，得到百分比
        ImageView imageView = binding.progress;
        if (item.getPercent() != 0) {
            imageView.setVisibility(View.VISIBLE);
            float percentFloat = MathUtils.clamp(item.getPercent() / 100.0f, 0, 1);
            int width = binding.progressWrapper.getWidth();
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    imageView.getLayoutParams();
            //获取剩下的长度
            layoutParams.rightMargin = (int) ((1 - percentFloat) * width);
            imageView.setLayoutParams(layoutParams);
            imageView.postInvalidate();
        } else {
            imageView.setVisibility(View.INVISIBLE);
        }
    }
}
