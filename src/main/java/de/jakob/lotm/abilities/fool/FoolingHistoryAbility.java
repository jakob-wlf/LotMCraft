package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;

public class FoolingHistoryAbility extends Ability {

    public FoolingHistoryAbility() {
        super("fooling_history", 20);
        this.canBeCopied = false;
        this.canBeReplicated = false;
        this.canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("fool", 0);
    }

    @Override
    protected float getSpiritualityCost() {
        return 300;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int color = BeyonderData.pathwayInfos.get("fool").color();
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30, 2, false, true);

        if (target != null && AbilityUtil.mayDamage(entity, target)) {
            int actualSeq = FoolingAbility.getUnmaskedSequence(target);
            int maskedSeq = Mth.clamp(actualSeq + serverLevel.random.nextInt(5) - 2, 1, 9);
            long expiresAt = serverLevel.getGameTime() + FoolingAbility.HISTORY_DURATION_TICKS;
            FoolingAbility.HISTORY_MASKS.put(target.getUUID(),
                    new FoolingAbility.HistoryMask(actualSeq, maskedSeq, expiresAt, entity.getUUID()));
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("lotmcraft.ability.fooling_history.applied").withColor(color));
        } else if (target != null) {
            FoolingAbility.restoreBattleReadyState(target);
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("lotmcraft.ability.fooling_history.restored").withColor(color));
        } else {
            int actualSeq = FoolingAbility.getUnmaskedSequence(entity);
            int maskedSeq = Mth.clamp(actualSeq + 3, 1, 9);
            long expiresAt = serverLevel.getGameTime() + FoolingAbility.HISTORY_DURATION_TICKS;
            FoolingAbility.HISTORY_MASKS.put(entity.getUUID(),
                    new FoolingAbility.HistoryMask(actualSeq, maskedSeq, expiresAt, entity.getUUID()));
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("lotmcraft.ability.fooling_history.applied").withColor(color));
        }
    }
}
