package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ConcealmentWorldEventHandler {

    @SubscribeEvent
    public static void EntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!entity.level().dimension().equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY)) {
            return;
        }

        if(!BeyonderData.isBeyonder(entity)) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 10, false, false, false));
            return;
        }

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        if(!pathway.equalsIgnoreCase("darkness") || sequence > 2) {
            BeyonderData.addModifierWithTimeLimit(entity, "concealment_world", .4, 2000);
        }
        else {
            BeyonderData.addModifierWithTimeLimit(entity, "concealment_world", 1.4, 2000);
        }
    }

}
