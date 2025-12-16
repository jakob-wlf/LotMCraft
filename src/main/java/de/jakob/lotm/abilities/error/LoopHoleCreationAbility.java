package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoopHoleCreationAbility extends AbilityItem {
    public LoopHoleCreationAbility(Properties properties) {
        super(properties, 3.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 40, 2);

        if(entity instanceof ServerPlayer serverPlayer) {
            EffectManager.playEffect(EffectManager.Effect.LOOPHOLE, targetLoc.x, targetLoc.y, targetLoc.z, serverPlayer);
        }

        ServerScheduler.scheduleForDuration(0, 2, 20 * 14, () -> {
            AbilityUtil.getNearbyEntities(entity, serverLevel, targetLoc, 3).forEach(e -> {
                stealAbilities(entity, e);

                Vec3 newPos = targetLoc.add((serverLevel.random.nextDouble() - 0.5) * 40, (serverLevel.random.nextDouble() - 0.5) * 40, (serverLevel.random.nextDouble() - 0.5) * 40);
                e.teleportTo(newPos.x, newPos.y, newPos.z);
            });
        });
    }

    private void stealAbilities(LivingEntity entity, LivingEntity target) {
        if(!BeyonderData.isBeyonder(target)) {
            return;
        }

        ArrayList<AbilityItem> stealableAbilities = new ArrayList<>(AbilityItemHandler.ITEMS.getEntries().stream().filter(abilityEntry -> {
            if(!(abilityEntry.get() instanceof AbilityItem abilityItem) || abilityEntry.get() instanceof ToggleAbilityItem) return false;
            if(!abilityItem.canBeCopied) return false;
            if(BeyonderData.isSpecificAbilityDisabled(target, abilityItem.getDescriptionId())) return false;

            return abilityItem.canUse(target, true);
        }).map(abilityEntry -> (AbilityItem) abilityEntry.get()).toList());

        if(stealableAbilities.isEmpty()) {
            return;
        }

        if(AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            return;
        }

        if(doesTheftFail(BeyonderData.getSequence(entity), BeyonderData.getSequence(target))) {
            return;
        }

        if(entity instanceof ServerPlayer serverPlayer)  {
            EffectManager.playEffect(EffectManager.Effect.ABILITY_THEFT, target.position().x, target.position().y + target.getEyeHeight(), target.position().z, serverPlayer);
        }

        List<AbilityItem> stolenItems = new ArrayList<>();
        int abilityCount = 3;
        int abilityUses = 4;
        for(int i = 0; i < abilityCount; i++) {
            if(stealableAbilities.isEmpty()) {
                break;
            }
            int index = random.nextInt(stealableAbilities.size());
            AbilityItem stolenAbility = stealableAbilities.get(index);
            stealableAbilities.remove(index);
            stolenItems.add(stolenAbility);
            BeyonderData.disableSpecificAbilityWithTimeLimit(target, "ability_theft_disable", stolenAbility.getDescriptionId(), 120 * 1000L);
        }

        for(AbilityItem stolenItem : stolenItems) {
            ItemStack stolenStack = new ItemStack(stolenItem);
            stolenStack.set(ModDataComponents.ABILITY_USES, abilityUses);
            stolenStack.set(ModDataComponents.IS_STOLEN, true);


            if(entity instanceof Player player && !player.getInventory().add(stolenStack)) {
                player.drop(stolenStack, false);
            }

        }
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
}
