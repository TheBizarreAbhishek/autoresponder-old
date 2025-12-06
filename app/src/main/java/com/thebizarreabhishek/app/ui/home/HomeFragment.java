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
