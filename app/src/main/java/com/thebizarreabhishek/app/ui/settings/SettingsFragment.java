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
        setupGeneralSettings();
        setupAIEngine();
        setupAPIKey();
    }

    private void setupThemeToggle() {
        // Mock implementation for now
        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Apply theme logic
        });
    }

    private void setupGeneralSettings() {
        // Default Reply
        String defaultReply = prefs.getString("default_reply_message", "I am busy right now, will talk to you later.");
        binding.tvDefaultReply.setText(defaultReply);
        binding.containerDefaultReply.setOnClickListener(v -> showEditTextDialog("Default Reply Message",
                "default_reply_message", defaultReply, binding.tvDefaultReply));

        // Group Reply
        boolean isGroupEnabled = prefs.getBoolean("is_group_reply_enabled", false);
        binding.switchGroupReply.setChecked(isGroupEnabled);
        binding.switchGroupReply.setOnCheckedChangeListener(
                (buttonView, isChecked) -> prefs.edit().putBoolean("is_group_reply_enabled", isChecked).apply());

        // Prefix
        String prefix = prefs.getString("reply_prefix_message", "[Bot]");
        binding.tvReplyPrefix.setText(prefix);
        binding.containerReplyPrefix.setOnClickListener(
                v -> showEditTextDialog("Reply Prefix", "reply_prefix_message", prefix, binding.tvReplyPrefix));

        // Max Reply
        String maxReply = prefs.getString("max_reply", "100");
        binding.tvMaxReply.setText(maxReply);
        binding.containerMaxReply.setOnClickListener(
                v -> showEditTextDialog("Max Reply Per Person", "max_reply", maxReply, binding.tvMaxReply));
    }

    private void showEditTextDialog(String title, String key, String currentValue, android.widget.TextView updateView) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentValue);

        // For Max Reply, stick to number input if preferred, but existing code used
        // String "100" so text is fine.
        if (key.equals("max_reply"))
            input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = input.getText().toString();
                    prefs.edit().putString(key, newValue).apply();
                    if (updateView != null)
                        updateView.setText(newValue);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        binding.btnCopyKey.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("API Key",
                    prefs.getString("api_key", ""));
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(requireContext(), "Copied to clipboard", android.widget.Toast.LENGTH_SHORT)
                    .show();
        });

        setupBotIdentity();
    }

    private void setupBotIdentity() {
        // Bot Name
        String botName = prefs.getString("bot_name", "Abhishek Babu");
        binding.tvBotNameSetting.setText(botName);
        binding.containerBotName.setOnClickListener(
                v -> showEditTextDialog("Bot Name", "bot_name", botName, binding.tvBotNameSetting));

        // Bot Language
        String currentLanguage = prefs.getString("bot_language", "English");
        binding.tvBotLanguageSetting.setText(currentLanguage + " >");
        binding.containerBotLanguage.setOnClickListener(v -> {
            String[] languages = getResources().getStringArray(R.array.languages);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select Bot Language")
                    .setItems(languages, (dialog, which) -> {
                        String selectedLanguage = languages[which];
                        binding.tvBotLanguageSetting.setText(selectedLanguage + " >");
                        prefs.edit().putString("bot_language", selectedLanguage).apply();
                    })
                    .show();
        });
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
