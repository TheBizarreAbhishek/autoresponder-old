package com.thebizarreabhishek.app.ui.smartreply;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.thebizarreabhishek.app.R;
import com.thebizarreabhishek.app.databinding.FragmentSmartReplyBinding;
import com.thebizarreabhishek.app.helpers.DatabaseHelper;
import com.thebizarreabhishek.app.models.SmartReply;
import java.util.List;

public class SmartReplyFragment extends Fragment {

    private FragmentSmartReplyBinding binding;
    private SmartReplyAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSmartReplyBinding.inflate(inflater, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        setupRecyclerView();
        setupListeners();
        loadReplies();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new SmartReplyAdapter();
        adapter.setOnItemClickListener(new SmartReplyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SmartReply reply) {
                showEditDialog(reply);
            }

            @Override
            public void onToggleChange(SmartReply reply, boolean enabled) {
                dbHelper.updateSmartReply(reply.getId(), reply.getTrigger(), reply.getResponse(), enabled);
            }

            @Override
            public void onDeleteClick(SmartReply reply) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Smart Reply")
                        .setMessage("Delete this preset?")
                        .setPositiveButton("Delete", (d, w) -> {
                            dbHelper.deleteSmartReply(reply.getId());
                            loadReplies();
                            Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        binding.recyclerSmartReplies.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void loadReplies() {
        new Thread(() -> {
            List<SmartReply> replies = dbHelper.getAllSmartReplies();
            requireActivity().runOnUiThread(() -> {
                adapter.setReplies(replies);
                binding.tvEmpty.setVisibility(replies.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_smart_reply, null);
        builder.setView(dialogView);

        EditText etTrigger = dialogView.findViewById(R.id.et_trigger);
        EditText etResponse = dialogView.findViewById(R.id.et_response);
        View btnCancel = dialogView.findViewById(R.id.btn_cancel);
        View btnSave = dialogView.findViewById(R.id.btn_save);

        AlertDialog dialog = builder.create();
        applyDialogStyling(dialog);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String trigger = etTrigger.getText().toString().trim();
            String response = etResponse.getText().toString().trim();

            if (trigger.isEmpty() || response.isEmpty()) {
                Toast.makeText(requireContext(), "Fill both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.insertSmartReply(trigger, response);
            loadReplies();
            dialog.dismiss();
            Toast.makeText(requireContext(), "Smart Reply added", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showEditDialog(SmartReply reply) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_smart_reply, null);
        builder.setView(dialogView);

        android.widget.TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etTrigger = dialogView.findViewById(R.id.et_trigger);
        EditText etResponse = dialogView.findViewById(R.id.et_response);
        View btnCancel = dialogView.findViewById(R.id.btn_cancel);
        View btnSave = dialogView.findViewById(R.id.btn_save);

        tvTitle.setText("Edit Smart Reply");
        etTrigger.setText(reply.getTrigger());
        etResponse.setText(reply.getResponse());

        AlertDialog dialog = builder.create();
        applyDialogStyling(dialog);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String trigger = etTrigger.getText().toString().trim();
            String response = etResponse.getText().toString().trim();

            if (trigger.isEmpty() || response.isEmpty()) {
                Toast.makeText(requireContext(), "Fill both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.updateSmartReply(reply.getId(), trigger, response, reply.isEnabled());
            loadReplies();
            dialog.dismiss();
            Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void applyDialogStyling(android.app.Dialog dialog) {
        if (dialog == null || dialog.getWindow() == null) return;
        android.view.Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(R.drawable.dialog_background);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReplies();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
