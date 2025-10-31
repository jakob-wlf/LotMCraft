package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlantControllingAbility extends SelectableAbilityItem {
    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(40 / 255f, 200 / 255f, 130 / 255f), .7f);
    private static final HashSet<UUID> boundEntities = new HashSet<>();

    public PlantControllingAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 45;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.plants.trap", "ability.lotmcraft.plants.attack"};
    }

    private void entrap(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        int duration = 20 * 20;

        for(int i = 0; i < 12; i++) {
            Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 16, 1.4f);

            double x = random.nextBoolean() ? random.nextDouble(-4.5, -2) : random.nextDouble(2, 4.5);
            double z = random.nextBoolean() ? random.nextDouble(-4.5, -2) : random.nextDouble(2, 4.5);

            Vec3 startPos = targetLoc.add(x, -.5, z);

            animateParticleLine(new Location(startPos, level), targetLoc, 3, 0, duration);
        }

        level.playSound(null, entity, Blocks.GRASS_BLOCK.getSoundType(Blocks.GRASS_BLOCK.defaultBlockState(), level, BlockPos.containing(entity.position().x, entity.position().y, entity.position().z), null).getBreakSound(), SoundSource.BLOCKS, 5,1);
        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16, 2);
        if(targetEntity == null)
            return;

        if(boundEntities.contains(targetEntity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("Entity is already bound by your plants.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        boundEntities.add(targetEntity.getUUID());

        if(!BeyonderData.isBeyonder(targetEntity) || BeyonderData.getSequence(targetEntity) - 1 > BeyonderData.getSequence(entity)) {
            if(targetEntity instanceof Mob) {
                ((Mob) targetEntity).setNoAi(true);
                ServerScheduler.scheduleDelayed(duration, () -> ((Mob) targetEntity).setNoAi(false));
            }
            if(BeyonderData.isBeyonder(targetEntity)) {
                BeyonderData.disableAbilityUse(targetEntity, "plant");
                ServerScheduler.scheduleDelayed(duration, () -> BeyonderData.enableAbilityUse(targetEntity, "requiem"));
            }
        }

        ServerScheduler.scheduleForDuration(0, 5, duration, () -> {
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 10, false, false, false));
            targetEntity.setDeltaMovement(new Vec3(0, 0, 0));
            targetEntity.hurtMarked = true;
        });


        ServerScheduler.scheduleDelayed(duration, () -> boundEntities.remove(targetEntity.getUUID()));
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> entrap(level, entity);
            case 1 -> attack(level, entity);
        }
    }

    private void attack(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 16, 1.4f);
        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(-4, 4), -entity.getEyeHeight());

        float distance = (float) targetLoc.distanceTo(startPos);
        float bezierSteps = .15f / distance;

        List<Vec3> points = VectorUtil.createBezierCurve(startPos, targetLoc, bezierSteps, 5.5f, 5);

        ServerScheduler.scheduleForDuration(0, 0, 30, () -> {
            for (Vec3 point : points) {
                ParticleUtil.spawnParticles((ServerLevel) level, dust, point, 1, 0, 0);
            }
        });

        level.playSound(null, entity, Blocks.GRASS_BLOCK.getSoundType(Blocks.GRASS_BLOCK.defaultBlockState(), level, BlockPos.containing(entity.position().x, entity.position().y, entity.position().z), null).getBreakSound(), SoundSource.BLOCKS, 5,1);
        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16, 2);
        if(targetEntity == null)
            return;

        targetEntity.hurt(entity.damageSources().mobAttack(entity), (float) (18.5f * multiplier(entity)));
    }

    private void animateParticleLine(Location startLoc, Vec3 end, int step, int interval, int duration) {
        if(!(startLoc.getLevel() instanceof ServerLevel level))
            return;
        AtomicInteger tick = new AtomicInteger(0);

        float distance = (float) end.distanceTo(startLoc.getPosition());
        float bezierSteps = .15f / distance;

        List<Vec3> points = VectorUtil.createBezierCurve(startLoc.getPosition(), end, bezierSteps, 2.5f, 1);

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            for(int i = 0; i < Math.min(tick.get(), points.size() - step); i+=step) {
                for(int j = 0; j < step; j++) {
                    boolean shouldSpawn = tick.get() < duration || random.nextInt(3) == 0;
                    if(shouldSpawn)
                        ParticleUtil.spawnParticles(level, dust, points.get(i + j), 0, 0, 0);
                }
            }

            tick.addAndGet(1);
        });
    }
}
