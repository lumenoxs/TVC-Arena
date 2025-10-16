package net.tvc.arena.managers;

import net.tvc.arena.ArenaInstance;

public class ConfigManager extends Manager {
    public static Boolean debug() {
        return ArenaInstance.getInstance().getConfig().getBoolean("debug");
    }

    @Override
    public void register() {
        super.register();
        ArenaInstance.getInstance().saveDefaultConfig();
    }
}
