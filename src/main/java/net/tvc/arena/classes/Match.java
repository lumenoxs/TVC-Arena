package net.tvc.arena.classes;

import java.util.*;

public class Match {
    private boolean queueing;
    private final List<UUID> players;
    private Integer arena;
    private final int matchId;
    private final int priority;
    private final String kit;
    private final List<UUID> diedPlayers;

    public Match(boolean queueing, List<UUID> players, Integer arena, String kit, int matchId, int priority) {
        this.queueing = queueing;
        this.players = new ArrayList<>(players);
        this.arena = arena;
        this.kit = kit;
        this.matchId = matchId;
        this.priority = priority;
        this.diedPlayers = new ArrayList<>();
    }

    public boolean isQueueing() {
        return queueing;
    }
    public List<UUID> getPlayers() {
        return Collections.unmodifiableList(players);
    }
    public Integer getArena() {
        return arena;
    }
    public String getKit() {
		return kit;
	}
    public int getMatchId() {
        return matchId;
    }
    public int getPriority() {
        return priority;
    }
    public void setArena(Integer arena) {
        this.arena = arena;
    }
    public void addPlayer(UUID playeruuid) {
        players.add(playeruuid);
    }

    public void setQueueing(boolean queueing) {
        this.queueing = queueing;
    }

    public List<UUID> getDiedPlayers() {
        return diedPlayers;
    }

    public boolean containsPlayer(UUID uuid) {
        return players.contains(uuid) || diedPlayers.contains(uuid);
    }

    public void markDead(UUID uuid) {
        players.remove(uuid);
        if (!diedPlayers.contains(uuid)) {
            diedPlayers.add(uuid);
        }
    }

    public int getPlacement(UUID uuid) {
        if (diedPlayers.contains(uuid)) {
            return diedPlayers.size();
        }
        return -1;
    }
}
