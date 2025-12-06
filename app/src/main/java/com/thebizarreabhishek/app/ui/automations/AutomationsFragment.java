package com.thebizarreabhishek.app.ui.automations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.thebizarreabhishek.app.databinding.FragmentAutomationsBinding;

import android.content.SharedPreferences;

public class AutomationsFragment extends Fragment {

    private FragmentAutomationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAutomationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());

        // WhatsApp (Default True)
        setupToggle(prefs, binding.switchWhatsapp, "is_whatsapp_enabled", true);

        // WhatsApp Business
        setupToggle(prefs, binding.switchWhatsappBusiness, "is_whatsapp_business_enabled", false);

        // Telegram
        setupToggle(prefs, binding.switchTelegram, "is_telegram_enabled", false);

        // Instagram
        setupToggle(prefs, binding.switchInstagram, "is_instagram_enabled", false);

        // Snapchat
        setupToggle(prefs, binding.switchSnapchat, "is_snapchat_enabled", false);

        // Twitter/X
        setupToggle(prefs, binding.switchTwitter, "is_twitter_enabled", false);
    }

    private void setupToggle(SharedPreferences prefs, com.google.android.material.switchmaterial.SwitchMaterial toggle,
            String key, boolean defaultValue) {
        toggle.setChecked(prefs.getBoolean(key, defaultValue));
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(key, isChecked).apply());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
