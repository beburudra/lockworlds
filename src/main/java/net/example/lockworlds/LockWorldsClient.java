package net.example.lockworlds;

import net.example.lockworlds.config.LockConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockWorldsClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("lockworlds");
    public static final Identifier LOCK_SOUND_ID = Identifier.fromNamespaceAndPath("lockworlds", "lock");
    public static final SoundEvent LOCK_SOUND_EVENT = SoundEvent.createVariableRangeEvent(LOCK_SOUND_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[LockWorlds] Initializing Lock Worlds mod (MC 26.2).");
        LockConfig.get();
        LOGGER.info("[LockWorlds] Config loaded. Ready.");
    }

    public static void playLockSound() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;

        client.getSoundManager().play(SimpleSoundInstance.forUI(LOCK_SOUND_EVENT, 1.0F, 1.0F));
    }
}
