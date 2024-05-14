package io.servertap.webhooks;

import org.bukkit.configuration.file.FileConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Webhook {
    private String listenerUrl;
    private List<String> registeredEvents;

    public Webhook(String listenerUrl, List<String> registeredEvents) {
        setListenerUrl(listenerUrl);
        setRegisteredEvents(registeredEvents);
    }

    public String getListenerUrl() {
        return listenerUrl;
    }

    public void setListenerUrl(String listenerUrl) {
        this.listenerUrl = listenerUrl;
    }

    public static Optional<Webhook> getWebhookFromConfig(FileConfiguration bukkitConfig, String webhookName, String configPath, Logger log) {
        // Check for listener parameter and validate it
        String listenerUrl = bukkitConfig.getString(configPath + "listener");
        if (listenerUrl == null) {
            log.warning(String.format("[ServerTap] Warning: webhook '%s' doesn't have 'listener' set", webhookName));
            return Optional.empty();
        }

        try {
            new URL(listenerUrl);
        } catch (MalformedURLException ex) {
            log.warning(String.format("[ServerTap] Warning: webhook '%s' url is invalid", webhookName));
            return Optional.empty();
        }

        // Check for events parameter
        if (!bukkitConfig.isSet(configPath + "events")) {
            log.warning(String.format("[ServerTap] Warning: webhook '%s' doesn't have 'events' set", webhookName));
            return Optional.empty();
        }

        List<String> events = getWebhookEvents(webhookName, configPath, bukkitConfig, log);
        return events.isEmpty() ? Optional.empty() : Optional.of(new Webhook(listenerUrl, events));
    }

    private static List<String> getWebhookEvents(String webhookName, String configPath, FileConfiguration bukkitConfig, Logger log) {
        List<String> events = new ArrayList<>();
        List<String> configEvents = bukkitConfig.getStringList(configPath + "events");

        // If the events path can't be interpreted as a list, try as a single string
        if (configEvents.isEmpty()) {
            String singleEvent = bukkitConfig.getString(configPath + "events");

            if (singleEvent != null) {
                configEvents.add(singleEvent);
            } else {
                log.warning(String.format("[ServerTap] Warning: webhook \"%s\" doesn't register any events", webhookName));
                return events;
            }
        }

        for (String eventName : configEvents) {
            if (events.contains(eventName)) {
                log.warning(String.format("[ServerTap] Warning: webhook '%s' registers duplicate event '%s'", webhookName, eventName));
                continue;
            }
            events.add(eventName);
        }
        return events;
    }

    public List<String> getRegisteredEvents() {
        return registeredEvents;
    }

    public void setRegisteredEvents(List<String> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }
}