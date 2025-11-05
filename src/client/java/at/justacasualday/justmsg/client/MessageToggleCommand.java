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

import java.util.stream.Collectors;

public class MessageToggleCommand {
	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			dispatcher.register(ClientCommandManager.literal("msgt").executes(MessageToggleCommand::disable)
					.then(ClientCommandManager.argument("target", StringArgumentType.string())
							.suggests((context, builder) -> CommandSource
									.suggestMatching(PlayerManager.getAllOnlinePlayers(), builder))
							.executes(context -> {

								String target = StringArgumentType.getString(context, "target");

								if (!PlayerManager.isPlayerOnline(target)) {
									player.sendMessage(Text.literal("Player " + target + " could not be found!"),
											false);

									if (PlayerManager.getTargets().contains(target.toLowerCase())) {
										player.sendMessage(Text.literal("Removing " + player + " from targets!"),
												false);
									}

									return 0;
								}

								if (PlayerManager.addOrRemoveTarget(target)) {
									player.sendMessage(
											Text.literal("Added " + target.toLowerCase() + " to MSG-Multicast!"),
											false);
								} else {
									player.sendMessage(
											Text.literal("Removed " + target.toLowerCase() + " from MSG-Multicast!"),
											false);
								}

								if (!PlayerManager.getTargets().isEmpty()) {
									player.sendMessage(Text.literal("Currently Messaging: "
											+ PlayerManager.getTargets().stream().collect(Collectors.joining(", "))
											+ "!"), false);
								}
								return 0;
							})));
		});
	}

	private static int disable(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (PlayerManager.clearTargets()) {
			player.sendMessage(Text.literal("Successfully cleared all targets!"), false);
		} else {
			player.sendMessage(Text.literal("Nothing to clear!"), false);
		}

		return 0;
	}
}
