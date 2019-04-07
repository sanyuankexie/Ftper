package org.kexie.android.ftper.widget;

import org.jetbrains.annotations.NotNull;
import org.kexie.android.ftper.BR;
import org.kexie.android.ftper.R;
import org.kexie.android.ftper.databinding.ItemTransferBinding;
import org.kexie.android.ftper.viewmodel.bean.TransferItem;

public final class TransferItemAdapter extends GenericQuickAdapter<TransferItem> {
    public TransferItemAdapter() {
        super(R.layout.item_transfer, BR.file);
        closeLoadAnimation();
    }

    @Override
    protected void convert(@NotNull GenericViewHolder helper, TransferItem item) {
        super.convert(helper, item);
        ItemTransferBinding binding = helper.getBinding();
    }
}
