package com.thebizarreabhishek.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

import com.thebizarreabhishek.app.R;
import com.thebizarreabhishek.app.apis.ChatGPTReplyGenerator;
import com.thebizarreabhishek.app.apis.CustomReplyGenerator;
import com.thebizarreabhishek.app.apis.GeminiReplyGenerator;
import com.thebizarreabhishek.app.helpers.WhatsAppMessageHandler;

public class MyNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "MADARA";
    private final String notificationChannelId = "wa_auto_reply_channel";
    private final Set<String> respondedMessages = new HashSet<>();
    private SharedPreferences sharedPreferences;
    private WhatsAppMessageHandler messageHandler;
    private String botReplyMessage;

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        super.onNotificationPosted(statusBarNotification);

        String packageName = statusBarNotification.getPackageName();
        Log.d(TAG, "onNotificationPosted: packageName=" + packageName);
        boolean isSupported = false;

        if (packageName.equalsIgnoreCase("com.whatsapp") && sharedPreferences.getBoolean("is_whatsapp_enabled", true))
            isSupported = true;
        else if (packageName.equalsIgnoreCase("com.whatsapp.w4b")
                && sharedPreferences.getBoolean("is_whatsapp_business_enabled", false))
            isSupported = true;
        else if (packageName.equals("org.telegram.messenger")
                && sharedPreferences.getBoolean("is_telegram_enabled", false))
            isSupported = true;
        else if (packageName.equals("com.instagram.android")
                && sharedPreferences.getBoolean("is_instagram_enabled", false))
            isSupported = true;
        else if (packageName.equals("com.snapchat.android")
                && sharedPreferences.getBoolean("is_snapchat_enabled", false))
            isSupported = true;
        else if (packageName.equals("com.twitter.android") && sharedPreferences.getBoolean("is_twitter_enabled", false))
            isSupported = true;

        if (isSupported) {

            Bundle extras = statusBarNotification.getNotification().extras;
            String messageId = statusBarNotification.getKey();
            String title = extras.getString(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

            // Check if sender is blocked
            Set<String> blockedContacts = sharedPreferences.getStringSet("blocked_contacts", new HashSet<>());
            if (title != null && blockedContacts.contains(title)) {
                Log.d(TAG, "Sender is blocked: " + title);
                return;
            }

            // Check if we've already responded to this message
            if (respondedMessages.contains(messageId)) {
                return;
            }

            // Add this message to the set of responded messages to avoid looping
            respondedMessages.add(messageId);

            // Process the message and send auto-reply
            if (text != null && !text.toString().isEmpty()) {

                String senderMessage = text.toString();

                boolean isBotEnabled = sharedPreferences.getBoolean("is_bot_enabled", true);
                Log.d(TAG, "onNotificationPosted: isBotEnabled=" + isBotEnabled);
                if (isBotEnabled) {

                    int maxReply = Integer.parseInt(sharedPreferences.getString("max_reply", "100"));

                    messageHandler.getAllMessagesBySender(title, messages -> {

                        if (messages != null && messages.size() < maxReply) {

                            boolean groupReplyEnabled = sharedPreferences.getBoolean("is_group_reply_enabled", false);

                            if (groupReplyEnabled) {
                                processAutoReply(statusBarNotification, title, senderMessage, messageId);
                            } else {
                                if (!isGroupMessage(title)) {
                                    processAutoReply(statusBarNotification, title, senderMessage, messageId);
                                }
                            }
                        }
                    });
                }
            }

            // Clear the set if it reaches size 50 for ram memory free // but no necessary
            // currently
            if (respondedMessages.size() > 50) {
                respondedMessages.clear();
            }
        }
    }

    // ----------------------------------------------------------------------------------------------

    private void finalizeAndSend(Notification.Action action, String sender, String incomingMessage,
            String replyMessageRaw, String messageId) {
        String replyPrefix = sharedPreferences
                .getString("reply_prefix_message", getString(R.string.default_reply_prefix)).trim();
        String botReplyMessage = (replyPrefix + " " + replyMessageRaw).trim();
        String botReplyWithoutPrefix = botReplyMessage.replace(replyPrefix, "").trim();

        long delay = 0;
        if (sharedPreferences.getBoolean("is_natural_delay_enabled", false)) {
            // Natural Delay Algorithm: Reaction Time (1.5-3s) + Typing Time (100-150ms/char)
            long reactionTime = 1500 + new java.util.Random().nextInt(1500);
            long typingTime = botReplyWithoutPrefix.length() * (100L + new java.util.Random().nextInt(50));
            delay = reactionTime + typingTime;
            Log.d(TAG, "finalizeAndSend: Natural Delay=" + delay + "ms (Reaction=" + reactionTime + ", Typing=" + typingTime + ")");
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            messageHandler.handleIncomingMessage(sender, incomingMessage, botReplyWithoutPrefix);
            send(action, botReplyMessage);
            new Handler(Looper.getMainLooper()).postDelayed(() -> respondedMessages.remove(messageId), 750);
        }, delay);
    }

    private void processAutoReply(StatusBarNotification statusBarNotification, String sender, String message,
            String messageId) {

        Notification.Action[] actions = statusBarNotification.getNotification().actions;

        if (actions != null) {

            for (Notification.Action action : actions) {

                // Here is validating sender's message. Not whatsapp checking for messages
                if (action.getRemoteInputs() != null && action.getRemoteInputs().length > 0) {

                    boolean aiConfigured = isAIConfigured();
                    Log.d(TAG, "processAutoReply: isAIConfigured=" + aiConfigured);
                    if (aiConfigured) {

                        String llmModel = sharedPreferences.getString("llm_model", "gpt-4o-mini").toLowerCase();

                        if (llmModel.startsWith("gpt") || llmModel.startsWith("deepseek")
                                || llmModel.startsWith("grok") || llmModel.startsWith("o1")) {
                            ChatGPTReplyGenerator chatGPTReplyGenerator = new ChatGPTReplyGenerator(this,
                                    sharedPreferences, messageHandler);
                            chatGPTReplyGenerator.generateReply(sender, message,
                                    reply -> finalizeAndSend(action, sender, message, reply, messageId));

                        } else if (llmModel.startsWith("custom")) {
                            CustomReplyGenerator customReplyGenerator = new CustomReplyGenerator(this,
                                    sharedPreferences, messageHandler);
                            customReplyGenerator.generateReply(sender, message,
                                    reply -> finalizeAndSend(action, sender, message, reply, messageId));

                        } else if (llmModel.startsWith("gemini")) {
                            GeminiReplyGenerator geminiReplyGenerator = new GeminiReplyGenerator(this,
                                    sharedPreferences, messageHandler);
                            geminiReplyGenerator.generateReply(sender, message,
                                    reply -> finalizeAndSend(action, sender, message, reply, messageId));
                        }

                    } else {
                        String defaultReply = sharedPreferences.getString("default_reply_message",
                                getString(R.string.default_bot_message));
                        finalizeAndSend(action, sender, message, defaultReply, messageId);
                    }

                    break;
                }
            }
        }
    }

    // ----------------------------------------------------------------------------------------------

    private void send(Notification.Action action, String botReplyMessage) {

        RemoteInput remoteInput = action.getRemoteInputs()[0];

        Intent intent = new Intent();

        Bundle bundle = new Bundle();
        bundle.putCharSequence(remoteInput.getResultKey(), botReplyMessage);

        RemoteInput.addResultsToIntent(new RemoteInput[] { remoteInput }, intent, bundle);

        try {
            action.actionIntent.send(this, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "sendAutoReply: ", e);
        }
    }

    // ----------------------------------------------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();

        messageHandler = new WhatsAppMessageHandler(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.ic_smart_toy_black_24dp)
                .setContentTitle("Auto-Reply Active")
                .setContentText("WhatsApp auto-reply is running")
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(1, builder.build());
    }

    // ----------------------------------------------------------------------------------------------

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    notificationChannelId,
                    "Auto Reply Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel for Auto Reply Service");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // ----------------------------------------------------------------------------------------------

    private boolean isGroupMessage(String title) {
        return title != null && title.contains(":");
    }

    // ----------------------------------------------------------------------------------------------

    private boolean isAIConfigured() {
        boolean isAIConfigured = false;
        boolean aiReplyEnabled = sharedPreferences.getBoolean("is_ai_reply_enabled", false);
        String apiKey = sharedPreferences.getString("api_key", "");
        Log.d(TAG, "isAIConfigured: is_ai_reply_enabled=" + aiReplyEnabled + ", api_key_length=" + apiKey.length());
        if (aiReplyEnabled) {
            if (!apiKey.isEmpty()) {
                isAIConfigured = true;
            }
        }
        return isAIConfigured;
    }

    // ----------------------------------------------------------------------------------------------
}