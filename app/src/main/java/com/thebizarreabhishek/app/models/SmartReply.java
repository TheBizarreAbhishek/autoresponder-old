package com.thebizarreabhishek.app.models;

public class SmartReply {
    private int id;
    private String trigger;
    private String response;
    private boolean enabled;

    public SmartReply(int id, String trigger, String response, boolean enabled) {
        this.id = id;
        this.trigger = trigger;
        this.response = response;
        this.enabled = enabled;
    }

    public SmartReply(String trigger, String response) {
        this.trigger = trigger;
        this.response = response;
        this.enabled = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
