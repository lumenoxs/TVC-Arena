package net.tvc.managers;

import net.tvc.commands.ArenaCommand;

public class PluginMgr extends Manager {
    public PluginMgr() {
        super();
    }

    @Override
    public void register() {
        super.register();

        new ConfigMgr().register();
        
        registerManagers();
        registerCommands();
    }

    private void registerManagers() {
        // none
    }

    private void registerCommands() {
        ArenaCommand.registerCommand();
    }
}
