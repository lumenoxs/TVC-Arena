package net.tvc.arena.utils;

import net.tvc.arena.ArenaInstance;
import net.tvc.arena.classes.Match;
import net.tvc.arena.managers.ConfigMgr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Comparator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mojang.brigadier.context.CommandContext;

import io.papermc.paper.command.brigadier.CommandSourceStack;

public class ArenaLogic {
    private static List<Match> matches = new ArrayList<>();

    @SuppressWarnings("deprecation")
    public static void command(CommandContext<CommandSourceStack> ctx, String label) {
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

            case "default" -> {
                sender.sendMessage("§eStart a new match with:\n§a/arena start <kit>\n§eJoin a current match with:\n§a/arena join [match]\n§eSkip the countdown for the match you are in:\n§a/arena skip\n§ePreview a kit:\n§a/arena preview <kit>\n§eSee a list of kits:\n§a/arena kits");
                return;
            }

            default -> {
                sender.sendMessage("§eStart a new match with:\n§a/arena start <kit>\n§eJoin a current match with:\n§a/arena join [match]\n§eSkip the countdown for the match you are in:\n§a/arena skip\n§ePreview a kit:\n§a/arena preview <kit>\n§eSee a list of kits:\n§a/arena kits");
                return;
            }
        }
    }

    public static Set<String> getKits() {
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

    public static Match getPlayerMatch(UUID uuid) {
        for (Match m : matches) {
            if (m.getPlayers().contains(uuid)) {
                return m;
            }
        }
        return null;
    }

    public static List<Integer> getAllArenaIds() {
        if (ConfigMgr.debug()) {
            ArenaInstance.getInstance().getLogger().info(ArenaInstance.getInstance().getConfig().getValues(true).toString());
        }

        List<Map<?, ?>> arenas = ArenaInstance.getInstance().getConfig().getMapList("arenas");
        List<Integer> ids = new ArrayList<>();
        for (Map<?, ?> arena : arenas) {
            ids.add((int) arena.get("id"));
        }
        return ids;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Location> getArenaRooms(Integer arenaId) {
        List<Map<?, ?>> arenas = ArenaInstance.getInstance().getConfig().getMapList("arenas");
        List<Location> locations = new ArrayList<>();
        Map<?, ?> wantedArena = null;
        
        if (!getAllArenaIds().contains(arenaId)) return locations;

        for (Map<?, ?> arena : arenas) {
            if ((int) arena.get("id") == arenaId) {
                wantedArena = arena;
                break;
            }
        } 

        if (wantedArena == null) return locations;

        List<Map<?, ?>> rooms = (List<Map<?, ?>>) wantedArena.get("rooms");

        if (rooms == null) return locations;

        for (Map<?, ?> room : rooms) {
            String worldName = (String) room.get("world");
            World world = Bukkit.getWorld(worldName);

            int x = (int) room.get("x");
            int y = (int) room.get("y");
            int z = (int) room.get("z");
            Float yaw_ = (Float) room.get("yaw");
            float yaw = (yaw_ != null) ? yaw_ : 0f;
            Float pitch_ = (Float) room.get("pitch");
            float pitch = (pitch_ != null) ? pitch_ : 0f;

            locations.add(new Location(world, x, y, z, yaw, pitch));
        }

        return locations;
    }

    @SuppressWarnings("deprecation")
    public static void endMatch(Match match) {
        matches.remove(match);
        match.setArena(null);
        Bukkit.broadcastMessage("§aMatch #" + match.getMatchId() + " has ended.");
    }


    public static Integer findFreeArena() {
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

    public static Match getActiveQueue() {
        return matches.stream()
                .filter(Match::isQueueing)
                .max(Comparator.comparingInt(Match::getPriority))
                .orElse(null);
    }

    public static Match findMatch(UUID uuid) {
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
        if (ConfigMgr.debug()) {
            Bukkit.broadcastMessage("Player "+event.getPlayer().getName()+" died!");
        }
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

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (ConfigMgr.debug()) {
            Bukkit.broadcastMessage("Player "+event.getPlayer().getName()+" quit!");
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Match match = getPlayerMatch(uuid);
        if (match == null) return;

        player.setHealth(0.0);
    }

    public static Match getMatchById(int id) {
        for (Match m : matches) {
            if (m.getMatchId() == id) return m;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static void startMatch(Match match) {
        if (match.getPlayers().size() == 1) {
            if (!ConfigMgr.debug()) {
                Player p = Bukkit.getPlayer(match.getPlayers().get(0));
                if (p != null) {
                    p.sendMessage("§cYou can't play a match by yourself!");
                }
                return;
            }
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
        openCloseGates(match.getArena());
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    public static void openCloseGates(Integer arenaId) {
        if (ConfigMgr.debug()) {
            Bukkit.broadcastMessage("Opening/closing gates");
        }
        List<Map<?, ?>> arenas = (List<Map<?, ?>>) ArenaInstance.getInstance().getConfig().get("arenas");
        Map<?, ?> wantedArena = null;

        for (Map<?, ?> arena : arenas) {
            if ((int) arena.get("id") == arenaId) {
                wantedArena = arena;
                break;
            }
        }

        if (wantedArena == null) return;

        Map<?, ?> init_config = (Map<?, ?>) wantedArena.get("init");
        String worldName = (String) init_config.get("world");
        World world = Bukkit.getWorld(worldName);

        int x = (int) init_config.get("x");
        int y = (int) init_config.get("y");
        int z = (int) init_config.get("z");

        Location init_location = new Location(world, x, y, z);
        Block init_block = world.getBlockAt(init_location);

        Bukkit.getScheduler().runTaskLater(ArenaInstance.getInstance(), () -> {
            init_block.setType(Material.REDSTONE_BLOCK);
            Bukkit.getScheduler().runTaskLater(ArenaInstance.getInstance(), () -> {
                init_block.setType(Material.AIR);
            }, 20L);
        }, 60L);
    }

    @SuppressWarnings("deprecation")
    public static void countdownMatch(Match match) {
        Bukkit.getScheduler().runTaskLater(ArenaInstance.getInstance(), () -> {
            Bukkit.broadcastMessage("§aStarting match #"+match.getMatchId()+" in 10 seconds...");
            Bukkit.getScheduler().runTaskLater(ArenaInstance.getInstance(),
                () -> startMatch(match), 200L);
        }, 800L);
    }
}
