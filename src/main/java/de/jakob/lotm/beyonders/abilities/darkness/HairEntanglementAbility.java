package de.jakob.lotm.beyonders.abilities.darkness;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class HairEntanglementAbility extends Ability {
    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(.2f, .2f, .2f), .5f);
    private static final HashSet<UUID> pacifiedEntities = new HashSet<>();

    public HairEntanglementAbility(String id) {
        super(id, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;
        float multiplier = multiplier(entity);
        for(int i = 0; i < 8; i++) {
            Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(),  random.nextDouble(-4.5, 3f), random.nextDouble(-7, 7), random.nextDouble(-2, 5));
            Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 35*(int) Math.max(multiplier/2,1), 1.4f);

            final float step = .15f;
            final float length = (float) startPos.distanceTo(targetLoc);
            final int duration = (int) Math.ceil(length / step) + 20 * 3;

            animateParticleLine(new Location(startPos, level), targetLoc, 2, 1, duration);
        }

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
        int duration = 0;

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(targetEntity);
        Location eLoc = new Location(targetEntity.position(), (ServerLevel) level);
        if(entitySeq < targetSeq) {
            duration = 20 * 60*(int) multiplier;
        }else if (entitySeq > targetSeq){
            if (!BeyonderData.getPathway(targetEntity).equals("darkness")){
                duration = 35*(int) multiplier;
            };
        }else{
            duration = 20 * 30*(int) multiplier/  (int) multiplier(targetEntity);
        };
        if(targetSeq > entitySeq-1 ) {
            if(targetEntity instanceof Mob) {
                ((Mob) targetEntity).setNoAi(true);
                ServerScheduler.scheduleDelayed(duration, () -> ((Mob) targetEntity).setNoAi(false));
            }
            if(BeyonderData.isBeyonder(targetEntity)) {
                boolean hasMorale = InteractionHandler.isInteractionPossibleForEntity(eLoc, "morale_boost", targetSeq, targetEntity);
                int durationmorale = hasMorale? 20*2:20*4;
                double reduction = -4*multiplier(entity);
                BeyonderData.addModifierWithTimeLimit(targetEntity, "hair_entanglement_multiplier_reduction", reduction, durationmorale);
            }
        }

        Location loc = new Location(targetEntity.position(), targetEntity.level());

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 5, duration, () -> {
            // Burn Binding
            if(InteractionHandler.isInteractionPossible(loc, "burning")) {
                ServerScheduler.cancel(taskIdRef.get());

                targetEntity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                targetEntity.removeEffect(MobEffects.WEAKNESS);
                targetEntity.removeEffect(MobEffects.DIG_SLOWDOWN);
                if (targetEntity instanceof Mob mob) mob.setNoAi(false);

                Vec3 pos = targetEntity.getPosition(0.5f);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME,       pos, 180, 1.0, 0);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.LARGE_SMOKE, pos, 90, 1.0, 0.15);
                level.playSound(null, BlockPos.containing(pos),
                        SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.5f, 1.2f);

                pacifiedEntities.remove(targetEntity.getUUID());
                return;
            }

            if(InteractionHandler.isInteractionPossibleForEntity(loc, "escape", AbilityUtil.getSeqWithArt(entity, this), targetEntity)) {
                ServerScheduler.cancel(taskIdRef.get());

                targetEntity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                targetEntity.removeEffect(MobEffects.WEAKNESS);
                targetEntity.removeEffect(MobEffects.DIG_SLOWDOWN);
                if (targetEntity instanceof Mob mob) mob.setNoAi(false);

                pacifiedEntities.remove(targetEntity.getUUID());
                return;
            }

            targetEntity.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 40, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 10, false, false, false));
            targetEntity.setDeltaMovement(Vec3.ZERO);
            targetEntity.setOnGround(true);
            var pos = targetEntity.position();
            targetEntity.setDeltaMovement(new Vec3(0, 0, 0));
            targetEntity.hurtMarked = true;

            targetEntity.teleportTo(pos.x, pos.y, pos.z);
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
        taskIdRef.set(taskId);


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
        }, null, level, () -> AbilityUtil.getTimeInArea(null, startLoc));
    }
}
