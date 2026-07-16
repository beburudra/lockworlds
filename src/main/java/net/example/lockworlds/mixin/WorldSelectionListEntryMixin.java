package net.example.lockworlds.mixin;

import net.example.lockworlds.LockWorldsClient;
import net.example.lockworlds.config.LockConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldSelectionListEntryMixin {

    @Shadow
    private LevelSummary summary;

    @Shadow
    private WorldSelectionList list;

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
            String folder = lockworlds$folderName();
            if (folder == null) return;

            boolean locked = LockConfig.get().isLocked(folder);
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
                        ? Component.translatable("lockworlds.tooltip.unlock")
                        : Component.translatable("lockworlds.tooltip.lock");
                graphics.setTooltipForNextFrame(tip, mouseX, mouseY);
            }
        } catch (Throwable t) {
            if (!lockworlds$loggedRenderError) {
                lockworlds$loggedRenderError = true;
                LockWorldsClient.LOGGER.error("[LockWorlds] World row icon rendering failed; the screen will keep working without the icon.", t);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
    private void lockworlds$onMouseClicked(MouseButtonEvent click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (click.buttonInfo().button() != GLFW.GLFW_MOUSE_BUTTON_1) return;

            String folder = lockworlds$folderName();
            if (folder == null) return;

            int iconX = lockworlds$iconX();
            int iconY = lockworlds$iconY();
            double mouseX = click.x();
            double mouseY = click.y();

            if (lockworlds$hit(mouseX, mouseY, iconX, iconY)) {
                boolean nowLocked = LockConfig.get().toggleLock(folder);
                LockWorldsClient.playLockSound();
                list.reloadWorldList();
                LockWorldsClient.LOGGER.info("[LockWorlds] '{}' is now {} and {}.", folder, nowLocked ? "LOCKED" : "UNLOCKED", nowLocked ? "PINNED" : "UNPINNED");
                cir.setReturnValue(true);
            }
        } catch (Throwable t) {
            if (!lockworlds$loggedClickError) {
                lockworlds$loggedClickError = true;
                LockWorldsClient.LOGGER.error("[LockWorlds] World row click handling failed; clicks will fall back to vanilla behavior.", t);
            }
        }
    }

    @Unique
    private String lockworlds$folderName() {
        if (summary == null) return null;
        return summary.getLevelId();
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
