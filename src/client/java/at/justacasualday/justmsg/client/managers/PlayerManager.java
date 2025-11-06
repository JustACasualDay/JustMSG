package at.justacasualday.justmsg.client.managers;

import net.minecraft.client.MinecraftClient;

import java.util.*;

public abstract class PlayerManager {
	private static Set<String> targets = new HashSet<>();

	private static HashMap<String, Set<String>> groups = new HashMap<>();

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

	public static Set<String> getGroups() {
		return groups.keySet();
	}

	public static Set<String> getGroupMembers(String group) {
		return groups.get(group);
	}

	public static boolean copyGroup(String srcGroup, String destGroup) {
		if (!groups.containsKey(srcGroup) || groups.containsKey(destGroup))
			return false;

		groups.put(destGroup, new HashSet<>(groups.get(srcGroup)));

		return true;
	}

	public static boolean addToGroup(String group, String player) {
		Set<String> groupMembers = groups.get(group);
		if (groupMembers == null) {
			groupMembers = new HashSet<>();
			groupMembers.add(player.toLowerCase());

			groups.put(group, groupMembers);
			return true;
		}

		if (!groupMembers.contains(player.toLowerCase())) {
			groupMembers.add(player.toLowerCase());

			return true;
		}

		return false;
	}

	public static boolean removeFromGroup(String group, String player) {
		Set<String> groupMembers = groups.get(group);

		if (groupMembers == null)
			return false;

		if (groupMembers.contains(player.toLowerCase())) {
			groupMembers.remove(player.toLowerCase());

			if (groupMembers.isEmpty()) {
				groups.remove(group);
			}

			return true;
		}

		return false;
	}
}
