package net.example.lockworlds.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(WorldSelectionList.class)
public interface WorldSelectionListInvoker {
    @Invoker("fillLevels")
    void lockworlds$invokeFillLevels(String filter, List<LevelSummary> levels);
}
