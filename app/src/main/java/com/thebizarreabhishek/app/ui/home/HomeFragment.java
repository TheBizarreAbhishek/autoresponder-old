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
    }

    private void loadCustomPrompt() {
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        String savedPrompt = prefs.getString("custom_prompt", "You are a helpful AI assistant.");
        binding.etCustomPrompt.setText(savedPrompt);

        binding.btnSavePrompt.setOnClickListener(v -> {
            String newPrompt = binding.etCustomPrompt.getText().toString();
            prefs.edit().putString("custom_prompt", newPrompt).apply();
            android.widget.Toast.makeText(requireContext(), "Behavior Saved!", android.widget.Toast.LENGTH_SHORT)
                    .show();
        });
    }

    private void loadStats() {
        com.thebizarreabhishek.app.helpers.DatabaseHelper dbHelper = new com.thebizarreabhishek.app.helpers.DatabaseHelper(
                requireContext());
        int count = dbHelper.getMessagesCount();

        binding.tvRepliesCount.setText(String.valueOf(count));

        // Assume 2 minutes saved per reply as an estimate
        int minutesSaved = count * 2;
        if (minutesSaved < 60) {
            binding.tvSavedTime.setText(minutesSaved + "m");
        } else {
            int hours = minutesSaved / 60;
            int mins = minutesSaved % 60;
            if (mins == 0)
                binding.tvSavedTime.setText(hours + "h");
            else
                binding.tvSavedTime.setText(hours + "h " + mins + "m");
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
