package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.AuthorityResistanceManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DamageResistanceHandler {

    @SubscribeEvent
   public static void onSunHitDigestion(LivingDamageEvent.Pre event) {
        if(!(event.getEntity().level() instanceof ServerLevel level)) return;

        var entity = event.getEntity();
        var source = event.getSource();

        if(!BeyonderData.isBeyonder(entity)) return;

        LOTMCraft.LOGGER.info("AUTHORITY: path {}, seq {}", BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));

        float resistance = AuthorityResistanceManager.getResistance(source,
                BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));
        float damage = event.getOriginalDamage();
        float result = damage * resistance;

        LOTMCraft.LOGGER.info("AUTHORITY: resistance {}, damage {}, res {}", resistance, damage, result);

        event.setNewDamage(result);
    }

}
