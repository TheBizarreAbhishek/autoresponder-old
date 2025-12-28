package com.thebizarreabhishek.app.ui.logs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.thebizarreabhishek.app.activities.ContactMessagesActivity;
import com.thebizarreabhishek.app.databinding.FragmentLogsBinding;
import com.thebizarreabhishek.app.models.ContactSummary;

import java.util.List;

public class LogsFragment extends Fragment {

    private FragmentLogsBinding binding;

    private LogsAdapter adapter;
    private com.thebizarreabhishek.app.helpers.DatabaseHelper dbHelper;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogsBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupListeners();

        dbHelper = new com.thebizarreabhishek.app.helpers.DatabaseHelper(requireContext());
        loadContacts();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new LogsAdapter();
        adapter.setOnContactClickListener(this::openContactMessages);
        binding.recyclerLogs.setAdapter(adapter);
    }

    private void openContactMessages(ContactSummary contact) {
        Intent intent = new Intent(requireContext(), ContactMessagesActivity.class);
        intent.putExtra(ContactMessagesActivity.EXTRA_SENDER_NAME, contact.getSenderName());
        intent.putExtra(ContactMessagesActivity.EXTRA_PLATFORM, contact.getPlatform());
        startActivity(intent);
    }

    private void setupListeners() {
        binding.btnFilter.setOnClickListener(v -> {
            android.widget.Toast
                    .makeText(requireContext(), "Filtering not implemented yet", android.widget.Toast.LENGTH_SHORT)
                    .show();
        });

        binding.btnClear.setOnClickListener(v -> {
            dbHelper.deleteOldMessages();
            loadContacts();
            android.widget.Toast.makeText(requireContext(), "Logs Cleared", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void loadContacts() {
        new Thread(() -> {
            List<ContactSummary> contacts = dbHelper.getUniqueSenders();
            requireActivity().runOnUiThread(() -> {
                if (contacts.isEmpty()) {
                    android.widget.Toast.makeText(requireContext(), "No logs found", android.widget.Toast.LENGTH_SHORT)
                            .show();
                }
                adapter.setContacts(contacts);
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContacts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
