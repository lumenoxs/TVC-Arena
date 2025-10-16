package net.tvc.arena.managers;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.tvc.arena.ArenaInstance;
import net.tvc.arena.commands.ArenaCommand;

public class PluginManager extends Manager {
    @Override
    public void register() {
        super.register();
        
        registerManagers();
        registerCommands();
    }

    private void registerManagers() {
        new ConfigManager().register();
    }

    private void registerCommands() {
         ArenaInstance.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(ArenaCommand.createArenaCommand());
        });
    }
}
