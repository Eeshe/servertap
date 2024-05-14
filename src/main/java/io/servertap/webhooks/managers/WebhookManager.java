package io.servertap.webhooks.managers;

import com.google.gson.Gson;
import io.servertap.ServerTapMain;
import io.servertap.utils.GsonSingleton;
import io.servertap.webhooks.Webhook;
import io.servertap.webhooks.models.events.WebhookEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class WebhookManager {
    private final Logger log;
    private final ServerTapMain main;
    private List<Webhook> webhooks;

    public WebhookManager(ServerTapMain main, FileConfiguration bukkitConfig, Logger logger) {
        this.main = main;
        this.log = logger;

        loadWebhooksFromConfig(bukkitConfig);
    }

    private static void sendHttpRequest(WebhookEvent eventModel, Webhook webhook) {
        try {
            Gson gson = GsonSingleton.getInstance();
            String jsonContent = gson.toJson(eventModel);
            byte[] output = jsonContent.getBytes(StandardCharsets.UTF_8);

            URL url = new URL(webhook.getListenerUrl());
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setFixedLengthStreamingMode(output.length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(output);
            }
        } catch (MalformedURLException ignored) {
            //This branch should never be reached, since all listeners are validated in the constructor
        } catch (IOException ignored) {
        }
    }

    public void loadWebhooksFromConfig(FileConfiguration bukkitConfig) {
        webhooks = getWebhooksFromConfig(bukkitConfig);
    }

    private List<Webhook> getWebhooksFromConfig(FileConfiguration bukkitConfig) {
        final List<Webhook> configWebhooks = new ArrayList<>();

        ConfigurationSection webhookSection = bukkitConfig.getConfigurationSection("webhooks");
        if (webhookSection == null) {
            return configWebhooks;
        }

        Set<String> webhookNames = webhookSection.getKeys(false);

        for (String webhookName : webhookNames) {
            String configPath = "webhooks." + webhookName + ".";

            Webhook.getWebhookFromConfig(bukkitConfig, webhookName, configPath, log)
                    .ifPresent(configWebhooks::add);
        }
        return configWebhooks;
    }

    public void broadcastEvent(WebhookEvent eventModel, Event event) {
        broadcastEvent(eventModel, event.getEventName());
    }

    public void broadcastEvent(WebhookEvent eventModel, String eventName) {
        for (Webhook webhook : webhooks) {
            List<String> registeredEvents = webhook.getRegisteredEvents();

            if (!registeredEvents.contains(eventName)) {
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(main, () -> sendHttpRequest(eventModel, webhook));
        }
    }
}
