package com.thebizarreabhishek.app.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.thebizarreabhishek.app.databinding.FragmentHomeBinding;
import com.thebizarreabhishek.app.helpers.NotificationHelper;
import android.app.AlertDialog;
import com.thebizarreabhishek.app.R;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateServiceStatus();
        loadStats();
        loadCustomPrompt();
        loadNaturalDelayStatus();

    }

    private void loadNaturalDelayStatus() {
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean isEnabled = prefs.getBoolean("is_natural_delay_enabled", false);
        binding.switchNaturalDelay.setChecked(isEnabled);

        binding.switchNaturalDelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("is_natural_delay_enabled", isChecked).apply();
        });
    }

    private void loadCustomPrompt() {
        binding.cardCustomPrompt.setOnClickListener(v -> showCustomPromptPopup());
    }

    private void showEditTextDialog(String title, String key, String currentValue, android.widget.TextView updateView) {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setText(currentValue);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = input.getText().toString();
                    prefs(key, newValue); // helper
                    if (updateView != null)
                        updateView.setText(newValue);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void prefs(String key, String value) {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString(key, value).apply();
    }

    private void showCustomPromptPopup() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(com.thebizarreabhishek.app.R.layout.dialog_custom_prompt, null);
        builder.setView(dialogView);

        com.google.android.material.textfield.TextInputEditText etPrompt = dialogView
                .findViewById(com.thebizarreabhishek.app.R.id.et_dialog_prompt);
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        String savedPrompt = prefs.getString("custom_prompt", "You are a friendly AI assistant.");
        etPrompt.setText(savedPrompt);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newPrompt = etPrompt.getText().toString();
            prefs.edit().putString("custom_prompt", newPrompt).apply();
            android.widget.Toast.makeText(requireContext(), "Behavior Saved!", android.widget.Toast.LENGTH_SHORT)
                    .show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadStats() {
        com.thebizarreabhishek.app.helpers.DatabaseHelper dbHelper = new com.thebizarreabhishek.app.helpers.DatabaseHelper(
                requireContext());
        int count = dbHelper.getMessagesCount();

        binding.tvRepliesCount.setText(String.valueOf(count));

        int totalWords = dbHelper.getTotalWordsCount();
        double secondsSaved = totalWords * 1.25;

        if (secondsSaved < 60) {
            binding.tvSavedTime.setText((int) secondsSaved + "s");
        } else {
            int minutesSaved = (int) (secondsSaved / 60);
            int hours = minutesSaved / 60;
            int mins = minutesSaved % 60;
            if (hours > 0) {
                if (mins > 0)
                    binding.tvSavedTime.setText(hours + "h " + mins + "m");
                else
                    binding.tvSavedTime.setText(hours + "h");
            } else {
                binding.tvSavedTime.setText(mins + "m");
            }
        }
    }

    private void updateServiceStatus() {
        boolean isPermissionGranted = NotificationHelper.isNotificationServicePermissionGranted(requireContext());

        // Also check if "Enabled" in prefs
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean isBotEnabled = prefs.getBoolean("is_bot_enabled", true);

        boolean isRunning = isPermissionGranted && isBotEnabled;

        binding.switchService.setOnCheckedChangeListener(null); // Avoid recursion
        binding.switchService.setChecked(isRunning);

        if (isRunning) {
            binding.tvServiceStatus.setText("Service is\nRunning");
        } else {
            binding.tvServiceStatus.setText("Service is\nStopped");
        }

        binding.switchService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isPermissionGranted) {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                return;
            }
            prefs.edit().putBoolean("is_bot_enabled", isChecked).apply();
            updateServiceStatus();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
