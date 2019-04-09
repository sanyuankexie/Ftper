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

import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

public class TaskItemAdapter extends GenericQuickAdapter<TaskItem> {

    public TaskItemAdapter() {
        super(R.layout.item_task, BR.task);
    }

    @Override
    protected void convert(GenericViewHolder helper, TaskItem item) {
        super.convert(helper, item);
        ItemTaskBinding binding = helper.getBinding();
        ImageView imageView = binding.progress;
        Resources resources = helper.itemView.getResources();
        TaskState state = item.getState();
        switch (state) {
            case FINISH: {
                binding.setState("已完成");
                binding.setPercent("100%");
            }
            break;
            case PAUSE: {
                imageView.setVisibility(View.INVISIBLE);
                binding.state.setTextColor(resources.getColor(R.color.colorBlackAlpha54));
                binding.setPercent("--");
            }
            break;
            case PENDING:
            case RUNNING: {
                imageView.setVisibility(View.VISIBLE);
                //除以100，得到百分比
                float percentFloat = MathUtils
                        .clamp(item.getPercent() / 100.0f, 0, 1);
                int width = binding.progressWrapper.getWidth();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        imageView.getLayoutParams();
                //获取剩下的长度
                layoutParams.rightMargin = (int) ((1 - percentFloat) * width);
                binding.setState(item.getSize());
                binding.state.setTextColor(resources.getColor(R.color.colorBlackAlpha54));
                binding.setPercent(item.getPercent() + "%");
                imageView.setLayoutParams(layoutParams);
                imageView.postInvalidate();
            }
            break;
            case FAILED: {
                binding.setState("任务出错");
                binding.state.setTextColor(resources.getColor(R.color.app_color_theme_1));
            }
            break;
        }
        binding.executePendingBindings();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator != null) {
            animator.setChangeDuration(0);
        }
    }
}
