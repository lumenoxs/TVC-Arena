package net.tvc.managers;

import net.tvc.ArenaInstance;

public class ConfigMgr extends Manager {
    public ConfigMgr(boolean ...registerAtStartUp) {
        super(registerAtStartUp);
    }

    @Override
    public void register() {
        super.register();
        ArenaInstance.getInstance().saveDefaultConfig();
    }

    public Object get(String path) {
        return ArenaInstance.getInstance().getConfig().get(path);
    }

    public void set(String path, Object value) {
        ArenaInstance.getInstance().getConfig().set(path, value);
    }
}
