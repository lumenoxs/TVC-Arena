package net.tvc.arena.managers;

import net.tvc.arena.ArenaInstance;

public class Manager {
    public void register() {
        ArenaInstance.getInstance().getLogger().info("[Manager] Registered: " + getClass().getSimpleName());
    }
}
