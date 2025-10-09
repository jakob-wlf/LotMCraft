package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SealingAbility extends AbilityItem {
    public SealingAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    private final DustParticleOptions dustOptions = new DustParticleOptions(new Vector3f(120 / 255f, 208 / 255f, 245 / 255f), 3f);
    private final DustParticleOptions dustOptions2 = new DustParticleOptions(new Vector3f(224 / 255f, 120 / 255f, 245 / 255f), 2.5f);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        int radius = 5;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 20, 2);

        List<LivingEntity> sealedEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, targetLoc, radius, false);
        sealedEntities.forEach(e -> {
            BeyonderData.addModifier(e, "sealed", .5);
            if(BeyonderData.isBeyonder(e) && BeyonderData.getSequence(e) > BeyonderData.getSequence(entity)) {
                BeyonderData.disableAbilityUse(e, "sealed");
            }
            if(!(e instanceof Player) && !BeyonderData.isBeyonder(e) && e instanceof Mob mob) {
                mob.setNoAi(true);
            }
        });

        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1f, 1f);

        ServerScheduler.scheduleForDuration(0, 4, 20 * 14, () -> {
            ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.END_ROD, targetLoc, radius, 100);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, dustOptions, targetLoc, radius, 80);
            ParticleUtil.spawnSphereParticles((ServerLevel) level, dustOptions2, targetLoc, radius, 60);

            sealedEntities.forEach(e -> {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false, false));
                e.setDeltaMovement(new Vec3(0, 0, 0));
                e.hurtMarked = true;
                ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.STAR.get(), e.getEyePosition().subtract(0, .5, 0), 15, .4, .9, .4, .05);
            });
        });

        ServerScheduler.scheduleDelayed(20 * 14, () -> {
            sealedEntities.forEach(e -> {
                BeyonderData.removeModifier(e, "sealed");
                BeyonderData.enableAbilityUse(e, "sealed");
                if(!(e instanceof Player) && !BeyonderData.isBeyonder(e) && e instanceof Mob mob) {
                    mob.setNoAi(false);
                }
            });
        });
    }
}
