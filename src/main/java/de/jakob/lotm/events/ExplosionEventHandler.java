package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ExplosionEventHandler {

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedEntities().removeIf(entity -> entity instanceof ItemEntity);
    }

}
