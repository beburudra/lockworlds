package net.example.lockworlds.mixin;

import net.example.lockworlds.LockWorldsClient;
import net.example.lockworlds.config.LockConfig;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.storage.LevelSummary;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(AbstractContainerWidget.class)
public abstract class LockButtonListClickMixin {
    @Unique private static final int ICON_SIZE = 16;
    @Unique private static final int THUMBNAIL_SIZE = 32;
    @Unique private static final int ICON_GAP = 8;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void lockworlds$clickLockButton(MouseButtonEvent click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (click.buttonInfo().button() != GLFW.GLFW_MOUSE_BUTTON_1) return;

        Object self = this;
        if (self instanceof WorldSelectionList worldList && lockworlds$clickWorldLock(worldList, click.x(), click.y())) {
            cir.setReturnValue(true);
            return;
        }

        if (self instanceof ServerSelectionList serverList && lockworlds$clickServerLock(serverList, click.x(), click.y())) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean lockworlds$clickWorldLock(WorldSelectionList list, double mouseX, double mouseY) {
        for (WorldSelectionList.Entry row : list.children()) {
            if (!(row instanceof WorldSelectionList.WorldListEntry worldEntry)) continue;
            if (!lockworlds$hit(row, mouseX, mouseY)) continue;

            LevelSummary summary = worldEntry.getLevelSummary();
            if (summary == null) return false;

            String folder = summary.getLevelId();
            boolean nowLocked = LockConfig.get().toggleLock(folder);
            LockWorldsClient.playLockSound();
            list.reloadWorldList();
            LockWorldsClient.LOGGER.info("[LockWorlds] '{}' is now {} and {}.", folder, nowLocked ? "LOCKED" : "UNLOCKED", nowLocked ? "PINNED" : "UNPINNED");
            return true;
        }
        return false;
    }

    @Unique
    private static boolean lockworlds$clickServerLock(ServerSelectionList list, double mouseX, double mouseY) {
        for (ServerSelectionList.Entry row : list.children()) {
            if (!(row instanceof ServerSelectionList.OnlineServerEntry serverEntry)) continue;
            if (!lockworlds$hit(row, mouseX, mouseY)) continue;

            ServerData serverData = serverEntry.getServerData();
            String key = lockworlds$serverKey(serverData);
            if (key == null) return false;

            boolean nowLocked = LockConfig.get().toggleServerLock(key);
            LockWorldsClient.playLockSound();
            JoinMultiplayerScreen screen = ((ServerSelectionListAccessor) list).lockworlds$getScreen();
            if (screen != null && screen.getServers() != null) {
                list.updateOnlineServers(screen.getServers());
            }
            LockWorldsClient.LOGGER.info("[LockWorlds] Server '{}' is now {} and {}.", key, nowLocked ? "LOCKED" : "UNLOCKED", nowLocked ? "PINNED" : "UNPINNED");
            return true;
        }
        return false;
    }

    @Unique
    private static boolean lockworlds$hit(ObjectSelectionList.Entry<?> row, double mouseX, double mouseY) {
        int iconX = row.getContentX() - ICON_SIZE - ICON_GAP;
        int iconY = row.getContentY() + (THUMBNAIL_SIZE - ICON_SIZE) / 2;
        return mouseX >= iconX && mouseX < iconX + ICON_SIZE && mouseY >= iconY && mouseY < iconY + ICON_SIZE;
    }

    @Unique
    private static String lockworlds$serverKey(ServerData serverData) {
        if (serverData == null || serverData.ip == null || serverData.ip.isBlank()) return null;
        return serverData.ip.trim().toLowerCase(Locale.ROOT);
    }
}
