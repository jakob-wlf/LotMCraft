package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Map;

public class FoolingBlindStupidityAbility extends Ability {

    public FoolingBlindStupidityAbility() {
        super("fooling_blind_stupidity", 15);
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
        if (!(level instanceof ServerLevel)) return;

        int color = BeyonderData.pathwayInfos.get("fool").color();
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30, 2);

        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("lotmcraft.ability.fooling_blind_stupidity.no_target").withColor(color));
            return;
        }

        target.getData(ModAttachments.FOOLING_COMPONENT).setTicksRemaining(FoolingAbility.BLIND_STUPIDITY_TICKS);
        target.addEffect(new MobEffectInstance(ModEffects.FOOLING, FoolingAbility.BLIND_STUPIDITY_TICKS, 0));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

        if (target instanceof Player playerTarget) {
            FoolingAbility.shuffleInventory(playerTarget);
        }

        AbilityUtil.sendActionBar(entity,
                Component.translatable("lotmcraft.ability.fooling_blind_stupidity.applied").withColor(color));
    }
}
