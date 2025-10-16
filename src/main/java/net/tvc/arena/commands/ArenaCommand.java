package net.tvc.arena.commands;

import net.tvc.arena.ArenaInstance;
import net.tvc.arena.managers.ConfigManager;
import net.tvc.arena.utils.ArenaLogic;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class ArenaCommand {
    public static CompletableFuture<Suggestions> suggestKits(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Set<String> kits = ArenaLogic.getKits();
        String[] args = ctx.getInput().toString().split(" ");
        Boolean skip = false;
        if (args.length == 2) {
            skip = true;
        }

        if (ConfigManager.debug()) {
            ArenaInstance.getInstance().getLogger().info("Suggesting kits to "+ctx.getSource().getSender().getName());
            ArenaInstance.getInstance().getLogger().info("Args: "+Arrays.toString(args));
            ArenaInstance.getInstance().getLogger().info("Args length: "+args.length);
        }

        for (String kit : kits) {
            if (ConfigManager.debug()) {
                ArenaInstance.getInstance().getLogger().info("Kit: "+kit);
            }

            if (skip) {
                builder.suggest(kit);
                continue;
            } else if (kit.startsWith(args[2].toUpperCase())) {
                builder.suggest(kit);
            }
        }
        
        return builder.buildFuture();
    }

    public static LiteralCommandNode<CommandSourceStack> createArenaCommand() {
        LiteralCommandNode<CommandSourceStack> root = Commands.literal("arena")
            .executes(ctx -> {
                ArenaLogic.command(ctx, "default");
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("start")
                .then(Commands.argument("kit", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        return suggestKits(ctx, builder);
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
                        for (Integer arena_id : arena_ids) builder.suggest(arena_id);

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
                        return suggestKits(ctx, builder);
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
        
        return root;
    }
}
