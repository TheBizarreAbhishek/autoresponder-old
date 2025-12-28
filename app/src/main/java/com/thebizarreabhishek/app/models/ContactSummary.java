package com.thebizarreabhishek.app.models;

public class ContactSummary {
    private String senderName;
    private String lastMessage;
    private String lastTimestamp;
    private int messageCount;
    private String platform;

    public ContactSummary(String senderName, String lastMessage, String lastTimestamp, int messageCount, String platform) {
        this.senderName = senderName;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
        this.messageCount = messageCount;
        this.platform = platform;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastTimestamp() {
        return lastTimestamp;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getPlatform() {
        return platform;
    }
}
