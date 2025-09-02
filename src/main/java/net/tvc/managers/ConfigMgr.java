package net.tvc.managers;

import net.tvc.ArenaInstance;

public class ConfigMgr extends Manager {

    /**
     * @param registerAtStartUp If true, the manager will be registered at startup
     * ConfigMgr is the manager that handles the config.yml file.
     */
    public ConfigMgr(boolean ...registerAtStartUp) {
        super(registerAtStartUp);
    }

    @Override
    public void register() {
        super.register(); // logs the manager name
        ArenaInstance.getInstance().saveDefaultConfig();
    }

    /**
     * Example: getConfig().get("debug") returns the value of debug in config.yml
     * @param path The path to the value
     * @return The value at the path
     */
    public Object get(String path) {
        return ArenaInstance.getInstance().getConfig().get(path);
    }

    /**
     * Example: getConfig().set("debug", true) sets the value of debug in config.yml to true
     * @param path The path to the value
     * @param value The value to set
     */
    public void set(String path, Object value) {
        ArenaInstance.getInstance().getConfig().set(path, value);
    }
}
