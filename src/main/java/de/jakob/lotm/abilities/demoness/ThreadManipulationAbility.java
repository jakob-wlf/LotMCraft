package de.jakob.lotm.abilities.demoness;

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
import net.minecraft.sounds.SoundEvents;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadManipulationAbility extends SelectableAbilityItem {

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(.6f, .75f, .75f), .5f);
    private final DustParticleOptions dustBig = new DustParticleOptions(new Vector3f(.6f, .75f, .75f), 1.75f);
    private final DustParticleOptions dustVeryBig = new DustParticleOptions(new Vector3f(.6f, .75f, .75f), 2.5f);
    private static final HashSet<UUID> boundEntities = new HashSet<>();
    private static final HashSet<UUID> inCocoon = new HashSet<>();

    public ThreadManipulationAbility(Properties properties) {
        super(properties, 1.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 45;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.threads.binding", "ability.lotmcraft.threads.cocoon", "ability.lotmcraft.threads.shoot"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;

        switch (abilityIndex) {
            case 0 -> binding((ServerLevel) level, entity);
            case 1 -> cocoon((ServerLevel) level, entity);
            case 2 -> shoot((ServerLevel) level, entity);
        }
    }

    private void cocoon(ServerLevel level, LivingEntity entity) {
        if(inCocoon.contains(entity.getUUID())) {
            inCocoon.remove(entity.getUUID());
            return;
        }

        inCocoon.add(entity.getUUID());

        Location loc = new Location(entity.position(), level);
        List<AtomicBoolean> stopConditions = ParticleUtil.createParticleCocoons(dustVeryBig, loc, .25, 1.45, entity.getEyeHeight() + 1.4, .4, 5, 20 * 20, 22, 5);
        ServerScheduler.scheduleForDuration(0, 5, 20 * 20, () -> {
            if(!inCocoon.contains(entity.getUUID())) {
                stopConditions.forEach(b -> b.set(true));
                return;
            }

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 10, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 3, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 6, 2, false, false, false));
            entity.setDeltaMovement(new Vec3(0, 0, 0));
            entity.hurtMarked = true;

            loc.setPosition(entity.position());
            loc.setLevel(entity.level());
        }, () -> inCocoon.remove(entity.getUUID()), level);
    }

    private void shoot(ServerLevel level, LivingEntity entity) {
        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(-.65, .65), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 10, 1.4f).subtract(startPos).normalize();

        AtomicReference<Vec3> currentPos = new AtomicReference<>(startPos);

        AtomicBoolean hasHit = new AtomicBoolean(false);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.SNOWBALL_THROW, entity.getSoundSource(), 1.0f, 1.0f);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 20, () -> {
            if(hasHit.get())
                return;

            Vec3 pos = currentPos.get();

            if(AbilityUtil.damageNearbyEntities(level, entity, 2.5f, 14 * multiplier(entity), pos, true, false, true,0)) {
                hasHit.set(true);
                AbilityUtil.addPotionEffectToNearbyEntities(level, entity, 2.5, pos, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 8, 4, false, false, false));
                return;
            }

            if(!level.getBlockState(BlockPos.containing(pos.x, pos.y, pos.z)).isAir()) {
                if(BeyonderData.isGriefingEnabled(entity)) {
                    pos = pos.subtract(direction);
                    level.setBlockAndUpdate(BlockPos.containing(pos.x, pos.y, pos.z), Blocks.FIRE.defaultBlockState());
                }
                hasHit.set(true);
                return;
            }

            ParticleUtil.spawnParticles(level, dust, pos, 60, 0.1, 0.02);

            currentPos.set(pos.add(direction));
        }, level);
    }

    private void binding(ServerLevel level, LivingEntity entity) {
        for(int i = 0; i < 11; i++) {
            Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(),  random.nextDouble(-4.5, 3f), random.nextDouble(-7, 7), random.nextDouble(-2, 5));
            Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 16, 1.4f);

            final float step = .15f;
            final float length = (float) startPos.distanceTo(targetLoc);
            final int duration = (int) Math.ceil(length / step) + 20 * 3;

            animateParticleLine(new Location(startPos, level), targetLoc, 3, 0, duration);
        }

        level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.SNOWBALL_THROW, SoundSource.BLOCKS, 1, 1);
        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16, 2);
        if(targetEntity == null)
            return;

        if(boundEntities.contains(targetEntity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("Entity is already bound by your threads.").withColor(0xFFaa42f5));
                player.connection.send(packet);
            }
            return;
        }

        boundEntities.add(targetEntity.getUUID());
        int duration = 20 * 20;

        if(!BeyonderData.isBeyonder(targetEntity) || BeyonderData.getSequence(targetEntity) - 1 > BeyonderData.getSequence(entity)) {
            if(targetEntity instanceof Mob) {
                ((Mob) targetEntity).setNoAi(true);
                ServerScheduler.scheduleDelayed(duration, () -> ((Mob) targetEntity).setNoAi(false));
            }
            if(BeyonderData.isBeyonder(targetEntity)) {
                BeyonderData.disableAbilityUse(targetEntity, "threads");
                ServerScheduler.scheduleDelayed(duration, () -> BeyonderData.enableAbilityUse(targetEntity, "requiem"));
            }
        }

        Location loc = new Location(targetEntity.position(), targetEntity.level());

        ServerScheduler.scheduleDelayed(20, () -> ParticleUtil.createParticleSpirals(dustBig, loc, .8, .8, targetEntity.getEyeHeight(), .35, 5, duration, 20, 10));

        ServerScheduler.scheduleForDuration(0, 5, duration, () -> {
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 10, false, false, false));
            targetEntity.setDeltaMovement(new Vec3(0, 0, 0));
            targetEntity.hurtMarked = true;

            loc.setLevel(targetEntity.level());
            loc.setPosition(targetEntity.position());
        });


        ServerScheduler.scheduleDelayed(duration, () -> boundEntities.remove(targetEntity.getUUID()));
    }

    private void animateParticleLine(Location startLoc, Vec3 end, int step, int interval, int duration) {
        if(!(startLoc.getLevel() instanceof ServerLevel level))
            return;
        AtomicInteger tick = new AtomicInteger(0);

        float distance = (float) end.distanceTo(startLoc.getPosition());
        float bezierSteps = .15f / distance;

        int maxPoints = Math.max(2, Math.min(10, (int) Math.ceil(distance * 1.5)));

        List<Vec3> points = VectorUtil.createBezierCurve(startLoc.getPosition(), end, bezierSteps, random.nextInt(1, maxPoints + 1));

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            for(int i = 0; i < Math.min(tick.get(), points.size() - step); i+=step) {
                for(int j = 0; j < step; j++) {
                    ParticleUtil.spawnParticles(level, dust, points.get(i + j), 1, 0, 0);
                }
            }

            tick.addAndGet(1);
        });
    }

}
