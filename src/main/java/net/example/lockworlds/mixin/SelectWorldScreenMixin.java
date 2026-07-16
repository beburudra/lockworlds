package net.example.lockworlds.mixin;

import net.example.lockworlds.config.LockConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin {

    @Shadow private Button deleteButton;
    @Shadow private WorldSelectionList list;

    @Inject(method = "updateButtonStatus(Lnet/minecraft/world/level/storage/LevelSummary;)V", at = @At("RETURN"), require = 0)
    private void lockworlds$guardDelete(LevelSummary levelSummary, CallbackInfo ci) {
        lockworlds$syncDeleteButton();
    }

    @Inject(method = "updateButtonStatus(ZZ)V", at = @At("RETURN"), require = 0)
    private void lockworlds$guardDelete(boolean validSelection, boolean emptySelection, CallbackInfo ci) {
        lockworlds$syncDeleteButton();
    }

    @Unique
    private void lockworlds$syncDeleteButton() {
        try {
            if (deleteButton == null || !deleteButton.active) return;

            String folder = lockworlds$selectedFolder();
            if (folder != null && LockConfig.get().isLocked(folder)) {
                deleteButton.active = false;
            }
        } catch (Throwable t) {
            net.example.lockworlds.LockWorldsClient.LOGGER.error("[LockWorlds] Failed to sync Delete button state.", t);
        }
    }

    @Unique
    private String lockworlds$selectedFolder() {
        if (list == null) return null;

        WorldSelectionList.WorldListEntry entry = list.getSelectedOpt().orElse(null);
        if (entry == null) return null;

        LevelSummary summary = entry.getLevelSummary();
        if (summary == null) return null;

        return summary.getLevelId();
    }
}
