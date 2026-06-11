package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.CogitationAbility;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.BeyonderComponent;
import de.jakob.lotm.attachments.CorruptionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.pathways.PathwayInfos;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@EventBusSubscriber
public class CorruptionEventHandler {

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide || entity.tickCount % 20 != 0) {
            return;
        }

        handleCorruptionGain(entity);
        handleCorruptionEffects(entity);
    }

    private static void handleCorruptionGain(LivingEntity entity) {
        CorruptionComponent corruptionComp = entity.getData(ModAttachments.CORRUPTION_COMPONENT);
        BeyonderComponent beyonderComp = entity.getData(ModAttachments.BEYONDER_COMPONENT);

        String currentPathway = beyonderComp.getPathway();
        int currentSequence = beyonderComp.getSequence();

        // Check for Cogitation
        Ability cogitation = LOTMCraft.abilityHandler.getById("cogitation_ability");
        boolean isCogitating = false;
        if (cogitation instanceof CogitationAbility toggleCogitation) {
            if (toggleCogitation.isActiveForEntity(entity)) {
                isCogitating = true;
            }
        }

        if (isCogitating) {
            // Cogitation decreases corruption very slightly
            corruptionComp.increaseCorruptionAndSync(-0.0001f, entity);
            return;
        }

        float totalGain = 0;
        float digestionProgress = beyonderComp.getDigestionProgress(); // 0 to 1

        PathwayInfos currentPathwayInfo = BeyonderData.pathwayInfos.get(currentPathway);
        List<String> neighboring = currentPathwayInfo != null ? Arrays.asList(currentPathwayInfo.neighboringPathways()) : List.of();

        for (Characteristic characteristic : beyonderComp.getCharacteristicList()) {
            String charPathway = characteristic.pathway();
            int charSeq = characteristic.sequence();
            int charStack = characteristic.stack();

            // We consider the "main" characteristics as the one belonging to the current pathway and current sequence.
            // Everything else is "extra".
            // Actually, if you are a sequence 5 fool, you should have characteristics for fool 9, 8, 7, 6, 5.
            // If you have more than 1 of fool 5, it's extra.
            // If you have fool 4, it's extra (and likely dangerous).
            
            int expectedStack = (charPathway.equals(currentPathway) && charSeq >= currentSequence) ? 1 : 0;
            int extraStack = Math.max(0, charStack - expectedStack);

            if (extraStack > 0) {
                float baseGain = 0.00001f * extraStack; // Base gain per extra characteristic

                if (charPathway.equals(currentPathway)) {
                    // Same pathway, extra characteristic
                    totalGain += baseGain;
                } else if (neighboring.contains(charPathway)) {
                    // Neighboring pathway
                    totalGain += baseGain * 2.0f;
                } else {
                    // Non-neighboring pathway
                    totalGain += baseGain * 10.0f;
                }
            }
        }

        if (totalGain > 0) {
            // Scales with digestion but never fully goes away.
            // At 100% digestion (1.0), gain is reduced but still present.
            // Let's say at 100% digestion it's reduced by 50%.
            float digestionMultiplier = 1.0f - (digestionProgress * 0.5f);
            corruptionComp.increaseCorruptionAndSync(totalGain * digestionMultiplier, entity);
        }
    }

    private static void handleCorruptionEffects(LivingEntity entity) {
        CorruptionComponent corruptionComp = entity.getData(ModAttachments.CORRUPTION_COMPONENT);
        float corruption = corruptionComp.getCorruption();
        int corruptionValue = (int) (corruption * 100);

        if (corruptionValue <= 20) {
            return;
        }

        Random random = new Random();

        // PHASE 1: Corruption 21-35 - Early symptoms
        if (corruptionValue <= 35) {
            if (random.nextInt(10) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
            }
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 0, false, true));
        }
        // PHASE 2: Corruption 36-50 - Moderate symptoms
        else if (corruptionValue <= 50) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 1, false, true));

            if (random.nextInt(3) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
            }
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, true));

            if (random.nextInt(30) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 1.0f);
            }
        }
        // PHASE 3: Corruption 51-65 - Severe symptoms
        else if (corruptionValue <= 65) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 2, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1, false, true));

            if (random.nextInt(5) == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
            }

            if (entity instanceof Player player && random.nextInt(10) == 0) {
                player.getFoodData().addExhaustion(2.0f);
            }

            if (random.nextInt(20) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 2.0f);
            }
        }
        // PHASE 4: Corruption 66-85 - Critical state
        else if (corruptionValue <= 85) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 2, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 3, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 2, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false));

            if (random.nextInt(10) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 3.0f);
            }
        }
        // PHASE 5: Corruption 86-100 - Near complete corruption
        else {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 3, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 4, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 1, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 3, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 25, 1, false, true));

            if (random.nextInt(5) == 0) {
                entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 4.0f);
            }
        }
    }
}
