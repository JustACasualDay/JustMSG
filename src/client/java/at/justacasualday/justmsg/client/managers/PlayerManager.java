package at.justacasualday.justmsg.client.managers;

import com.google.common.collect.HashBiMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.*;

public abstract class PlayerManager {
	private static Set<String> SAVEDGROUPMEMBERS = null;
	private static boolean groupActive = false;

	private static Set<String> targets = new HashSet<>();
	private static HashMap<String, Set<String>> groups = new HashMap<>();
	private static HashBiMap<String, String> aliases = HashBiMap.create();

	public static boolean addAlias(String alias, String player) {
		if (aliases.containsKey(alias))
			return false;

		aliases.put(alias, player);

		return true;
	}

	public static boolean removeAlias(String alias) {
		if (!aliases.containsKey(alias))
			return false;

		aliases.remove(alias);

		return true;
	}

	public static HashBiMap<String, String> getAliasesMap() {
		return aliases;
	}

	public static List<String> clearOfflinePlayers() {
		List<String> clearedPlayers = new ArrayList<>();
		Set<String> currentPlayers = getAllOnlinePlayers();

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

	public static Set<String> getAllOnlinePlayers() {
		Set<String> players = new HashSet<>();
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

	public static void sendMessage(String text) {
		MinecraftClient.getInstance().player.sendMessage(Text.literal(text), false);
	}

	public static boolean setGroup(String group) {
		if (group == null) {
			if (!isGroupActive())
				return false;

			groupActive = false;
			targets = SAVEDGROUPMEMBERS;
			return true;
		}

		if (groups.get(group) == null)
			return false;

		if (!isGroupActive()) {
			SAVEDGROUPMEMBERS = targets;
			groupActive = true;
		}

		targets = groups.get(group);

		return true;
	}

	public static boolean isGroupActive() {
		return groupActive;
	}

	public static Set<String> getAllOnlineTargets() {
		Set<String> onlineTargets = new HashSet<>();
		Set<String> onlinePlayers = PlayerManager.getAllOnlinePlayers();

		for (String target : targets) {
			if (onlinePlayers.contains(target.toLowerCase())) {
				onlineTargets.add(target);
			}
		}

		return onlineTargets;
	}

	public static Set<String> getAllAliases() {
		return aliases.keySet();
	}

	public static String getAliasForPlayer(String player) {
		String alias = aliases.inverse().get(player);

		// if no alias was found return just playername
		if (alias == null) {
			alias = player;
		}

		return alias;
	}

	public static String getPlayerFromAlias(String alias) {
		String player = aliases.get(alias);

		if (player == null) {
			player = alias;
		}

		return player;
	}

	public static List<String> getMessagedTargets() {
		List<String> playerList = new ArrayList<>();

		getAllOnlineTargets().stream().forEach(p -> playerList.add(PlayerManager.getAliasForPlayer(p)));

		return playerList;
	}
}
