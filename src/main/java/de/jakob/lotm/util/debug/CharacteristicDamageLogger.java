package de.jakob.lotm.util.debug;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class CharacteristicDamageLogger {

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        int charCount = BeyonderData.getCurrentCharacteristicCount(entity);
        // Only log when there are "extra" characteristics (more than 1)
        if (charCount <= 1) return;

        // Post damage — event doesn't expose amount reliably for this event type, log health after hit instead
        float healthAfter = entity.getHealth();

        LOTMCraft.LOGGER.warn("[CharDamageLogger] Damage event for Beyonder with extra characteristics: uuid={}, name={}, charCount={}, healthAfter={}, source={}",
                entity.getUUID(), entity.getDisplayName().getString(), charCount, healthAfter, event.getSource());

        // Log a stacktrace to help locate the origin of the damage call
        LOTMCraft.LOGGER.warn("[CharDamageLogger] Stacktrace for damage call:", new Exception("Damage stacktrace"));
    }
}
