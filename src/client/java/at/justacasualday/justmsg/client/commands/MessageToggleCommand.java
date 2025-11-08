package at.justacasualday.justmsg.client.commands;

import at.justacasualday.justmsg.client.api.enums.CommandParams;
import at.justacasualday.justmsg.client.managers.PlayerManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.Set;
import java.util.stream.Collectors;

public class MessageToggleCommand {
	public static void register() {
		// spotless:off
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("msgt").executes(MessageToggleCommand::disable)

                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument(CommandParams.TARGET.getArgName(), StringArgumentType.string())
                        .suggests((context, builder) -> CommandSource
                            .suggestMatching(PlayerManager.getAllOnlinePlayers(), builder))
                        .executes(MessageToggleCommand::addTarget)))

                .then(ClientCommandManager.literal("aliases")
                    .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument(CommandParams.TARGET.getArgName(), StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource
                                .suggestMatching(PlayerManager.getAllOnlinePlayers(), builder))
                            .then(ClientCommandManager.argument(CommandParams.ALIAS.getArgName(), StringArgumentType.string())
                                .executes(MessageToggleCommand::addAlias))))
                    .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument(CommandParams.ALIAS.getArgName(), StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource
                                .suggestMatching(PlayerManager.getAllAliases(), builder))
                            .executes(MessageToggleCommand::removeAlias)))
                    .then(ClientCommandManager.literal("list")
                        .executes(MessageToggleCommand::listAliases)))


                .then(ClientCommandManager.literal("group")

                    .then(ClientCommandManager.literal("set")
                        .then(ClientCommandManager.argument(CommandParams.GROUP.getArgName(), StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource
                                .suggestMatching(PlayerManager.getGroups(), builder))
                            .executes(MessageToggleCommand::set)))

                    .then(ClientCommandManager.literal("reset")
                        .executes(MessageToggleCommand::resetGroup))

                    .then(ClientCommandManager.literal("modify")
                        .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument(CommandParams.GROUP.getArgName(), StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource
                                .suggestMatching(PlayerManager.getGroups(), builder))
                                .then(ClientCommandManager.argument(CommandParams.TARGET.getArgName(), StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource
                                    .suggestMatching(PlayerManager.getAllOnlinePlayers(), builder))
                            .executes(MessageToggleCommand::addToGroup))))

                        .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument(CommandParams.GROUP.getArgName(), StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource
                                    .suggestMatching(PlayerManager.getGroups(), builder))
                                .then(ClientCommandManager.argument(CommandParams.TARGET.getArgName(), StringArgumentType.string())
                                    .suggests((context, builder) -> CommandSource
                                        .suggestMatching(PlayerManager.getAllOnlinePlayers(), builder))
                                .executes(MessageToggleCommand::removeFromGroup))))

                        .then(ClientCommandManager.literal("list")
                            .then(ClientCommandManager.argument(CommandParams.GROUP.getArgName(), StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource
                                    .suggestMatching(PlayerManager.getGroups(), builder))
                            .executes(MessageToggleCommand::listGroupMembers)))

                        .then(ClientCommandManager.literal("copy")
                            .then(ClientCommandManager.argument(CommandParams.SRCGROUP.getArgName(), StringArgumentType.string())
                            .suggests(
                                (context, builder) -> CommandSource.suggestMatching(
                                    PlayerManager.getGroups(), builder))
                                .then(ClientCommandManager.argument(CommandParams.DESTGROUP.getArgName(), StringArgumentType.string())
                                .executes(MessageToggleCommand::copyGroup))))

                    )
                )
            );
        });
        // spotless:on
	}

	private static int listAliases(CommandContext<FabricClientCommandSource> context) {

		PlayerManager.sendMessage("Aliases: ");

		for (String alias : PlayerManager.getAllAliases()) {
			PlayerManager.sendMessage(alias + " : " + PlayerManager.getPlayerFromAlias(alias));
		}

		return 0;
	}

	private static int removeAlias(CommandContext<FabricClientCommandSource> context) {
		String alias = StringArgumentType.getString(context, CommandParams.ALIAS.getArgName());

		if (!PlayerManager.removeAlias(alias)) {
			PlayerManager.sendMessage("Alias " + alias + " could not been removed!");
			return 0;
		}

		PlayerManager.sendMessage("Alias " + alias + " has been removed!");
		return 0;
	}

	private static int addAlias(CommandContext<FabricClientCommandSource> context) {
		String alias = StringArgumentType.getString(context, CommandParams.ALIAS.getArgName());
		String player = StringArgumentType.getString(context, CommandParams.TARGET.getArgName());

		if (!PlayerManager.addAlias(alias, player)) {
			PlayerManager.sendMessage("Cannot add alias " + alias + " because it already exists!");
			return 0;
		}

		PlayerManager.sendMessage("Added alias " + alias + " for player " + player + "!");
		return 0;
	}

	private static int resetGroup(CommandContext<FabricClientCommandSource> context) {
		if (!PlayerManager.setGroup(null)) {
			PlayerManager.sendMessage("You cannot reset, since you are not in a group!");
			return 0;
		}

		PlayerManager.sendMessage("Successfully reset group! Restored previous session!");
		return 0;
	}

	private static int set(CommandContext<FabricClientCommandSource> context) {
		String group = StringArgumentType.getString(context, CommandParams.GROUP.getArgName());

		if (!PlayerManager.setGroup(group)) {
			PlayerManager.sendMessage("Invalid GroupName!");
			return 0;
		}

		PlayerManager.sendMessage("Successfully set current group to " + group + "!");
		return 0;
	}

	private static int copyGroup(CommandContext<FabricClientCommandSource> context) {
		String srcGroup = StringArgumentType.getString(context, CommandParams.SRCGROUP.getArgName());
		String destGroup = StringArgumentType.getString(context, CommandParams.DESTGROUP.getArgName());

		if (PlayerManager.copyGroup(srcGroup, destGroup)) {
			PlayerManager.sendMessage(srcGroup + " has been copied to " + destGroup + "!");
		} else {
			PlayerManager.sendMessage("There isn't a group with name  " + srcGroup + "!");
		}

		return 0;
	}

	private static int addToGroup(CommandContext<FabricClientCommandSource> context) {
		String group = StringArgumentType.getString(context, CommandParams.GROUP.getArgName());
		String target = StringArgumentType.getString(context, CommandParams.TARGET.getArgName());

		if (PlayerManager.addToGroup(group, target)) {
			PlayerManager.sendMessage(target + " has successfully been added to " + group + "!");
		} else {
			PlayerManager.sendMessage("Could not add " + target + " to " + group + "!");
		}

		return 0;
	}

	private static int removeFromGroup(CommandContext<FabricClientCommandSource> context) {
		String group = StringArgumentType.getString(context, CommandParams.GROUP.getArgName());
		String target = StringArgumentType.getString(context, CommandParams.TARGET.getArgName());

		if (PlayerManager.removeFromGroup(group, target)) {
			PlayerManager.sendMessage(target + " has successfully been removed from " + group + "!");
		} else {
			PlayerManager.sendMessage("Could not remove " + target + " from " + group + "!");
		}

		return 0;
	}

	private static int listGroupMembers(CommandContext<FabricClientCommandSource> context) {
		String group = StringArgumentType.getString(context, CommandParams.GROUP.getArgName());

		Set<String> groupMembers = PlayerManager.getGroupMembers(group);
		if (groupMembers == null) {
			PlayerManager.sendMessage("This Group does not exist!");
			return 0;
		}

		PlayerManager.sendMessage("Contains: " + groupMembers.stream().collect(Collectors.joining(", ")) + "!");
		return 0;
	}

	private static int addTarget(CommandContext<FabricClientCommandSource> context) {
		String target = StringArgumentType.getString(context, CommandParams.TARGET.getArgName());

		target = PlayerManager.getPlayerFromAlias(target);

		if (!PlayerManager.isPlayerOnline(target)) {
			PlayerManager.sendMessage(target + " is not online!");

			if (PlayerManager.getTargets().contains(target.toLowerCase())) {
				PlayerManager.sendMessage("Removing " + target + " from targets!");
				PlayerManager.removeTarget(target);
			}

			return 0;
		}

		if (PlayerManager.addTarget(target)) {
			PlayerManager.sendMessage("Added " + target.toLowerCase() + " to MSG-Multicast!");
		} else {
			PlayerManager.removeTarget(target);
			PlayerManager.sendMessage("Removed " + target.toLowerCase() + " from MSG-Multicast!");
		}

		if (!PlayerManager.getTargets().isEmpty()) {
			PlayerManager.sendMessage("Currently Messaging: "
					+ PlayerManager.getTargets().stream().collect(Collectors.joining(", ")) + "!");
		}
		return 0;
	}

	private static int disable(CommandContext<FabricClientCommandSource> context) {
		if (PlayerManager.clearTargets()) {
			PlayerManager.sendMessage("Successfully cleared all targets!");
		} else {
			PlayerManager.sendMessage("Nothing to clear!");
		}

		return 0;
	}
}
