package at.justacasualday.justmsg.client;

import at.justacasualday.justmsg.client.managers.PlayerManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.stream.Collectors;

public class MessageToggleCommand {
    public static void register() {
        // spotless:off
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("msgt").executes(MessageToggleCommand::disable)
                .then(ClientCommandManager.argument("target", StringArgumentType.string())
                    .suggests((context, builder) -> CommandSource
                        .suggestMatching(PlayerManager.getAllOnlinePlayers(), builder))
                    .executes(MessageToggleCommand::addTarget))

                .then(ClientCommandManager.literal("group")
                    .then(ClientCommandManager.literal("modify")
                        .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("GroupName", StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource
                                .suggestMatching(PlayerManager.getGroups(), builder))
                            .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource
                                    .suggestMatching(PlayerManager.getAllOnlinePlayers(), builder))
                            .executes(MessageToggleCommand::addToGroup))))

                        .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("GroupName", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource
                                    .suggestMatching(PlayerManager.getGroups(), builder))
                                .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                    .suggests((context, builder) -> CommandSource
                                        .suggestMatching(PlayerManager.getAllOnlinePlayers(), builder))
                                .executes(MessageToggleCommand::removeFromGroup))))

                        .then(ClientCommandManager.literal("list")
                            .then(ClientCommandManager.argument("GroupName", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource
                                    .suggestMatching(PlayerManager.getGroups(), builder))
                            .executes(MessageToggleCommand::listGroupMembers)))

                        .then(ClientCommandManager.literal("set")
                            .then(ClientCommandManager.argument("GroupName", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource
                                    .suggestMatching(PlayerManager.getGroups(), builder))))

                        .then(ClientCommandManager.literal("copy")
                            .then(ClientCommandManager.argument("srcGroup", StringArgumentType.string())
                            .suggests(
                                (context, builder) -> CommandSource.suggestMatching(
                                    PlayerManager.getGroups(), builder))
                            .then(ClientCommandManager.argument("destGroup", StringArgumentType.string())
                                .executes(MessageToggleCommand::copyGroup))))

                    )));
        });
        // spotless:on
    }

    private static int copyGroup(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        String srcGroup = StringArgumentType.getString(context, "srcGroup");
        String destGroup = StringArgumentType.getString(context, "destGroup");

        if (PlayerManager.copyGroup(srcGroup, destGroup)) {
            player.sendMessage(Text.literal(srcGroup + " has been copied to " + destGroup + "!"), false);
        } else {
            player.sendMessage(Text.literal("Enter correct groupNames!"), false);
        }

        return 0;
    }

    private static int addToGroup(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        String group = StringArgumentType.getString(context, "GroupName");
        String target = StringArgumentType.getString(context, "player");

        if (PlayerManager.addToGroup(group, target)) {
            player.sendMessage(Text.literal(target + " has successfully been added to " + group + "!"), false);
        } else {
            player.sendMessage(Text.literal("Could not add " + target + " to " + group + "!"), false);
        }

        return 0;
    }

    private static int removeFromGroup(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        String group = StringArgumentType.getString(context, "GroupName");
        String target = StringArgumentType.getString(context, "player");

        if (PlayerManager.removeFromGroup(group, target)) {
            player.sendMessage(Text.literal(target + " has successfully been removed from " + group + "!"), false);
        } else {
            player.sendMessage(Text.literal("Could not add " + target + " to " + group + "!"), false);
        }

        return 0;
    }

    private static int listGroupMembers(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        String group = StringArgumentType.getString(context, "GroupName");

        Set<String> groupMembers = PlayerManager.getGroupMembers(group);
        if (groupMembers == null) {
            player.sendMessage(Text.literal("This Group does not exist!"), false);
            return 0;
        }

        player.sendMessage(Text.literal("Contains: " + groupMembers.stream().collect(Collectors.joining(", ")) + "!"), false);
        return 0;
    }

    private static int addTarget(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        String target = StringArgumentType.getString(context, "target");

        if (!PlayerManager.isPlayerOnline(target)) {
            player.sendMessage(Text.literal("Player " + target + " could not be found!"), false);

            if (PlayerManager.getTargets().contains(target.toLowerCase())) {
                player.sendMessage(Text.literal("Removing " + player + " from targets!"), false);
            }

            return 0;
        }

        if (PlayerManager.addOrRemoveTarget(target)) {
            player.sendMessage(Text.literal("Added " + target.toLowerCase() + " to MSG-Multicast!"), false);
        } else {
            player.sendMessage(Text.literal("Removed " + target.toLowerCase() + " from MSG-Multicast!"), false);
        }

        if (!PlayerManager.getTargets().isEmpty()) {
            player.sendMessage(Text.literal("Currently Messaging: "
                + PlayerManager.getTargets().stream().collect(Collectors.joining(", ")) + "!"), false);
        }
        return 0;
    }

    private static int disable(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (PlayerManager.clearTargets()) {
            player.sendMessage(Text.literal("Successfully cleared all targets!"), false);
        } else {
            player.sendMessage(Text.literal("Nothing to clear!"), false);
        }

        return 0;
    }
}
