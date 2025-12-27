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
        loadAIStatus();
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

        binding.tvFooter.setOnClickListener(v -> {
            try {
                startActivity(
                        new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/TheBizarreAbhishek")));
            } catch (Exception e) {
                // Ignore
            }
        });
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

    private void loadAIStatus() {
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        
        // Load current AI model
        String modelId = prefs.getString("llm_model", "gemini-2.5-flash");
        String displayName = getModelDisplayName(modelId);
        binding.tvCurrentModel.setText(displayName);
        
        // Check API status
        String apiKey = prefs.getString("api_key", "").trim();
        if (apiKey.isEmpty() || apiKey.equals("not-set")) {
            binding.tvApiStatus.setText("No API Key");
            binding.tvApiStatus.setTextColor(getResources().getColor(R.color.error, null));
            binding.ivApiStatusIcon.setColorFilter(getResources().getColor(R.color.error, null));
        } else {
            // Show configured status
            binding.tvApiStatus.setText("Configured");
            binding.tvApiStatus.setTextColor(getResources().getColor(R.color.success, null));
            binding.ivApiStatusIcon.setColorFilter(getResources().getColor(R.color.success, null));
        }
    }
    
    private String getModelDisplayName(String modelId) {
        switch (modelId) {
            case "gemini-3-pro-preview": return "Gemini 3 Pro";
            case "gemini-3-flash-preview": return "Gemini 3 Flash";
            case "gemini-2.5-pro": return "Gemini 2.5 Pro";
            case "gemini-2.5-flash": return "Gemini 2.5 Flash";
            case "gemini-2.5-flash-lite": return "Gemini 2.5 Lite";
            case "gpt-4o": return "GPT-4o";
            case "gpt-4o-mini": return "GPT-4o Mini";
            case "gpt-4-turbo": return "GPT-4 Turbo";
            case "o3-mini": return "O3 Mini";
            case "deepseek-chat": return "DeepSeek V3";
            case "grok-beta": return "Grok Beta";
            case "custom-gpt-4o": return "Custom API";
            default: return modelId;
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
