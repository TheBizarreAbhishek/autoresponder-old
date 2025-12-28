package com.thebizarreabhishek.app.ui.smartreply;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.thebizarreabhishek.app.databinding.ItemSmartReplyBinding;
import com.thebizarreabhishek.app.models.SmartReply;
import java.util.ArrayList;
import java.util.List;

public class SmartReplyAdapter extends RecyclerView.Adapter<SmartReplyAdapter.ViewHolder> {

    private List<SmartReply> replies = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SmartReply reply);
        void onToggleChange(SmartReply reply, boolean enabled);
        void onDeleteClick(SmartReply reply);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setReplies(List<SmartReply> replies) {
        this.replies = replies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSmartReplyBinding binding = ItemSmartReplyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmartReply reply = replies.get(position);
        holder.bind(reply);
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSmartReplyBinding binding;

        public ViewHolder(ItemSmartReplyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SmartReply reply) {
            binding.tvTrigger.setText("\"" + reply.getTrigger() + "\"");
            binding.tvResponse.setText(reply.getResponse());
            binding.switchEnabled.setChecked(reply.isEnabled());

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(reply);
            });

            binding.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onToggleChange(reply, isChecked);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(reply);
            });
        }
    }
}
