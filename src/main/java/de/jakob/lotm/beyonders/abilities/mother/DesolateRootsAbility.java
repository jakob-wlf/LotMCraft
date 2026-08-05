package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DesolateRootsAbility extends Ability {
    private static final DustParticleOptions ROOT_DUST = new DustParticleOptions(new Vector3f(0.33f, 0.22f, 0.11f), 1.8f);
    public DesolateRootsAbility(String id) { super(id, 15); canBeShared = false; }
    @Override public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("mother", 2)); }
    @Override public float getSpiritualityCost() { return 1600; }
    @Override public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 center = AbilityUtil.getTargetLocation(entity, 36, 1.5f, true); List<LivingEntity> targets = AbilityUtil.getNearbyEntities(entity, serverLevel, center, 14);
        serverLevel.playSound(null, BlockPos.containing(center), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.2f, 0.6f);
        ServerScheduler.scheduleForDuration(0, 3, 45, () -> {
            for (int i = 0; i < 14; i++) { Vec3 pos = center.add(serverLevel.random.nextDouble(-8, 8), 0.1, serverLevel.random.nextDouble(-8, 8)); ParticleUtil.drawParticleLine(serverLevel, ROOT_DUST, pos, pos.add(0, 6 + serverLevel.random.nextDouble(3), 0), 10, 0.03); }
            for (LivingEntity target : targets) { if (target == entity) continue; target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.MOTHER_GENERIC, entity), (float) (DamageLookup.lookupDamage(3, 1.0) * multiplier(entity))); target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 4, false, false, false)); target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 2, false, false, false)); target.invulnerableTime = 0; }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel)));
        if (BeyonderData.isGriefingEnabled(entity)) for (BlockPos pos : AbilityUtil.getBlocksInCircle(serverLevel, center, 10, 30)) if (serverLevel.getBlockState(pos).is(Blocks.GRASS_BLOCK) || serverLevel.getBlockState(pos).is(Blocks.DIRT)) serverLevel.setBlockAndUpdate(pos, Blocks.ROOTED_DIRT.defaultBlockState());
    }
}
