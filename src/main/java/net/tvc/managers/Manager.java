package net.tvc.managers;

import net.tvc.ArenaInstance;

public class Manager {
    protected boolean initialized = false;

    public Manager() {
        this.initialized = true;
    }

    public void register() {
        ArenaInstance.getInstance().getLogger().info("[Manager] Registered: " + name());
    }

    public final String name() {
        return getClass().getSimpleName();
    }
}
