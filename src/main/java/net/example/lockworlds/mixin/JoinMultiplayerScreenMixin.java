package net.example.lockworlds.mixin;

import net.example.lockworlds.config.LockConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin {
    @Shadow
    protected ServerSelectionList serverSelectionList;

    @Shadow
    private Button deleteButton;

    @Inject(method = "onSelectedChange", at = @At("RETURN"), require = 0)
    private void lockworlds$guardDeleteButton(CallbackInfo ci) {
        if (deleteButton == null || serverSelectionList == null || !deleteButton.active) return;

        ServerSelectionList.Entry entry = serverSelectionList.getSelected();
        if (entry instanceof ServerSelectionList.OnlineServerEntry onlineServerEntry) {
            ServerData serverData = onlineServerEntry.getServerData();
            if (serverData != null && LockConfig.get().isServerLocked(lockworlds$key(serverData))) {
                deleteButton.active = false;
            }
        }
    }

    private static String lockworlds$key(ServerData serverData) {
        if (serverData == null || serverData.ip == null) return "";
        return serverData.ip.trim().toLowerCase(Locale.ROOT);
    }
}
