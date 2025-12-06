package com.thebizarreabhishek.app.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.thebizarreabhishek.app.databinding.FragmentSettingsBinding;
import com.thebizarreabhishek.app.R;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences prefs;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        setupThemeToggle();
        setupBatteryOptimization();
        setupAIEngine();
        setupAPIKey();
    }

    private void setupThemeToggle() {
        // Mock implementation for now
        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Apply theme logic
        });
    }

    private void setupBatteryOptimization() {
        binding.btnBatteryIgnore.setOnClickListener(v -> {
            // Ignore battery optimization intent
        });
    }

    private void setupAIEngine() {
        String currentModel = prefs.getString("llm_model", "gpt-4o");
        binding.tvSelectedEngine.setText(currentModel + " >");

        // Load model names from resources or hardcode for now based on legacy XML
        final String[] models = { "gpt-3.5-turbo", "gpt-4", "gpt-4o", "gemini-1.5-flash", "claude-3-sonnet" };

        binding.tvSelectedEngine.getRootView().findViewById(R.id.tv_selected_engine).getParent().requestLayout(); // Force
                                                                                                                  // refresh?
                                                                                                                  // No,
                                                                                                                  // just
                                                                                                                  // set
                                                                                                                  // listener
                                                                                                                  // on
                                                                                                                  // parent
        ((View) binding.tvSelectedEngine.getParent()).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select AI Engine")
                    .setItems(models, (dialog, which) -> {
                        String selected = models[which];
                        prefs.edit().putString("llm_model", selected).apply();
                        binding.tvSelectedEngine.setText(selected + " >");
                    })
                    .show();
        });
    }

    private void setupAPIKey() {
        String currentKey = prefs.getString("api_key", "");
        updateKeyDisplay(currentKey);

        ((View) binding.tvApiKeyMasked.getParent()).setOnClickListener(v -> showEditKeyDialog());
        binding.btnToggleKeyVisibility.setOnClickListener(v -> toggleKeyVisibility());
    }

    private boolean isKeyVisible = false;

    private void updateKeyDisplay(String key) {
        if (key.isEmpty()) {
            binding.tvApiKeyMasked.setText("Not Set");
            return;
        }

        if (isKeyVisible) {
            binding.tvApiKeyMasked.setText(key);
            binding.btnToggleKeyVisibility.setImageResource(R.drawable.ic_visibility); // Needs off icon really
        } else {
            if (key.length() > 8) {
                binding.tvApiKeyMasked.setText(key.substring(0, 4) + "...." + key.substring(key.length() - 4));
            } else {
                binding.tvApiKeyMasked.setText("********");
            }
            binding.btnToggleKeyVisibility.setImageResource(R.drawable.ic_visibility);
        }
    }

    private void toggleKeyVisibility() {
        isKeyVisible = !isKeyVisible;
        updateKeyDisplay(prefs.getString("api_key", ""));
    }

    private void showEditKeyDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setText(prefs.getString("api_key", ""));

        new AlertDialog.Builder(requireContext())
                .setTitle("Enter API Key")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newKey = input.getText().toString();
                    prefs.edit().putString("api_key", newKey).apply();
                    updateKeyDisplay(newKey);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
