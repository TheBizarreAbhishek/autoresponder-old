package com.thebizarreabhishek.app.apis;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.thebizarreabhishek.app.R;
import com.thebizarreabhishek.app.helpers.WhatsAppMessageHandler;
import com.thebizarreabhishek.app.models.Message;

public class GeminiReplyGenerator {

    private static final String TAG = "MADARA";
    private final String API_KEY;
    private final String LLM_MODEL;
    private final WhatsAppMessageHandler messageHandler;
    private final String defaultReplyMessage;
    private final String aiReplyLanguage;
    private final String botName;

    private final String customPrompt;

    public GeminiReplyGenerator(Context context, SharedPreferences sharedPreferences,
            WhatsAppMessageHandler whatsAppMessageHandler) {
        this.messageHandler = whatsAppMessageHandler;
        API_KEY = sharedPreferences.getString("api_key", "not-set").trim();
        LLM_MODEL = sharedPreferences.getString("llm_model", "gemini-1.5-flash");
        defaultReplyMessage = sharedPreferences.getString("default_reply_message",
                context.getString(R.string.default_bot_message));
        aiReplyLanguage = sharedPreferences.getString("ai_reply_language", "English");
        botName = sharedPreferences.getString("bot_name", "Yuji");
        customPrompt = sharedPreferences.getString("custom_prompt", "");
    }

    public void generateReply(String sender, String message, CustomReplyGenerator.OnReplyGeneratedListener listener) {

        messageHandler.getMessagesHistory(sender, messages -> {

            StringBuilder chatHistory = getChatHistory(messages);
            StringBuilder prompt = buildPrompt(sender, message, chatHistory);

            GenerativeModel gm = new GenerativeModel(LLM_MODEL, API_KEY);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder()
                    .addText(prompt.toString())
                    .build();

            Executor executor = Executors.newSingleThreadExecutor();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    listener.onReplyGenerated(resultText);
                    Log.d(TAG, "onSuccess: " + resultText);
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    listener.onReplyGenerated(defaultReplyMessage);
                    Log.e(TAG, "onFailure: ", t);
                }
            }, executor);
        });
    }

    // ----------------------------------------------------------------------------------------------

    private @NonNull StringBuilder buildPrompt(String sender, String message, StringBuilder chatHistory) {

        StringBuilder prompt = new StringBuilder();

        if (customPrompt != null && !customPrompt.isEmpty()) {
            prompt.append(customPrompt);
            prompt.append("\n\nMake sure to reply in ").append(aiReplyLanguage).append(".");
        } else {
            String languageInstruction = "Reply in " + aiReplyLanguage + ".";
            if (aiReplyLanguage.equalsIgnoreCase("Hinglish")) {
                languageInstruction = "Reply in Hinglish (a casual blend of Hindi and English using Roman script). Example: 'Haan main thik hu, tum batao?'.";
            }

            prompt.append("IMPORTANT SYSTEM INSTRUCTIONS:\n");
            prompt.append("- Your name is ").append(botName).append(".\n");
            prompt.append(
                    "- You are a WhatsApp auto-reply bot who pretends to be human and doesn't disclose that you are a bot.\n");
            prompt.append(
                    "- Give appropriate replies according to the latest message and the previous chat history.\n");
            prompt.append("- Generate only reply texts. Don't add any other texts.\n");
            prompt.append("- ").append(languageInstruction).append("\n");
            prompt.append(
                    "- Keep replies concise and natural for a chat context. Reply within 1 or 2 sentences whenever possible.\n");
            prompt.append("- Be polite, context-aware, and ensure your replies are relevant to the conversation.\n");
        }

        if (!chatHistory.toString().isEmpty()) {
            prompt.append("\n\n\nPrevious chat history: \n").append(chatHistory);
        }

        prompt.append("\n\n\nMost recent message (from ");
        prompt.append(sender).append("): ");
        prompt.append(message);

        return prompt;
    }

    // ----------------------------------------------------------------------------------------------

    private @NonNull StringBuilder getChatHistory(List<Message> messages) {

        StringBuilder chatHistory = new StringBuilder();

        if (!messages.isEmpty()) {

            for (Message msg : messages) {

                String senderName = msg.getSender();
                String senderMessage = msg.getMessage();
                String senderMessageTimestamp = msg.getTimestamp();
                String myReplyToSenderMessage = msg.getReply();

                chatHistory.append(senderName).append(": ").append(senderMessage);
                chatHistory.append("\n");
                chatHistory.append("Time: ").append(senderMessageTimestamp);
                chatHistory.append("\n");
                chatHistory.append("My reply: ").append(myReplyToSenderMessage);
                chatHistory.append("\n\n");
            }
        }
        return chatHistory;
    }

    // ----------------------------------------------------------------------------------------------
}
