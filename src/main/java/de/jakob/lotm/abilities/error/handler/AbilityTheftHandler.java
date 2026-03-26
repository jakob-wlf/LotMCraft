package de.jakob.lotm.abilities.error.handler;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

public class AbilityTheftHandler {
    //protected final Random random = new Random();

    public static void performTheft(Level level, LivingEntity entity, LivingEntity target, Random random, boolean renderEffects) {

        if (entity instanceof ServerPlayer serverPlayer && renderEffects) {
            EffectManager.playEffect(EffectManager.Effect.ABILITY_THEFT, target.position().x, target.position().y + target.getEyeHeight(), target.position().z, serverPlayer, entity);
        }

        if (!BeyonderData.isBeyonder(target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.not_beyonder").withColor(0x6d32a8));
            return;
        }

        // Get stealable abilities from the target
        HashSet<Ability> targetAbilities = LOTMCraft.abilityHandler.getByPathwayAndSequence(
                BeyonderData.getPathway(target), BeyonderData.getSequence(target));

        DisabledAbilitiesComponent disabledComponent = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);

        ArrayList<Ability> stealableAbilities = new ArrayList<>(targetAbilities.stream()
                .filter(ability -> !ability.cannotBeStolen
                        && !disabledComponent.isSpecificAbilityDisabled(ability.getId()))
                .toList());

        HashSet<Ability> entityAbilities = LOTMCraft.abilityHandler.getByPathwayAndSequence(
                BeyonderData.getPathway(entity), BeyonderData.getSequence(entity));

        if (stealableAbilities.isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_abilities").withColor(0x6d32a8));
            return;
        }

        if (AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_abilities").withColor(0x6d32a8));
            return;
        }

        if (doesTheftFail(entity, target, random)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_abilities").withColor(0x6d32a8));
            return;
        }

        int sequence = BeyonderData.getSequence(entity);
        int abilityCount = getAbilityCountForSequence(sequence);
        int abilityUses = getAbilityUsesForSequence(sequence);
        int disableTime = getDisablingTimeForSequenceInSeconds(sequence);

        for (int i = 0; i < abilityCount; i++) {
            if (stealableAbilities.isEmpty()) break;

            int index = random.nextInt(stealableAbilities.size());
            Ability stolenAbility = stealableAbilities.get(index);
            stealableAbilities.remove(index);

            // Disable the ability on the target for the duration
            disabledComponent.disableSpecificAbilityForTime(stolenAbility.getId(), "theft_" + entity.getUUID(), disableTime * 20);

            // Add to the thief's copied abilities
            if (entity instanceof ServerPlayer player) {
                if(entityAbilities.contains(stolenAbility))
                    continue;

                CopiedAbilityHelper.addAbility(player,
                        new CopiedAbilityComponent.CopiedAbilityData(
                                stolenAbility.getId(),
                                "stolen",
                                abilityUses,
                                target.getUUID().toString()
                        ));
            }
        }

        if (entity instanceof ServerPlayer) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.success").withColor(0x6d32a8));
        }
    }

    public static int getAbilityCountForSequence(int sequence) {
        return switch (sequence) {
            default -> 1;
            case 4 -> 2;
            case 3 -> 3;
            case 2 -> 4;
            case 1 -> 5;
        };
    }

    public static int getDisablingTimeForSequenceInSeconds(int sequence) {
        return switch (sequence) {
            default -> 35;
            case 5 -> 60;
            case 4 -> 120;
            case 3 -> 240;
            case 2 -> 480;
            case 1 -> 800;
        };
    }

    public static boolean doesTheftFail(LivingEntity user, LivingEntity target, Random random) {
        int userSeq = BeyonderData.getSequence(user);
        int targetSeq = BeyonderData.getSequence(target);

        if (targetSeq > userSeq) {
            return false;
        }

        int difference = targetSeq - userSeq;

        int userLuck = user.hasEffect(ModEffects.LUCK) ? Objects.requireNonNull(user.getEffect(ModEffects.LUCK)).getAmplifier() : 0;
        int targetLuck = target.hasEffect(ModEffects.LUCK) ? Objects.requireNonNull(target.getEffect(ModEffects.LUCK)).getAmplifier() : 0;

        int userUnLuck = user.hasEffect(ModEffects.UNLUCK) ? Objects.requireNonNull(user.getEffect(ModEffects.UNLUCK)).getAmplifier() : 0;
        int targetUnLuck = target.hasEffect(ModEffects.UNLUCK) ? Objects.requireNonNull(target.getEffect(ModEffects.UNLUCK)).getAmplifier() : 0;

        int luckDiff = userLuck - targetLuck;
        int unLuckDiff = userUnLuck - targetUnLuck;

        double luckMultiplier = (double) (luckDiff - unLuckDiff) / 10;

        double baseFailPerStep = 0.15;

        double failChance = difference * baseFailPerStep - luckMultiplier;

        failChance = Math.min(Math.max(failChance, 0.0), 0.95);

        return random.nextDouble() < failChance;
    }


    public static int getAbilityUsesForSequence(int sequence) {
        return switch (sequence) {
            default -> 1;
            case 5 -> 5;
            case 4, 3 -> 10;
            case 2, 1 -> 20;
        };
    }
}
