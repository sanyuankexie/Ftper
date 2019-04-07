package org.kexie.android.ftper.widget;

import android.view.ViewGroup;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;

import org.jetbrains.annotations.NotNull;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.ItemTransferBinding;
import org.kexie.android.ftper.viewmodel.bean.TransferItem;

import androidx.core.math.MathUtils;

public final class TransferItemAdapter extends GenericQuickAdapter<TransferItem> {
    public TransferItemAdapter() {
        super(R.layout.item_transfer, BR.file);
        closeLoadAnimation();
    }

    @Override
    protected void convert(@NotNull GenericViewHolder helper, TransferItem item) {
        super.convert(helper, item);
        ItemTransferBinding binding = helper.getBinding();
        ImageView imageView = binding.progress;
        //除以100，得到百分比
        float percentFloat = MathUtils.clamp(item.getPercent() / 100.0f,0,1);
        //获取总长度
        final int ivWidth = binding.progressWrapper.getWidth();
        Logger.d(ivWidth);
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        //获取剩下的长度
        int marginEnd = (int) ((1 - percentFloat) * ivWidth);
        lp.width = ivWidth - marginEnd;
        imageView.setLayoutParams(lp);
    }
}
