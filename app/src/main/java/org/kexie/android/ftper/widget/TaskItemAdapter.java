package org.kexie.android.ftper.widget;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.ItemTaskBinding;
import org.kexie.android.ftper.viewmodel.bean.TaskItem;
import org.kexie.android.ftper.viewmodel.bean.TaskState;

import java.util.Objects;

import androidx.core.math.MathUtils;

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
        if (!item.getFinish()) {
            if (item.getPercent() != 0 && !Objects.equals(TaskState.PAUSE, item.getState())) {
                imageView.setVisibility(View.VISIBLE);
                float percentFloat = MathUtils.clamp(item.getPercent() / 100.0f, 0, 1);
                int width = binding.progressWrapper.getWidth();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        imageView.getLayoutParams();
                //获取剩下的长度
                layoutParams.rightMargin = (int) ((1 - percentFloat) * width);
                imageView.setLayoutParams(layoutParams);
                imageView.postInvalidate();
                binding.setPercent(item.getPercent() + "%");
            } else {
                imageView.setVisibility(View.INVISIBLE);
                binding.setPercent("--");
            }
        } else {
            binding.setPercent("100%");
        }
        Resources resources = helper.itemView.getResources();
        if (Objects.equals(TaskState.FAILED, item.getState())) {
            binding.state.setTextColor(resources.getColor(R.color.app_color_theme_1));
        } else {
            binding.state.setTextColor(resources.getColor(R.color.colorBlackAlpha54));
        }
        binding.executePendingBindings();
    }
}
