package net.example.lockworlds.mixin;

import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerSelectionList.class)
public interface ServerSelectionListAccessor {
    @Accessor("screen")
    JoinMultiplayerScreen lockworlds$getScreen();
}
