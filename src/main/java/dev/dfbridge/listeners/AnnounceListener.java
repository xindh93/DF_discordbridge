package dev.dfbridge.listeners;

import dev.dfbridge.DiscordBridgePlugin;
import dev.dfbridge.events.DFAnnounceEvent;
import dev.dfbridge.util.MessageFormatter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public final class AnnounceListener implements Listener {
    private final DiscordBridgePlugin plugin;

    public AnnounceListener(DiscordBridgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnnounce(DFAnnounceEvent event) {
        FileConfiguration cfg = plugin.getConfig();
        String channelId = cfg.getString("discord.channel-id", "");
        if (channelId == null || channelId.isEmpty()) {
            plugin.getLogger().warning("discord.channel-id is not set.");
            return;
        }

        // Resolve template by type path: messages.<typePath>.template
        String[] parts = event.getType().split("\\.");
        String template = null;
        ConfigurationSection msg = cfg.getConfigurationSection("messages");
        if (msg != null) {
            ConfigurationSection cursor = msg;
            for (String p : parts) {
                cursor = cursor.getConfigurationSection(p);
                if (cursor == null) break;
            }
            if (cursor != null) {
                template = cursor.getString("template");
            }
            if (template == null) {
                ConfigurationSection def = msg.getConfigurationSection("default");
                if (def != null) {
                    template = def.getString("template", "{type} {player}");
                }
            }
        }
        if (template == null) {
            template = "{type} {player}";
        }

        Map<String, String> data = event.getData();
        ConfigurationSection roles = cfg.getConfigurationSection("roles");
        String result = MessageFormatter.render(template, data, roles);

        // Apply auto @everyone prefix based on glob patterns
        List<String> patterns = cfg.getStringList("messages.prefix-groups.everyone.matches");
        result = MessageFormatter.applyAutoPrefixForGroups(event.getType(), result, patterns);

        sendToDiscord(channelId, result);
    }

    private void sendToDiscord(String channelId, String content) {
        try {
            Class<?> dsrvClass = Class.forName("org.discordsrv.discordsrv.DiscordSRV");
            Method getPlugin = dsrvClass.getMethod("getPlugin");
            Object dsrv = getPlugin.invoke(null);
            if (dsrv == null) {
                plugin.getLogger().warning("DiscordSRV is not ready.");
                return;
            }
            Method getJda = dsrv.getClass().getMethod("getJda");
            Object jda = getJda.invoke(dsrv);
            if (jda == null) {
                plugin.getLogger().warning("DiscordSRV JDA is not ready.");
                return;
            }
            Method getMainGuild = dsrv.getClass().getMethod("getMainGuild");
            Object guild = getMainGuild.invoke(dsrv);
            if (guild == null) {
                plugin.getLogger().warning("DiscordSRV getMainGuild() returned null.");
                return;
            }
            Method getTextChannelById = guild.getClass().getMethod("getTextChannelById", long.class);
            Object channel = getTextChannelById.invoke(guild, Long.parseLong(channelId));
            if (channel == null) {
                plugin.getLogger().warning("Channel not found: " + channelId);
                return;
            }
            Method sendMessage = channel.getClass().getMethod("sendMessage", CharSequence.class);
            Object action = sendMessage.invoke(channel, content);
            Method queue = action.getClass().getMethod("queue");
            queue.invoke(action);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("DiscordSRV class not found. Is the jar installed?");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send message via DiscordSRV: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
