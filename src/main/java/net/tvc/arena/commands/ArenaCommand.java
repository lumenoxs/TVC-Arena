package net.tvc.commands;

import net.tvc.ArenaInstance;
import net.tvc.managers.ConfigMgr;
import net.tvc.utils.ArenaLogic;

import java.util.List;
import java.util.Set;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class ArenaCommand {
    public static void registerCommand() {
        LiteralCommandNode<CommandSourceStack> root = Commands.literal("arena")
            .executes(ctx -> {
                ArenaLogic.command(ctx, "default");
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("start")
                .then(Commands.argument("kit", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        Set<String> kits = ArenaLogic.getKits();
                        String[] args = ctx.getInput().split(" ");
                        Boolean skip = false;
                        if (args.length == 3) {
                            skip = true;
                        }

                        if (ConfigMgr.debug()) {
                            ArenaInstance.getInstance().getLogger().info("Suggesting kits to "+ctx.getSource().getSender().getName());
                            ArenaInstance.getInstance().getLogger().info("Args: "+args);
                        }

                        for (String kit : kits) {
                            if (ConfigMgr.debug()) {
                                ArenaInstance.getInstance().getLogger().info("Kit: "+kit);
                            }

                            if (skip) {
                                builder.suggest(kit);
                                continue;
                            } else if (kit.startsWith(args[3])) {
                                builder.suggest(kit);
                            }
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        ArenaLogic.command(ctx, "start");
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(Commands.literal("join")
                .then(Commands.argument("arena", IntegerArgumentType.integer())
                    .suggests((ctx, builder) -> {
                        List<Integer> arena_ids = ArenaLogic.getAllArenaIds();
                        for (Integer arena_id : arena_ids) {
                            builder.suggest(arena_id);
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        ArenaLogic.command(ctx, "join");
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(Commands.literal("skip")
                .executes(ctx -> {
                    ArenaLogic.command(ctx, "skip");
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("preview")
                .then(Commands.argument("kit", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        Set<String> kits = ArenaLogic.getKits();
                        String[] args = ctx.getInput().split(" ");
                        Boolean skip = false;
                        if (args.length == 3) {
                            skip = true;
                        }

                        if (ConfigMgr.debug()) {
                            ArenaInstance.getInstance().getLogger().info("Suggesting kits to "+ctx.getSource().getSender().getName());
                            ArenaInstance.getInstance().getLogger().info("Args: "+args);
                        }

                        for (String kit : kits) {
                            if (ConfigMgr.debug()) {
                                ArenaInstance.getInstance().getLogger().info("Kit: "+kit);
                            }

                            if (skip) {
                                builder.suggest(kit);
                                continue;
                            } else if (kit.startsWith(args[3])) {
                                builder.suggest(kit);
                            }
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        ArenaLogic.command(ctx, "preview");
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(Commands.literal("kits")
                .executes(ctx -> {
                    ArenaLogic.command(ctx, "kits");
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("help")
                .executes(ctx -> {
                    ArenaLogic.command(ctx, "help");
                    return Command.SINGLE_SUCCESS;
                })
            )
            .build();
        
        ArenaInstance.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(root);
        });
    }
}
