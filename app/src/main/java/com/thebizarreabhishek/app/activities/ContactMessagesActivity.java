package com.thebizarreabhishek.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thebizarreabhishek.app.R;
import com.thebizarreabhishek.app.helpers.DatabaseHelper;
import com.thebizarreabhishek.app.models.Message;
import com.thebizarreabhishek.app.ui.logs.MessagesAdapter;

import java.util.List;

public class ContactMessagesActivity extends AppCompatActivity {

    public static final String EXTRA_SENDER_NAME = "sender_name";
    public static final String EXTRA_PLATFORM = "platform";

    private RecyclerView recyclerMessages;
    private MessagesAdapter messagesAdapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_messages);

        String senderName = getIntent().getStringExtra(EXTRA_SENDER_NAME);
        String platform = getIntent().getStringExtra(EXTRA_PLATFORM);

        dbHelper = new DatabaseHelper(this);

        // Setup views
        TextView tvAvatar = findViewById(R.id.tv_avatar);
        TextView tvName = findViewById(R.id.tv_name);
        ImageView ivPlatform = findViewById(R.id.iv_platform);
        ImageView btnBack = findViewById(R.id.btn_back);
        recyclerMessages = findViewById(R.id.recycler_messages);

        // Set header info
        if (senderName != null && !senderName.isEmpty()) {
            tvAvatar.setText(String.valueOf(senderName.charAt(0)).toUpperCase());
            tvName.setText(senderName);
        }

        // Set platform icon
        ivPlatform.setImageResource(getPlatformIcon(platform));

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        messagesAdapter = new MessagesAdapter();
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(messagesAdapter);

        // Load messages
        loadMessages(senderName);
    }

    private void loadMessages(String senderName) {
        new Thread(() -> {
            List<Message> messages = dbHelper.getAllMessagesBySender(senderName);
            runOnUiThread(() -> messagesAdapter.setMessages(messages));
        }).start();
    }

    private int getPlatformIcon(String platform) {
        if (platform == null) return R.drawable.ic_smart_toy_black_24dp;
        
        switch (platform) {
            case "whatsapp":
                return R.drawable.ic_whatsapp;
            case "whatsapp_business":
                return R.drawable.ic_whatsapp_business;
            case "telegram":
                return R.drawable.ic_telegram;
            case "instagram":
                return R.drawable.ic_instagram;
            case "snapchat":
                return R.drawable.ic_snapchat;
            case "twitter":
                return R.drawable.ic_twitter_x;
            default:
                return R.drawable.ic_smart_toy_black_24dp;
        }
    }
}
