package at.justacasualday.justmsg.client.managers;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PlayerManager {
	private static Set<String> targets = new HashSet<>();

	public static List<String> clearOfflinePlayers() {
		List<String> clearedPlayers = new ArrayList<>();
		List<String> currentPlayers = getAllOnlinePlayers();

		for (String player : targets) {
			if (!currentPlayers.contains(player)) {
				clearedPlayers.add(player);
			}
		}

		clearedPlayers.forEach(targets::remove);

		return clearedPlayers;
	}

	public static boolean isPlayerOnline(String player) {
		return getAllOnlinePlayers().contains(player.toLowerCase());
	}

	public static List<String> getAllOnlinePlayers() {
		List<String> players = new ArrayList<>();
		MinecraftClient.getInstance().getNetworkHandler().getPlayerList().stream()
				.forEach(p -> players.add(p.getProfile().getName().toLowerCase()));

		return players;
	}

	public static boolean addOrRemoveTarget(String target) {
		String pTarget = target.toLowerCase();
		if (targets.contains(pTarget)) {
			targets.remove(pTarget);

			if (targets.isEmpty()) {
				ChatManager.setIsActive(false);
			}
			return false;
		} else {
			targets.add(pTarget);
			ChatManager.setIsActive(true);
		}

		return true;
	}

	public static boolean clearTargets() {
		if (!targets.isEmpty()) {
			targets.clear();
			ChatManager.setIsActive(false);

			return true;
		}

		return false;
	}

	public static Set<String> getTargets() {
		return targets;
	}
}
