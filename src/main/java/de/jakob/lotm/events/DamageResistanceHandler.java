package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
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
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DamageResistanceHandler {

    private static Map<UUID, Holder<DamageType>> damageMap = new ConcurrentHashMap<>(300);

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        var entity = event.getEntity();
        var source = event.getSource();
        float damage = event.getAmount();

        var sourceEntity = source.getEntity();

        if (sourceEntity != null && (sourceEntity instanceof LivingEntity livingSource
                && BeyonderData.isBeyonder(livingSource))) {

            float mult = 1f;
            int sourceSeq = AuthorityResistanceManager.getFromBuffer(livingSource);
            AuthorityResistanceManager.removeFromBuffer(livingSource);

            if (BeyonderData.isBeyonder(entity) || entity instanceof ServerPlayer) {

                int entitySeq = BeyonderData.getSequence(entity);

                float baseStep;

                if (entitySeq >= 5 && sourceSeq >= 5)
                    baseStep = 0.1f;
                else if (entitySeq >= 3 && sourceSeq >= 3)
                    baseStep = 0.6f;
                else if(entitySeq >= 1 && sourceSeq >= 1)
                    baseStep = 0.7f;
                else
                    baseStep = 0.9f;


                if(CullAbility.active.contains(livingSource.getUUID()) && sourceSeq > entitySeq){
                    baseStep /= 2;
                }

                int seqDifference = entitySeq - sourceSeq;
                mult = 1.0f + (baseStep * seqDifference);

                if (mult <= 0.0f) {
                    mult = 0f;
                }
            } else {
                if (sourceSeq <= 4) {
                    mult = 10f;
                }
            }

            LOTMCraft.LOGGER.info("AUTHORITY: godhood target seq {} - path {}",BeyonderData.getSequence(entity), BeyonderData.getPathway(entity));
            LOTMCraft.LOGGER.info("AUTHORITY: godhood mult: {}, damage: {}", mult, damage);

            damage *= mult;
        }

        if (BeyonderData.isBeyonder(entity)) {

            int seq = BeyonderData.getSequence(entity);

            float mult = 1.0f;
            switch (seq) {
                case 4 -> mult = 0.75f;
                case 3 -> mult = 0.5f;
                case 0, 1, 2 -> mult = 0f;
            }

            if (!ModDamageTypes.isModDamage(source) && !(damage >= Float.MAX_VALUE / 2)) {
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

        if (storedDamageType != null) {
            if (ModDamageTypes.isModDamage(source)
                    && ModDamageTypes.isModDamage(storedDamageType)
                    && !storedDamageType.is(Objects.requireNonNull(source.typeHolder().getKey()))) {
                entity.invulnerableTime = 0;
                entity.hurtTime = 0;
            }
        }

        damageMap.put(entity.getUUID(), source.typeHolder());

        if(sourceEntity != null && sourceEntity instanceof LivingEntity livingSource){
            livingSource.setLastHurtMob(entity);
        }

        if (damage <= 0f) {
            event.setCanceled(true);
        }
    }


    //Hand Damage
    private static final Map<String, List<Float>> physicalDamage = new HashMap<>(22);

    static{
        List<Float> tyrant = new LinkedList<>(List.of(4f, 3f, 3f,  2.5f, 2f, 1.5f, 1.25f, 1f, 0.75f, 0.5f));
        List<Float> visionary = new LinkedList<>(List.of(2.5f, 2.25f, 2.25f, 2f, 1.75f, 1f, 0.75f, 0.5f));
        List<Float> wof = new LinkedList<>(List.of(2f, 1.5f, 1.5f, 1.25f, 1f, 0.75f));
        List<Float> sun = new LinkedList<>(List.of(2.5f, 2.25f, 2.25f, 2f, 1.75f, 1f, 0.75f, 0.5f, 0.25f));
        List<Float> hunter = new LinkedList<>(List.of(5f, 4f, 4f, 3.5f, 3f, 2.5f, 2.25f, 1.75f, 1f, 0.75f));
        List<Float> mother = new LinkedList<>(List.of(2.5f, 2.25f, 2.25f, 2f, 1.75f, 1f, 0.75f, 0.5f));
        List<Float> fool = new LinkedList<>(List.of(2f, 1.5f, 1.5f, 1.25f, 1f, 0.75f));
        List<Float> error = new LinkedList<>(List.of(2f, 1.5f, 1.5f, 1.25f, 1f, 0.75f));
        List<Float> door = new LinkedList<>(List.of(2f, 1.5f, 1.5f, 1.25f, 1f, 0.75f));

        physicalDamage.put("tyrant", tyrant);
        physicalDamage.put("visionary", visionary);
        physicalDamage.put("wheel_of_fortune", wof);
        physicalDamage.put("sun", sun);
        physicalDamage.put("red_priest", hunter);
        physicalDamage.put("mother", mother);
        physicalDamage.put("fool", fool);
        physicalDamage.put("error", error);
        physicalDamage.put("door", door);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel level)) return;

        LivingEntity entity = event.getEntity();

        if (!(event.getTarget() instanceof LivingEntity target))
            return;

        int seq = BeyonderData.getSequence(entity);
        String path = BeyonderData.getPathway(entity);

        var list = physicalDamage.get(path);

        if(list == null || seq + 1 > list.size())
            return;

        float damage = list.get(seq);

        target.hurt(ModDamageTypes.source(level, ModDamageTypes.IMPACT, entity), damage);
    }

    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if(!BeyonderData.isBeyonder(entity)) return;

        var component = entity.getData(ModAttachments.REGEN_DISABLER.get());
        if(component.isDisabled()){
            event.setAmount(0);
            event.setCanceled(true);
        }
    }
}
