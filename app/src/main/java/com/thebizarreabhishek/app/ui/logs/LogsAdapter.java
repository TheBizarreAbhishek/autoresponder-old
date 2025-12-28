package com.thebizarreabhishek.app.ui.logs;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.thebizarreabhishek.app.R;
import com.thebizarreabhishek.app.databinding.ItemLogBinding;
import com.thebizarreabhishek.app.models.ContactSummary;
import java.util.ArrayList;
import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ContactViewHolder> {

    private List<ContactSummary> contactList = new ArrayList<>();
    private OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(ContactSummary contact);
    }

    public void setOnContactClickListener(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void setContacts(List<ContactSummary> contacts) {
        this.contactList = contacts;
        notifyDataSetChanged();
    }

    public void clearContacts() {
        this.contactList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLogBinding binding = ItemLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactSummary contact = contactList.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final ItemLogBinding binding;

        public ContactViewHolder(ItemLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ContactSummary contact) {
            binding.tvName.setText(contact.getSenderName());
            binding.tvLastMessage.setText(contact.getLastMessage());
            binding.tvTime.setText(contact.getLastTimestamp());
            binding.tvCount.setText(String.valueOf(contact.getMessageCount()));

            // Set first letter as avatar text
            if (contact.getSenderName() != null && !contact.getSenderName().isEmpty()) {
                binding.tvAvatar.setText(String.valueOf(contact.getSenderName().charAt(0)).toUpperCase());
            } else {
                binding.tvAvatar.setText("?");
            }

            // Set platform icon
            binding.ivPlatform.setImageResource(getPlatformIcon(contact.getPlatform()));

            // Click listener
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactClick(contact);
                }
            });
        }

        private int getPlatformIcon(String platform) {
            if (platform == null) return R.drawable.ic_smart_toy_black_24dp;
            
            switch (platform) {
                case "whatsapp":
                    return R.drawable.ic_whatsapp;
                case "whatsapp_business":
                    return R.drawable.ic_whatsapp_business;
                case "telegram":
                    return R.drawable.ic_telegram;
                case "instagram":
                    return R.drawable.ic_instagram;
                case "snapchat":
                    return R.drawable.ic_snapchat;
                case "twitter":
                    return R.drawable.ic_twitter_x;
                default:
                    return R.drawable.ic_smart_toy_black_24dp;
            }
        }
    }
}
