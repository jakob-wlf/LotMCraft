package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AnimationUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class HolyLightAbility extends Ability {
    public HolyLightAbility(String id) {
        super(id, 1.25f, "purification", "light_source", "light_weak");
        postsUsedAbilityEventManually = true;
        interactionRadius = 6;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 1, 1, 1, 2, 2, 2, 2));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(2000f, 1000f, 670f, 380f, 325f, 237f, 170f, 150f, 50f));

        baseDamage = 3;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("sun", 8);
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 35;
    }

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            2f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        Vec3 initialPos = AbilityUtil.getTargetLocation(entity, baseDistance, .75f).add(0, 14, 0);

        List<BlockPos> lights = new ArrayList<>();

        if (!level.isClientSide) {
            AtomicReference<Vec3> currentPos = new AtomicReference<>(initialPos);

            level.playSound(null, initialPos.x, initialPos.y - 14, initialPos.z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 3.0f, 1.0f);

            int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

            ServerScheduler.scheduleForDuration(0, 1, 18, () -> {
                Vec3 pos = currentPos.get();
                ParticleUtil.spawnCircleParticles((ServerLevel) level, ParticleTypes.FIREWORK, pos, 1.4, 20);
                ParticleUtil.spawnCircleParticles((ServerLevel) level, dustOptions, pos, 1.4, 20);

                BlockPos blockPos = BlockPos.containing(pos);
                if (level.getBlockState(blockPos).isAir()) {
                    level.setBlockAndUpdate(blockPos, Blocks.LIGHT.defaultBlockState());
                    lights.add(blockPos);
                }

                if(entitySeq <= 4){
                    AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5* (int) multiplier(entity), ModDamageTypes.PURIFICATION, baseDamage * 0.33, pos, true, false, true, 0);
                    AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5* (int) multiplier(entity), ModDamageTypes.LIGHT, baseDamage * 0.34, pos, true, false, true, 0);
                    AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5* (int) multiplier(entity), ModDamageTypes.FAITH, baseDamage * 0.33, pos, true, false, true, 0);
                }
                else{
                    AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5* (int) multiplier(entity), ModDamageTypes.LIGHT, baseDamage, pos, true, false, true, 0);
                }

                currentPos.set(pos.subtract(0, 2.5, 0));
            }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

            ServerScheduler.scheduleDelayed(18, () -> {
                NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, initialPos.subtract(0, 14, 0), entity, this, interactionFlags, interactionRadius, interactionCacheTicks));
            }, ( ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

            ServerScheduler.scheduleDelayed(22, () -> {
                lights.forEach(l -> level.setBlockAndUpdate(l, Blocks.AIR.defaultBlockState()));
            }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

            if(entity instanceof Player player) AnimationUtil.playOpenArmAnimation(player);
        }
    }
}
