package io.servertap.webhooks;

import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.ItemStack;
import io.servertap.api.v1.models.Player;
import io.servertap.utils.JSONUtil;
import io.servertap.webhooks.managers.WebhookManager;
import io.servertap.webhooks.models.events.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class WebhookEventListener implements Listener {
    private final Logger log;
    private final WebhookManager webhookManager;
    private final ServerTapMain main;

    public WebhookEventListener(ServerTapMain main, WebhookManager webhookManager, Logger logger) {
        this.main = main;
        this.webhookManager = webhookManager;
        this.log = logger;
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

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlayerChatWebhookEvent eventModel = new PlayerChatWebhookEvent();

        eventModel.setPlayer(JSONUtil.fromBukkitPlayer(event.getPlayer()));
        eventModel.setMessage(normalizeMessage(event.getMessage()));
        eventModel.setPlayerName(event.getPlayer().getDisplayName());

        webhookManager.broadcastEvent(eventModel, event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerDeathWebhookEvent eventModel = new PlayerDeathWebhookEvent();

        Player player = JSONUtil.fromBukkitPlayer(event.getEntity());
        List<ItemStack> drops = new ArrayList<>();
        event.getDrops().forEach(itemStack -> drops.add(fromBukkitItemStack(itemStack)));

        eventModel.setPlayer(player);
        eventModel.setDrops(drops);
        eventModel.setDeathMessage(normalizeMessage(event.getDeathMessage()));

        webhookManager.broadcastEvent(eventModel, event);
    }

    private ItemStack fromBukkitItemStack(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack i = new ItemStack();
        i.setId("minecraft:" + itemStack.getType().toString().toLowerCase());
        i.setCount(itemStack.getAmount());
        i.setSlot(-1);
        return i;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerJoinWebhookEvent eventModel = new PlayerJoinWebhookEvent();

        Player player = JSONUtil.fromBukkitPlayer(event.getPlayer());

        eventModel.setPlayer(player);
        eventModel.setJoinMessage(normalizeMessage(event.getJoinMessage()));

        webhookManager.broadcastEvent(eventModel, event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerQuitWebhookEvent eventModel = new PlayerQuitWebhookEvent();

        Player player = JSONUtil.fromBukkitPlayer(event.getPlayer());

        eventModel.setPlayer(player);
        eventModel.setQuitMessage(normalizeMessage(event.getQuitMessage()));

        webhookManager.broadcastEvent(eventModel, event);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        PlayerKickWebhookEvent eventModel = new PlayerKickWebhookEvent();

        Player player = JSONUtil.fromBukkitPlayer(event.getPlayer());

        eventModel.setPlayer(player);
        eventModel.setReason(normalizeMessage(event.getReason()));

        webhookManager.broadcastEvent(eventModel, event);
    }

    private String normalizeMessage(String message) {
        try {
            if (!this.main.getConfig().getBoolean("normalizeMessages")) {
                return message;
            }
            return ChatColor.stripColor(message);
        } catch (Exception e) {
            return message;
        }
    }
}
