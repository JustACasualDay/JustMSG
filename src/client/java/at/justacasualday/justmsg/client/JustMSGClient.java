package at.justacasualday.justmsg.client;

import at.justacasualday.justmsg.client.managers.ChatManager;
import net.fabricmc.api.ClientModInitializer;

public class JustMSGClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MessageToggleCommand.register();
        ChatManager.register();
    }
}
