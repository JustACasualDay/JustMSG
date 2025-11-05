package at.justacasualday.justmsg.client.managers;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ChatManager {
	private static boolean isActive = false;

	private static String currentMessage = "";
	private static String currentGroup = null;

	public static void register() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (!isIsActive()) {
                return true;
            }

            ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();

            List<String> cleared = PlayerManager.clearOfflinePlayers();

            if (!cleared.isEmpty()) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Removed " + cleared.stream().collect(Collectors.joining(", ")) + " because they were offline!"), false);
            }

            currentMessage = message;

            for (String player : PlayerManager.getTargets()) {
                handler.sendChatCommand("msg " + player + " " + message);
            }

            MinecraftClient.getInstance().inGameHud.getChatHud()
                    .addMessage(Text.literal("You whispered to " + PlayerManager.getTargets().stream().collect(Collectors.joining(" and ")) + ": " + message)
                            .setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)));

            return false;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((text, signedMessage, gameProfile, parameters, instant) -> {
            String message;

            if (text.getContent() instanceof TranslatableTextContent translatable) {
                Object[] args = translatable.getArgs();
                if (args.length >= 2 && args[1] instanceof Text msgPart) {
                    message = msgPart.getString();

                    // minecraft:msg_command_outgoing
                    if (parameters.type().getIdAsString().equalsIgnoreCase(MessageType.MSG_COMMAND_OUTGOING.getValue().toString())
                            && message.equalsIgnoreCase(currentMessage)) {
                        // Block Vanilla Echo Message
                        return false;
                    }
                }
            }

            return true;
        });
    }

	public static boolean isIsActive() {
		return isActive;
	}

	public static void setIsActive(boolean value) {
		isActive = value;
	}
}
