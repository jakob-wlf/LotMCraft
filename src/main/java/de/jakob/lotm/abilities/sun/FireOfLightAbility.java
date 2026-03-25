package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class FireOfLightAbility extends Ability {
    public FireOfLightAbility(String id) {
        super(id, .75f, "purification", "burning", "light_source", "light_weak");
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("sun", 7);
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 23;
    }

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            2f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 10, 1.4f);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 2.0f, .5f);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), .4f, .5f);

        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.HOLY_FLAME.get(), targetPos, 140, .4, .04);
        ParticleUtil.spawnParticles((ServerLevel) level, dustOptions, targetPos, 90, .75, 0);

        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5, DamageLookup.lookupDamage(7, .75) * multiplier(entity), targetPos, true, false, true, 0, 20 * 2, ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, entity));

        BlockState block = level.getBlockState(BlockPos.containing(targetPos));
        if(block.isAir()) {
            level.setBlockAndUpdate(BlockPos.containing(targetPos), Blocks.LIGHT.defaultBlockState());
        }

        ServerScheduler.scheduleDelayed(25, () -> level.setBlockAndUpdate(BlockPos.containing(targetPos), Blocks.AIR.defaultBlockState()));
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, targetPos, entity, this, interactionFlags, interactionRadius, interactionCacheTicks));
    }
}
