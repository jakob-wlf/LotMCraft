package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class AweAbility extends Ability {
    public AweAbility(String id) {
        super(id, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 7));
    }

    @Override
    public float getSpiritualityCost() {
        return 40;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            5f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 1, 1);
        ParticleUtil.spawnParticles((ServerLevel) level, dust, entity.position(), 1300, 17, 3, 17, 0);

        AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, entity, 25, entity.position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 11, false, false, false), new MobEffectInstance(MobEffects.WEAKNESS, 20 * 10, 6, false, false, false));
        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 25, DamageLookup.lookupDamage(7, .675) * multiplier(entity), entity.position(), true, false);
        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 25).forEach(e -> {
            if(BeyonderData.isBeyonder(e)) {
                BeyonderData.addModifier(e,"awe", .625);
            }
            ServerScheduler.scheduleForDuration(0, 8, 20 * 10, () -> {
                e.setDeltaMovement((new Vec3(random.nextDouble(-1, 1), random.nextDouble(0, .1), random.nextDouble(-1, 1))).normalize().scale(0.3));
                e.hurtMarked = true;
            }, () -> {
                if(BeyonderData.isBeyonder(e)) {
                    BeyonderData.removeModifier(e, "awe");
                }
            }, (ServerLevel) level);
        });
    }
}
