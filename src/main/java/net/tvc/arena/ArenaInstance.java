package net.tvc.arena;

import net.tvc.arena.managers.PluginMgr;
import net.tvc.arena.utils.ArenaLogic;

import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaInstance extends JavaPlugin {
    private static ArenaInstance instance;
    private PluginMgr manager;
    private ArenaLogic arenaLogicListener = new ArenaLogic();

    @Override
    public void onEnable() {
        instance = this;

        this.manager = new PluginMgr();
        this.manager.register();

        getServer().getPluginManager().registerEvents(arenaLogicListener, this);

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
