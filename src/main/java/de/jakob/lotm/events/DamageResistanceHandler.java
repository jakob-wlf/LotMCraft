package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.red_priest.CullAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.AuthorityResistanceManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DamageResistanceHandler {

    private static Map<UUID, Holder<DamageType>> damageMap = new ConcurrentHashMap<>(300);

    @SubscribeEvent
   public static void onDamage(LivingIncomingDamageEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel level)) return;

        var entity = event.getEntity();
        var source = event.getSource();
        float damage = event.getAmount();

        var sourceEntity = source.getEntity();

        if(sourceEntity != null && (sourceEntity instanceof LivingEntity livingSource
                && BeyonderData.isBeyonder(livingSource))){

            float mult = 1f;

            if(BeyonderData.isBeyonder(entity) || entity instanceof ServerPlayer) {

                float baseStep = 0.3f;

                int entitySeq = BeyonderData.getSequence(entity);
                int sourceSeq = BeyonderData.getSequence(livingSource);

                if (entitySeq >= 5 && sourceSeq >= 5)
                    baseStep = 0.1f;

                if(CullAbility.active.contains(livingSource.getUUID())){
                    baseStep /= 2;
                }

                int seqDifference = entitySeq - sourceSeq;
                mult = 1.0f + (baseStep * seqDifference);

                if (mult <= 0.0f) {
                    mult = 0f;
                }
            }
            else{
                if(BeyonderData.getSequence(livingSource) <= 4){
                    mult = 10f;
                }
            }

            LOTMCraft.LOGGER.info("AUTHORITY: godhood target seq {} - path {}",BeyonderData.getSequence(entity), BeyonderData.getPathway(entity));
            LOTMCraft.LOGGER.info("AUTHORITY: godhood mult: {}, damage: {}", mult, damage);

            damage *= mult;
        }

        if(BeyonderData.isBeyonder(entity)) {

            int seq = BeyonderData.getSequence(entity);

            float mult = 1.0f;
            switch (seq){
                case 4 -> mult = 0.75f;
                case 3 -> mult = 0.5f;
                case 0,1,2 -> mult = 0f;
            }

            if(!ModDamageTypes.isModDamage(source) && !(damage >= Float.MAX_VALUE/2)){
                damage *= mult;
            }

            float resistance = AuthorityResistanceManager.getResistance(source,
                    BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));

            LOTMCraft.LOGGER.info("AUTHORITY: res {}, damage {}, result {}", resistance, damage, damage * resistance);

            float result = damage * resistance;

            damage = result;
        }

        event.setAmount(damage);

        var storedDamageType = damageMap.get(entity.getUUID());

        if(storedDamageType != null) {
            if (ModDamageTypes.isModDamage(source)
            && ModDamageTypes.isModDamage(storedDamageType)
                    && !storedDamageType.is(Objects.requireNonNull(source.typeHolder().getKey()))) {
                entity.invulnerableTime = 0;
                entity.hurtTime = 0;
            }
        }

        damageMap.put(entity.getUUID(), source.typeHolder());

        if(damage <= 0f){
            event.setCanceled(true);
        }
    }

}
