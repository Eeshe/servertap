package io.servertap.webhooks.models.events;

import com.google.gson.annotations.Expose;

public class WebhookEvent {

    @Expose
    private final String eventName;

    public WebhookEvent(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
