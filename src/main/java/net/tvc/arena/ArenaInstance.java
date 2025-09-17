package net.tvc.arena;

import net.tvc.arena.managers.PluginMgr;

import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaInstance extends JavaPlugin {
    private static ArenaInstance instance;
    private PluginMgr manager;

    @Override
    public void onEnable() {
        instance = this;

        this.manager = new PluginMgr();
        this.manager.register();

        getLogger().info("Enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled.");
    }

    public static ArenaInstance getInstance() {
        return instance;
    }

    public PluginMgr getMgr() {
        return manager;
    }
}
