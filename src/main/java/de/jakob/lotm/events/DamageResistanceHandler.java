package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.AuthorityResistanceManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DamageResistanceHandler {

    @SubscribeEvent
   public static void onDamage(LivingIncomingDamageEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel level)) return;

        var entity = event.getEntity();
        var source = event.getSource();
        float damage = event.getAmount();

        var sourceEntity = source.getEntity();
        if(sourceEntity != null && (sourceEntity instanceof LivingEntity livingSource
                && BeyonderData.isBeyonder(livingSource))){
            LOTMCraft.LOGGER.info("AUTHORITY: source path {}, seq {}", BeyonderData.getPathway(livingSource), BeyonderData.getSequence(livingSource));

            float baseStep = 0.3f;
            int seqDifference = BeyonderData.getSequence(entity) - BeyonderData.getSequence(livingSource);
            float mult = 1.0f + (baseStep * seqDifference);

            if(mult <= 0.0f){
                mult = 0f;
            }

            LOTMCraft.LOGGER.info("AUTHORITY: damage before {}, mult {}, damage after {}", damage, mult, damage * mult);

            damage *= mult;
        }

        if(BeyonderData.isBeyonder(entity)) {

            LOTMCraft.LOGGER.info("AUTHORITY: entity path {}, seq {}", BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));

            float resistance = AuthorityResistanceManager.getResistance(source,
                    BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));

            float result = damage * resistance;

            LOTMCraft.LOGGER.info("AUTHORITY: resistance {}, damage {}, res {}", resistance, damage, result);

            damage = result;
        }


        event.setAmount(damage);

        if(damage <= 0f){
            event.setCanceled(true);
        }
    }

}
