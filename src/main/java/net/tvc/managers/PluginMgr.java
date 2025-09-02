package net.tvc.managers;

import net.tvc.ArenaInstance;
import net.tvc.classes.Match;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.io.File;
import java.util.*;

public class PluginMgr extends Manager {
    private final List<Match> matches = new ArrayList<>();
    private ConfigMgr configManager;

    public PluginMgr(boolean... registerAtStartUp) {
        super(registerAtStartUp);
    }

    @SuppressWarnings("deprecation")
    public void command(CommandContext<CommandSourceStack> ctx, String label) {
        CommandSender sender = ctx.getSource().getSender();
        String ctxCommand = ctx.getInput();
        String[] args = ctxCommand.substring(6).split(" ");

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§eStart a new match with:\n§a/arena start <kit>\n§eJoin a current match with:\n§a/arena join [match]\n§eSkip the countdown for the match you are in:\n§a/arena skip\n§ePreview a kit:\n§a/arena preview <kit>\n§eSee a list of kits:\n§a/arena kits");
            return;
        }
        
        Player player = (Player) sender;
        UUID puuid = player.getUniqueId();
        Match playerMatch = getPlayerMatch(puuid);
        Set<String> kits = getKits();

        switch (label.toLowerCase()) {
            case "start" -> {
                if (playerMatch != null) {
                    sender.sendMessage("§cYou're already in a match!");
                    return;
                }
                
                if (args.length < 2) {
                    sender.sendMessage("§eYou need a <kit> parameter! /arena start <kit>");
                    return;
                }
                
                if (!kits.contains(args[1])) {
                    sender.sendMessage("§cThats not a valid kit! Use /arena kits to list all the kits, preview them with /arena preview <kit>, and start a new match with /arena start <kit>.");
                    return;
                }
                
                Match newMatch = new Match(
                    true, new ArrayList<>(List.of(puuid)), null, args[1],
                    matches.size() + 1, 10 - matches.size()
                );

                matches.add(newMatch);
                sender.sendMessage("§aYou have started a new match with kit " + args[1] + "! Tell others to run /arena join " + matches.size() + " to join your match.");
                Bukkit.broadcastMessage("§bA new match with kit "+args[1]+" has started! Use /arena join " + matches.size() + " to enter.");
                countdownMatch(newMatch);
                return;
            }
            
            case "kits" -> {
                sender.sendMessage("§bHere is a list of the kits:\n§a"+String.join("§7, §a", kits)+"\n§bYou can preview a kit with /arena preview <kit>");
                return;
            }
            
            case "preview" -> {
                if (args.length < 2) {
                    sender.sendMessage("§eYou need a <kit> parameter! /arena preview <kit>");
                    return;
                }
                
                if (!kits.contains(args[1])) {
                    sender.sendMessage("§cThats not a valid kit! Use /arena kits to list all the kits, preview them with /arena preview <kit>, and start a new match with /arena start <kit>.");
                    return;
                }
                
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                String command = "kit preview "+args[1]+" "+player.getName();
                Bukkit.dispatchCommand(console, command);
                return;
            }
            
            case "join" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can join matches!");
                    return;
                }

                if (getPlayerMatch(puuid) != null) {
                    sender.sendMessage("§cYou're already in a match!");
                    return;
                }

                Match matchToJoin = null;

                if (args.length == 2) {
                    // player provided a match id
                    try {
                        int matchId = Integer.parseInt(args[1]);
                        matchToJoin = getMatchById(matchId);
                        if (matchToJoin == null || !matchToJoin.isQueueing()) {
                            sender.sendMessage("§cMatch #" + matchId + " does not exist or has already started.");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid match ID!");
                        return;
                    }
                } else {
                    // no id provided → join the active queue
                    matchToJoin = getActiveQueue();
                    if (matchToJoin == null) {
                        sender.sendMessage("§cThere is no queueing match to join.");
                        return;
                    }
                }

                matchToJoin.addPlayer(puuid);
                Bukkit.broadcastMessage(sender.getName() + " joined match #" + matchToJoin.getMatchId() + "!");
                return;
            }

            case "skip" -> {
                if (playerMatch == null) {
                    sender.sendMessage("§cYou aren't in a match!");
                    return;
                }
                if (!playerMatch.isQueueing()) {
                    sender.sendMessage("§cYour match has already started!");
                    return;
                }

                Bukkit.broadcastMessage(sender.getName() + " skipped the countdown for match #" + playerMatch.getMatchId() + "!");
                startMatch(playerMatch);
                return;
            }
            
            case "help" -> {
                sender.sendMessage("§eStart a new match with:\n§a/arena start <kit>\n§eJoin a current match with:\n§a/arena join [match]\n§eSkip the countdown for the match you are in:\n§a/arena skip\n§ePreview a kit:\n§a/arena preview <kit>\n§eSee a list of kits:\n§a/arena kits");
                return;
            }
            default -> {
                sender.sendMessage("§eStart a new match with:\n§a/arena start <kit>\n§eJoin a current match with:\n§a/arena join [match]\n§eSkip the countdown for the match you are in:\n§a/arena skip\n§ePreview a kit:\n§a/arena preview <kit>\n§eSee a list of kits:\n§a/arena kits");
                return;
            }
        }

    }

    @Override
    public void register() {
        super.register();

        new ConfigMgr().register();
        
        registerManagers();
        LiteralCommandNode<CommandSourceStack> arenaRoot = registerCommands();
        ArenaInstance.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(arenaRoot);
        });
    }

    private void registerManagers() {
        configManager = new ConfigMgr();
    }

    private LiteralCommandNode<CommandSourceStack> registerCommands() {
        LiteralCommandNode<CommandSourceStack> root = Commands.literal("arena")
            .then(Commands.literal("start")
                .then(Commands.argument("kit", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        Set<String> kits = getKits();
                        for (String kit : kits) {
                            builder.suggest(kit);
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        command(ctx, "start");
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(Commands.literal("join")
                .then(Commands.argument("arena", IntegerArgumentType.integer())
                    .suggests((ctx, builder) -> {
                        List<Integer> arena_ids = getAllArenaIds();
                        for (Integer arena_id : arena_ids) {
                            builder.suggest(arena_id);
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        command(ctx, "join");
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(Commands.literal("skip")
                .executes(ctx -> {
                    command(ctx, "skip");
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("preview")
                .then(Commands.argument("kit", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        Set<String> kits = getKits();
                        for (String kit : kits) {
                            builder.suggest(kit);
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        command(ctx, "preview");
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(Commands.literal("kits")
                .executes(ctx -> {
                    command(ctx, "kits");
                    return Command.SINGLE_SUCCESS;
                }))
            .build();
            
        return root;
    }

    public Set<String> getKits() {
        Set<String> kits = new HashSet<>();
        File dir = new File(ArenaInstance.getInstance().getDataFolder().getParent(), "PlayerKits2/kits");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                String kname = child.getName().replace(".yml", "");
                kits.add(kname);
            }
        } else {
            String errornoo = "Yea it doesnt exist";
            kits.add(errornoo);
            kits.add(ArenaInstance.getInstance().getDataFolder().getParent()+"PlayerKits2/kits");
        }
        return kits;
    }

    public ConfigMgr getConfigMgr() {
        return configManager;
    }

    public Match getPlayerMatch(UUID uuid) {
        for (Match m : matches) {
            if (m.getPlayers().contains(uuid)) {
                return m;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getAllArenaIds() {
        List<Map<String, Object>> arenas = (List<Map<String, Object>>) configManager.get("arenas");
        List<Integer> ids = new ArrayList<>();
        if (arenas != null) {
            for (Map<String, Object> arena : arenas) {
                Object id = arena.get("id");
                if (id instanceof Integer intId) {
                    ids.add(intId);
                }
            }
        }
        return ids;
    }
    
    @SuppressWarnings("unchecked")
    private List<Location> getArenaRooms(Integer arenaId) {
        List<Map<String, Object>> arenas = (List<Map<String, Object>>) configManager.get("arenas");
        List<Location> locations = new ArrayList<>();

        if (arenaId >= arenas.size()) return locations;
        Map<String, Object> arenaData = arenas.get(arenaId);

        List<Map<String, Object>> rooms = (List<Map<String, Object>>) arenaData.get("rooms");
        if (rooms == null) return locations;

        for (Map<String, Object> room : rooms) {
            String worldName = (String) room.get("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            double x = ((Number) room.get("x")).doubleValue();
            double y = ((Number) room.get("y")).doubleValue();
            double z = ((Number) room.get("z")).doubleValue();
            float yaw = room.containsKey("yaw") ? ((Number) room.get("yaw")).floatValue() : 0f;
            float pitch = room.containsKey("pitch") ? ((Number) room.get("pitch")).floatValue() : 0f;

            locations.add(new Location(world, x, y, z, yaw, pitch));
        }

        return locations;
    }

    @SuppressWarnings("deprecation")
    public void endMatch(Match match) {
        matches.remove(match);
        match.setArena(null);
        Bukkit.broadcastMessage("§aMatch #" + match.getMatchId() + " has ended.");
    }


    public Integer findFreeArena() {
        List<Integer> allArenas = getAllArenaIds();

        for (Integer arenaId : allArenas) {
            boolean taken = false;
            for (Match m : matches) {
                if (!m.isQueueing() && m.getArena() != null && m.getArena().equals(arenaId)) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                return arenaId;
            }
        }
        return null;
    }

    public Match getActiveQueue() {
        return matches.stream()
                .filter(Match::isQueueing)
                .max(Comparator.comparingInt(Match::getPriority))
                .orElse(null);
    }

    private Match findMatch(UUID uuid) {
        for (Match match : matches) {
            if (match.containsPlayer(uuid)) {
                return match;
            }
        }
        return null;
    }


    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        Match currentMatch = findMatch(uuid);
        if (currentMatch == null) return;

        currentMatch.markDead(uuid);

        player.getWorld().strikeLightningEffect(player.getLocation());

        int place = currentMatch.getPlacement(uuid);

        String original = event.getDeathMessage();
        if (original == null) {
            original = player.getName() + " died";
        }
        if (place > 2) {
            event.setDeathMessage(original + " and got #" + place);
        } else {
            event.setDeathMessage(original + " and got #" + place);
            List<UUID> alive = new ArrayList<>(currentMatch.getPlayers());
            alive.removeAll(currentMatch.getDiedPlayers());
            if (alive.size() == 1) {
                Player winner = Bukkit.getPlayer(alive.get(0));
                if (winner != null) {
                    Bukkit.broadcastMessage(winner.getName() + " has won the match! Congrats!");
                }
                endMatch(currentMatch);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Match match = getPlayerMatch(uuid);
        if (match == null) return;

        player.setHealth(0.0);
    }

    public Match getMatchById(int id) {
        for (Match m : matches) {
            if (m.getMatchId() == id) return m;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public void startMatch(Match match) {
        if (match.getPlayers().size() == 1) {
            Player p = Bukkit.getPlayer(match.getPlayers().get(0));
            if (p != null) {
                p.sendMessage("§cYou can't play a match by yourself!");
            }
            return;
        }
        if (match.getPlayers().size() > 4) {
            Bukkit.broadcastMessage("§4Error: max 4 players allowed.");
            return;
        }

        Integer freeArena = findFreeArena();
        if (freeArena == null) {
            Bukkit.broadcastMessage("§4No free arenas available right now. Try again later.");
            return;
        }

        match.setArena(freeArena);
        match.setQueueing(false);

        Bukkit.broadcastMessage("§aMatch #" + match.getMatchId() + " starting in Arena " + freeArena + "!");

        List<Location> rooms = getArenaRooms(freeArena);
        if (rooms == null || rooms.isEmpty()) {
            Bukkit.broadcastMessage("§4Error: Arena " + freeArena + " has no rooms defined.");
            return;
        }

        int index = 0;
        for (UUID uuid : match.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            if (index >= rooms.size()) {
                player.sendMessage("§cThere is no available room for you, sorry! There seems to have been an error, please report this to an admin.");
                continue;
            }

            player.teleport(rooms.get(index));
            player.sendMessage("§aYou've been teleported to your arena room!");
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            String command = "kit give "+match.getKit()+" "+player.getName();
            Bukkit.dispatchCommand(console, command);
            command = "effect clear "+player.getName();
            Bukkit.dispatchCommand(console, command);
            index++;

            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.setExhaustion(0.0f); // idk what this does, copilot in visual studio code suggested it
            player.updateInventory();
        }
    }

    @SuppressWarnings("deprecation")
    public void countdownMatch(Match match) {
        Bukkit.getScheduler().runTaskLater(ArenaInstance.getInstance(), () -> {
            Bukkit.broadcastMessage("§aStarting match #"+match.getMatchId()+" in 10 seconds...");
            Bukkit.getScheduler().runTaskLater(ArenaInstance.getInstance(),
                () -> startMatch(match), 200L);
        }, 800L);
    }
}
