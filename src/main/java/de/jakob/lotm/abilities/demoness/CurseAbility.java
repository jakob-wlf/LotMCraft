package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CurseAbility extends Ability {
    public CurseAbility(String id) {
        super(id, 1.5f);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 300;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(entity instanceof ServerPlayer player)) {
            return;
        }

        ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);

        if(!offHandItem.is(ModItems.BLOOD)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.curse.no_blood").withColor(0x6d32a8));
            return;
        }

        String ownerStr = offHandItem.getOrDefault(ModDataComponents.BLOOD_OWNER, "");

        UUID targetUUID = ownerStr.isEmpty() ? null : UUID.fromString(ownerStr);

        if(targetUUID == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.curse.no_target").withColor(0x6d32a8));
            offHandItem.consume(1, player);
            return;
        }

        Entity target = serverLevel.getEntity(targetUUID);
        if(!(target instanceof LivingEntity livingTarget)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.curse.no_target").withColor(0x6d32a8));
            offHandItem.consume(1, player);
            return;
        }

        if(AbilityUtil.isTargetSignificantlyStronger(entity, livingTarget)) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 3));
            entity.hurt(entity.damageSources().generic(), 10);
            return;
        }

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.curse.cursed_target").withColor(0x6d32a8));
        offHandItem.consume(1, player);

        ServerScheduler.scheduleForDuration(0, 8, 20 * 60 * 2, () -> {
            if (livingTarget.isDeadOrDying()) {
                return;
            }
            switch(random.nextInt(3)) {
                case 0 -> {
                    livingTarget.hurt(livingTarget.damageSources().onFire(), (float) (DamageLookup.lookupDamage(4, .6) * multiplier(entity)));
                    ParticleUtil.spawnParticles(serverLevel, ModParticles.BLACK_FLAME.get(), livingTarget.position().add(0, livingTarget.getEyeHeight() / 2, 0), 200, .4, livingTarget.getEyeHeight() / 2, .4, 0.01);
                }
                case 1 -> {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 3));
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 2, 3));
                }
            }
        }, serverLevel);
    }
}
