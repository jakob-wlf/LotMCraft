package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;

public class FoolingFateAbility extends Ability {

    public FoolingFateAbility() {
        super("fooling_fate", 18);
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
            target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 60, 0));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("lotmcraft.ability.fooling_fate.cursed").withColor(color));
        } else {
            LivingEntity beneficiary = target != null ? target : entity;
            FoolingAbility.FATE_PROTECTIONS.put(beneficiary.getUUID(),
                    new FoolingAbility.FateProtection(entity.getUUID(),
                            serverLevel.getGameTime() + FoolingAbility.FATE_DURATION_TICKS));
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("lotmcraft.ability.fooling_fate.protected").withColor(color));
        }
    }
}
