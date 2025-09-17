package net.tvc.managers;

import net.tvc.ArenaInstance;

public class ConfigMgr extends Manager {
    public ConfigMgr() {
        super();
    }

    public static Boolean debug() {
        return ArenaInstance.getInstance().getConfig().getBoolean("debug");
    }

    @Override
    public void register() {
        super.register();
        ArenaInstance.getInstance().saveDefaultConfig();
    }

    public static Object get(String path) {
        return ArenaInstance.getInstance().getConfig().get(path);
    }

    public static void set(String path, Object value) {
        ArenaInstance.getInstance().getConfig().set(path, value);
    }
}
