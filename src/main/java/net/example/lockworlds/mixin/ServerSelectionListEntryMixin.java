package net.example.lockworlds.mixin;

import net.example.lockworlds.LockWorldsClient;
import net.example.lockworlds.config.LockConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class ServerSelectionListEntryMixin {
    @Shadow
    private JoinMultiplayerScreen screen;

    @Shadow
    private ServerData serverData;

    @Unique private static final int ICON_SIZE = 16;
    @Unique private static final int THUMBNAIL_SIZE = 32;
    @Unique private static final int ICON_GAP = 8;
    @Unique private static final Identifier SPRITE_UNLOCKED = Identifier.fromNamespaceAndPath("lockworlds", "icon/lock_open");
    @Unique private static final Identifier SPRITE_LOCKED = Identifier.fromNamespaceAndPath("lockworlds", "icon/lock_closed");
    @Unique private static boolean lockworlds$loggedRenderError;
    @Unique private static boolean lockworlds$loggedClickError;

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void lockworlds$renderIcon(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        try {
            String key = lockworlds$serverKey();
            if (key == null) return;

            boolean locked = LockConfig.get().isServerLocked(key);
            int iconX = lockworlds$iconX();
            int iconY = lockworlds$iconY();
            boolean iconHovered = lockworlds$hit(mouseX, mouseY, iconX, iconY);

            if (iconHovered) {
                graphics.fill(iconX - 1, iconY - 1, iconX + ICON_SIZE + 1, iconY + ICON_SIZE + 1, 0x44FFFFFF);
            }

            Identifier sprite = locked ? SPRITE_LOCKED : SPRITE_UNLOCKED;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, iconX, iconY, ICON_SIZE, ICON_SIZE);

            if (iconHovered) {
                Component tip = locked
                        ? Component.translatable("lockworlds.tooltip.unlock_server")
                        : Component.translatable("lockworlds.tooltip.lock_server");
                graphics.setTooltipForNextFrame(tip, mouseX, mouseY);
            }
        } catch (Throwable t) {
            if (!lockworlds$loggedRenderError) {
                lockworlds$loggedRenderError = true;
                LockWorldsClient.LOGGER.error("[LockWorlds] Server row icon rendering failed; the screen will keep working without the icon.", t);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
    private void lockworlds$onMouseClicked(MouseButtonEvent click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (click.buttonInfo().button() != GLFW.GLFW_MOUSE_BUTTON_1) return;

            String key = lockworlds$serverKey();
            if (key == null) return;

            int iconX = lockworlds$iconX();
            int iconY = lockworlds$iconY();
            if (lockworlds$hit(click.x(), click.y(), iconX, iconY)) {
                boolean nowLocked = LockConfig.get().toggleServerLock(key);
                LockWorldsClient.playLockSound();
                ServerSelectionList list = ((JoinMultiplayerScreenAccessor) screen).lockworlds$getServerSelectionList();
                if (list != null && screen.getServers() != null) {
                    list.updateOnlineServers(screen.getServers());
                }
                LockWorldsClient.LOGGER.info("[LockWorlds] Server '{}' is now {} and {}.", key, nowLocked ? "LOCKED" : "UNLOCKED", nowLocked ? "PINNED" : "UNPINNED");
                cir.setReturnValue(true);
            }
        } catch (Throwable t) {
            if (!lockworlds$loggedClickError) {
                lockworlds$loggedClickError = true;
                LockWorldsClient.LOGGER.error("[LockWorlds] Server row click handling failed; clicks will fall back to vanilla behavior.", t);
            }
        }
    }

    @Unique
    private String lockworlds$serverKey() {
        if (serverData == null || serverData.ip == null || serverData.ip.isBlank()) return null;
        return serverData.ip.trim().toLowerCase(java.util.Locale.ROOT);
    }

    @Unique
    private int lockworlds$iconX() {
        ObjectSelectionList.Entry<?> entry = (ObjectSelectionList.Entry<?>)(Object)this;
        return entry.getContentX() - ICON_SIZE - ICON_GAP;
    }

    @Unique
    private int lockworlds$iconY() {
        ObjectSelectionList.Entry<?> entry = (ObjectSelectionList.Entry<?>)(Object)this;
        return entry.getContentY() + (THUMBNAIL_SIZE - ICON_SIZE) / 2;
    }

    @Unique
    private boolean lockworlds$hit(double mx, double my, int iconX, int iconY) {
        return mx >= iconX && mx < iconX + ICON_SIZE && my >= iconY && my < iconY + ICON_SIZE;
    }
}
