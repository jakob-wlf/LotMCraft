package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityTheftAbility extends AbilityItem {
    public AbilityTheftAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 95;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) {
            if(entity instanceof Player player) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_target").withColor(0x6d32a8));
            return;
        }

        if(entity instanceof ServerPlayer serverPlayer) {
            EffectManager.playEffect(EffectManager.Effect.ABILITY_THEFT, target.position().x, target.position().y + target.getEyeHeight(), target.position().z, serverPlayer);
        }

        if(!BeyonderData.isBeyonder(target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.not_beyonder").withColor(0x6d32a8));
            return;
        }

        ArrayList<AbilityItem> stealableAbilities = new ArrayList<>(AbilityItemHandler.ITEMS.getEntries().stream().filter(abilityEntry -> {
            if(!(abilityEntry.get() instanceof AbilityItem abilityItem) || abilityEntry.get() instanceof ToggleAbilityItem) return false;
            if(!abilityItem.canBeCopied) return false;
            if(BeyonderData.isSpecificAbilityDisabled(target, abilityItem.getDescriptionId())) return false;

            return abilityItem.canUse(target, true);
        }).map(abilityEntry -> (AbilityItem) abilityEntry.get()).toList());

        if(stealableAbilities.isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_abilities").withColor(0x6d32a8));
            return;
        }

        if(AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_abilities").withColor(0x6d32a8));
            return;
        }

        if(doesTheftFail(BeyonderData.getSequence(entity), BeyonderData.getSequence(target))) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.ability_theft.no_abilities").withColor(0x6d32a8));
            return;
        }

        List<AbilityItem> stolenItems = new ArrayList<>();
        int sequence = BeyonderData.getSequence(entity);
        int abilityCount = getAbilityCountForSequence(sequence);
        int abilityUses = getAbilityUsesForSequence(sequence);
        for(int i = 0; i < abilityCount; i++) {
            if(stealableAbilities.isEmpty()) {
                break;
            }
            int index = random.nextInt(stealableAbilities.size());
            AbilityItem stolenAbility = stealableAbilities.get(index);
            stealableAbilities.remove(index);
            stolenItems.add(stolenAbility);
            BeyonderData.disableSpecificAbilityWithTimeLimit(target, "ability_theft_disable", stolenAbility.getDescriptionId(), getDisablingTimeForSequenceInSeconds(sequence) * 1000L);
        }

        for(AbilityItem stolenItem : stolenItems) {
            ItemStack stolenStack = new ItemStack(stolenItem);
            stolenStack.set(ModDataComponents.ABILITY_USES, abilityUses);
            stolenStack.set(ModDataComponents.IS_STOLEN, true);

            if(entity instanceof Player player) {
                if(!player.getInventory().add(stolenStack)) {
                    player.drop(stolenStack, false);
                }
            }

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
