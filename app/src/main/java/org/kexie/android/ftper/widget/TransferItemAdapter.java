package org.kexie.android.ftper.widget;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.ItemTransferBinding;
import org.kexie.android.ftper.viewmodel.bean.TransferItem;

import java.util.Objects;

public final class TransferItemAdapter
        extends ListAdapter<TransferItem, TransferItemAdapter.DataBindingHolder> {

    public TransferItemAdapter() {
        super(CALLBACK);
    }

    @NonNull
    @Override
    public DataBindingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTransferBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_transfer,
                parent,
                false);
        return new DataBindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DataBindingHolder holder, int position) {
        TransferItem item = getItem(position);
        holder.mBinding.setFile(item);
        //除以100，得到百分比
        float percentFloat = MathUtils.clamp(item.getPercent() / 100.0f, 0, 1);
        int width = holder.mBinding.progressWrapper.getWidth();
        ImageView imageView = holder.mBinding.progress;
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        //获取剩下的长度
        int marginEnd = (int) ((1 - percentFloat) * width);
        layoutParams.width = width - marginEnd;
        imageView.setLayoutParams(layoutParams);
        imageView.postInvalidate();
    }

    private static final DiffUtil.ItemCallback<TransferItem> CALLBACK
            = new DiffUtil.ItemCallback<TransferItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransferItem oldItem, @NonNull TransferItem newItem) {
            return Objects.equals(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransferItem oldItem, @NonNull TransferItem newItem) {
            return areItemsTheSame(oldItem, newItem);
        }
    };

    static final class DataBindingHolder extends RecyclerView.ViewHolder {
        private ItemTransferBinding mBinding;

        private DataBindingHolder(ItemTransferBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
