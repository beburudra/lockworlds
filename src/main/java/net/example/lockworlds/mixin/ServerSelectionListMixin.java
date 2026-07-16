package net.example.lockworlds.mixin;

import net.example.lockworlds.config.LockConfig;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin {
    @Shadow
    private List<ServerSelectionList.OnlineServerEntry> onlineServers;

    @Inject(
            method = "updateOnlineServers",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;refreshEntries()V")
    )
    private void lockworlds$pinLockedServers(net.minecraft.client.multiplayer.ServerList servers, CallbackInfo ci) {
        onlineServers.sort((left, right) -> Boolean.compare(
                LockConfig.get().isServerLocked(lockworlds$key(right.getServerData())),
                LockConfig.get().isServerLocked(lockworlds$key(left.getServerData()))
        ));
    }

    private static String lockworlds$key(ServerData serverData) {
        if (serverData == null || serverData.ip == null) return "";
        return serverData.ip.trim().toLowerCase(Locale.ROOT);
    }
}
