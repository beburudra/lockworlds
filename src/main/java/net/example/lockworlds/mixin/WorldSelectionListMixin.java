package net.example.lockworlds.mixin;

import net.example.lockworlds.config.LockConfig;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldSelectionList.class)
public abstract class WorldSelectionListMixin {
    @Unique
    private boolean lockworlds$sortingLevels;

    @Inject(method = "fillLevels", at = @At("HEAD"), cancellable = true)
    private void lockworlds$pinLockedWorlds(String filter, List<LevelSummary> levels, CallbackInfo ci) {
        if (lockworlds$sortingLevels) {
            return;
        }

        List<LevelSummary> sorted = new ArrayList<>(levels);
        sorted.sort((left, right) -> Boolean.compare(
                LockConfig.get().isLocked(right.getLevelId()),
                LockConfig.get().isLocked(left.getLevelId())
        ));

        lockworlds$sortingLevels = true;
        try {
            ((WorldSelectionListInvoker) this).lockworlds$invokeFillLevels(filter, sorted);
        } finally {
            lockworlds$sortingLevels = false;
        }
        ci.cancel();
    }
}
