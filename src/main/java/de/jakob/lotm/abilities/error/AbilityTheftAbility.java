package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AbilityTheftAbility extends SelectableAbility {
    public AbilityTheftAbility(String id) {
        super(id, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 95;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.ability_theft.steal",
                "ability.lotmcraft.ability_theft.use_copied"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (selectedAbility == 0) {
            performTheft(level, entity);
        } else if (selectedAbility == 1) {
            openCopiedAbilityWheel(level, entity);
        }
    }

    private void openCopiedAbilityWheel(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel) || !(entity instanceof ServerPlayer player)) return;
        CopiedAbilityHelper.openCopiedAbilityWheel(player);
    }

    private void performTheft(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel)) {
            if (entity instanceof Player player) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_target").withColor(0x6d32a8));
            return;
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            EffectManager.playEffect(EffectManager.Effect.ABILITY_THEFT, target.position().x, target.position().y + target.getEyeHeight(), target.position().z, serverPlayer);
        }

        if (!BeyonderData.isBeyonder(target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.not_beyonder").withColor(0x6d32a8));
            return;
        }

        // Get stealable abilities from the target
        HashSet<Ability> targetAbilities = LOTMCraft.abilityHandler.getByPathwayAndSequence(
                BeyonderData.getPathway(target), BeyonderData.getSequence(target));

        ArrayList<Ability> stealableAbilities = new ArrayList<>(targetAbilities.stream()
                .filter(ability -> ability.canBeCopied
                        && ability.canUse(target, true, false)
                        && !BeyonderData.isSpecificAbilityDisabled(target, ability.getId()))
                .toList());

        if (stealableAbilities.isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_abilities").withColor(0x6d32a8));
            return;
        }

        if (AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_abilities").withColor(0x6d32a8));
            return;
        }

        if (doesTheftFail(BeyonderData.getSequence(entity), BeyonderData.getSequence(target))) {
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
            BeyonderData.disableSpecificAbilityWithTimeLimit(target, "ability_theft_disable",
                    stolenAbility.getId(), disableTime * 1000L);

            // Add to the thief's copied abilities
            if (entity instanceof ServerPlayer player) {
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

    private static int getAbilityCountForSequence(int sequence) {
        return switch (sequence) {
            default -> 1;
            case 4 -> 2;
            case 3 -> 3;
            case 2 -> 4;
            case 1 -> 5;
        };
    }

    private static int getDisablingTimeForSequenceInSeconds(int sequence) {
        return switch (sequence) {
            default -> 35;
            case 5 -> 60;
            case 4 -> 120;
            case 3 -> 240;
            case 2 -> 480;
            case 1 -> 800;
        };
    }


    private boolean doesTheftFail(int userSeq, int targetSeq) {
        if (targetSeq > userSeq) {
            return false;
        }

        int difference = userSeq - targetSeq;

        double baseFailPerStep = 0.15;

        double failChance = difference * baseFailPerStep;

        failChance = Math.min(Math.max(failChance, 0.0), 0.95);

        return random.nextDouble() < failChance;
    }


    private static int getAbilityUsesForSequence(int sequence) {
        return switch (sequence) {
            default -> 1;
            case 5 -> 5;
            case 4, 3 -> 10;
            case 2, 1 -> 20;
        };
    }
}
