package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IndividualBalanceAbility extends Ability {

    public static final Map<UUID, Long> INDIVIDUALLY_BALANCED = new ConcurrentHashMap<>();

    public IndividualBalanceAbility(String id) {
        super(id, 40f);
        interactionRadius = 20;
        hasOptimalDistance = true;
        optimalDistance = 10f;
        postsUsedAbilityEventManually = true;
        canBeCopied = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 1.5f);
        if (target == null) return;

        if (!BeyonderData.isBeyonder(target)) {
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.individual_balance.not_beyonder")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        int DURATION = 3600 * (int) Math.max(multiplier(entity) / 4, 1);
        UUID targetId = target.getUUID();
        INDIVIDUALLY_BALANCED.put(targetId, serverLevel.getGameTime() + DURATION);
        ServerScheduler.scheduleDelayed(DURATION, () -> INDIVIDUALLY_BALANCED.remove(targetId));

        playJudgementEffect(serverLevel, entity, target, DURATION);

        Component broadcast = Component.translatable("ability.lotmcraft.individual_balance.applied_prefix")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(target.getDisplayName().getString())
                        .withStyle(ChatFormatting.WHITE));
        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(entity) <= 60) {
                p.sendSystemMessage(broadcast);
            }
        });

        if (target instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.individual_balance.sealed")
                    .withStyle(ChatFormatting.RED));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, target.position(), entity, this, interactionFlags, 3, 20 * 2));
    }

    private void playJudgementEffect(ServerLevel serverLevel, LivingEntity caster, LivingEntity target, int duration) {
        Vec3 targetPos = target.position().add(0, 1, 0);
        Vec3 casterPos = caster.position().add(0, 1, 0);
        Location targetLocation = new Location(target.position(), serverLevel);

        serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 0.6f);

        // Ascending dust rings that collapse into target — cast-on animation
        ServerScheduler.scheduleForDuration(0, 2, 40, () -> {
            Vec3 pos = targetLocation.getPosition().add(0, 1, 0);
            ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.END_ROD, pos, 3.5, 24);
            ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.DUST_PLUME, pos.add(0, 0.3, 0), 2.5, 18);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos, 3.2, 6);
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(caster, targetLocation));

        // Implosion ring at moment of sealing
        ServerScheduler.scheduleDelayed(38, () -> {
            Vec3 pos = targetLocation.getPosition().add(0, 1, 0);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.5f, 0.5f);
            RingEffectManager.createRingForAll(target.position(), 4f, 60,
                    1.0f, 0.92f, 0.6f, 1.0f, 2f, 6f, serverLevel);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, pos, 2.5, 40);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos, 3.0, 30);
        }, serverLevel);

        // Persistent golden halo and dust — active indicator on target
        ParticleUtil.createParticleSpirals(
                serverLevel, ParticleTypes.END_ROD, target.position().add(0, 0.1, 0),
                1.2, 1.2, 2.5, 0.6, 1.5, duration, 2, 80
        );

        Location persistentLocation = new Location(target.position(), serverLevel);
        ServerScheduler.scheduleForDuration(0, 8, duration, () -> {
            Vec3 pos = target.position().add(0, 1.0, 0);
            persistentLocation.setPosition(target.position());
            ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.DUST_PLUME, pos, 1.0, 10);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, pos.add(0, 0.5, 0), 2, 0.3, 0.0);
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(caster, persistentLocation));

        // Particle beam from caster to target at moment of cast
        ServerScheduler.scheduleDelayed(5, () ->
                        ParticleUtil.drawParticleLine(serverLevel, ParticleTypes.END_ROD, casterPos, targetPos, 0.4, 2, 0.05),
                serverLevel);

        // Expiry burst when balance lifts
        ServerScheduler.scheduleDelayed(duration - 1, () -> {
            Vec3 pos = targetLocation.getPosition().add(0, 1, 0);
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.8f);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, pos, 2.0, 50);
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DUST_PLUME, pos, 2.5, 40);
            RingEffectManager.createRingForAll(target.position(), 3f, 50,
                    1.0f, 0.92f, 0.6f, 0.6f, 1.5f, 5f, serverLevel);
        }, serverLevel);
    }
}