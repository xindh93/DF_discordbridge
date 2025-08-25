package dev.dfbridge;

import dev.dfbridge.listeners.AnnounceListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiscordBridgePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("Enabling Discord bridge...");

        // Warn if DiscordSRV is not present (we use reflection at runtime)
        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") == null) {
            getLogger().warning("DiscordSRV not found. Messages won't be delivered until it's installed/enabled.");
        }

        // Register event listener
        Bukkit.getPluginManager().registerEvents(new AnnounceListener(this), this);

        getLogger().info("Discord bridge ready.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Discord bridge disabled.");
    }
}
