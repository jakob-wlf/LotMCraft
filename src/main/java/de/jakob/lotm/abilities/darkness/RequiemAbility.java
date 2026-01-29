package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.sound.ModSounds;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RequiemAbility extends Ability {
    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), .5f);
    private final DustParticleOptions bigDust = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 1f);
    private static final HashSet<UUID> pacifiedEntities = new HashSet<>();

    public RequiemAbility(String id) {
        super(id, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 45;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        for(int i = 0; i < 8; i++) {
            Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(),  random.nextDouble(-4.5, 3f), random.nextDouble(-7, 7), random.nextDouble(-2, 5));
            Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 16, 1.4f);

            final float step = .15f;
            final float length = (float) startPos.distanceTo(targetLoc);
            final int duration = (int) Math.ceil(length / step) + 20 * 3;

            animateParticleLine(new Location(startPos, level), targetLoc, 2, 0, duration);
        }

        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.MIDNIGHT_POEM.get(), SoundSource.BLOCKS, 1, 1);
        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16, 2);
        if(targetEntity == null)
            return;

        if(pacifiedEntities.contains(targetEntity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("Entity is already pacified.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        pacifiedEntities.add(targetEntity.getUUID());
        int duration = 20 * 15;
        if(AbilityUtil.isTargetSignificantlyStronger(entity, targetEntity)) {
            duration = 35;
        }
        if(AbilityUtil.isTargetSignificantlyWeaker(entity, targetEntity)) {
            duration = 20 * 65;
        }

        if(!BeyonderData.isBeyonder(targetEntity) || BeyonderData.getSequence(targetEntity) - 1 > BeyonderData.getSequence(entity)) {
            if(targetEntity instanceof Mob) {
                ((Mob) targetEntity).setNoAi(true);
                ServerScheduler.scheduleDelayed(duration, () -> ((Mob) targetEntity).setNoAi(false));
            }
            if(BeyonderData.isBeyonder(targetEntity)) {
                BeyonderData.disableAbilityUse(targetEntity, "requiem");
                ServerScheduler.scheduleDelayed(duration, () -> BeyonderData.enableAbilityUse(targetEntity, "requiem"));
            }
        }

        Location loc = new Location(targetEntity.position(), targetEntity.level());

        int finalDuration = duration;
        ServerScheduler.scheduleDelayed(20, () -> {
            ParticleUtil.createParticleSpirals(bigDust, loc, .8, .8, targetEntity.getEyeHeight(), .35, 5, finalDuration, 15, 5);
        });

        ServerScheduler.scheduleForDuration(0, 5, duration, () -> {
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 10, false, false, false));
            targetEntity.setDeltaMovement(new Vec3(0, 0, 0));
            targetEntity.hurtMarked = true;

            loc.setLevel(targetEntity.level());
            loc.setPosition(targetEntity.position());
        });


        ServerScheduler.scheduleDelayed(duration, () -> pacifiedEntities.remove(targetEntity.getUUID()));
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
