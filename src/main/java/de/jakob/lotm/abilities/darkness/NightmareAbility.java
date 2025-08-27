package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.util.data.LocationSupplier;
import de.jakob.lotm.util.data.NightmareCenter;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class NightmareAbility extends SelectableAbilityItem {
    private static final HashMap<UUID, NightmareCenter> activeNightmares = new HashMap<>();

    public NightmareAbility(Properties properties) {
        super(properties, .15f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.nightmare.nightmare", "ability.lotmcraft.nightmare.reshape", "ability.lotmcraft.nightmare.restrict", "ability.lotmcraft.nightmare.attack"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> nightmare(level, entity);
            case 1 -> reshape(level, entity);
            case 2 -> restrict(level, entity);
        }
    }

    private void restrict(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(!activeNightmares.containsKey(entity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You need to create a Nightmare first.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(targetEntity == null) {
            Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 15, 1.5f, true);
            ParticleUtil.createParticleSpirals((ServerLevel) level, dustSmall, targetPos, 2, 2, 2.5, .5, 8, 20 * 5, 11, 8);
            return;
        }

        LocationSupplier locationSupplier = new LocationSupplier(targetEntity.position());
        ParticleUtil.createParticleSpirals((ServerLevel) level, dustVerySmall, locationSupplier, 1.2, 1.2, 2.5, .5, 8, 20 * 20, 11, 8);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 20, () -> {
            locationSupplier.setPos(targetEntity.position());
            Vec3 startPos = targetEntity.getEyePosition().subtract(0, .15, 0);
            ParticleUtil.drawParticleLine((ServerLevel) level, dustSmall, startPos, startPos.add(2.75, -3, 0), .25, 1);
            ParticleUtil.drawParticleLine((ServerLevel) level, dustSmall, startPos, startPos.add(-2.75, -3, 0), .25, 1);
            ParticleUtil.drawParticleLine((ServerLevel) level, dustSmall, startPos, startPos.add(0, -3, -2.75), .25, 1);
            ParticleUtil.drawParticleLine((ServerLevel) level, dustSmall, startPos, startPos.add(0, -3, 2.75), .25, 1);
            targetEntity.setDeltaMovement(0, 0, 0);
            targetEntity.hurtMarked = true;
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 10, false, false, false));
        }, (ServerLevel) level);
    }

    private void reshape(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(!activeNightmares.containsKey(entity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You need to create a Nightmare first.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

    }

    private final DustParticleOptions dustBig = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 10f);
    private final DustParticleOptions dustSmall = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 2f);
    private final DustParticleOptions dustVerySmall = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), .7f);

    private void nightmare(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(activeNightmares.containsKey(entity.getUUID())) {
            activeNightmares.remove(entity.getUUID());
            return;
        }

        double radius = 30;
        NightmareCenter center = new NightmareCenter((ServerLevel) level, entity.position(), radius * radius);

        for(NightmareCenter c : activeNightmares.values()) {
            double maxRadius = Math.max(center.radiusSquared(), c.radiusSquared());
            if(c.pos().distanceToSqr(center.pos()) <= maxRadius) {
                if(entity instanceof ServerPlayer player) {
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You are already near another Nightmare.").withColor(0xFFff124d));
                    player.connection.send(packet);
                }
                return;
            }
        }

        activeNightmares.put(entity.getUUID(), center);

        AtomicBoolean shouldStop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
            if(!activeNightmares.containsKey(entity.getUUID())) {
                shouldStop.set(true);
                activeNightmares.remove(entity.getUUID());
                return;
            }

            if(entity.level() != center.level()) {
                shouldStop.set(true);
                activeNightmares.remove(entity.getUUID());
                return;
            }

            if(entity.position().distanceToSqr(center.pos()) > (radius * radius)) {
                shouldStop.set(true);
                activeNightmares.remove(entity.getUUID());
                return;
            }

            ParticleUtil.spawnParticles((ServerLevel) level, dustBig, center.pos(), 30, radius, 0);
        }, 10, null, shouldStop);
    }

    public static boolean hasActiveNightmare(Player player) {
        return activeNightmares.containsKey(player.getUUID());
    }

    private boolean isAffectedByNightmare(LivingEntity entity) {
        for(Map.Entry<UUID, NightmareCenter> entry : activeNightmares.entrySet()) {
            if(entity.getUUID() == entry.getKey())
                continue;

            if(entity.position().distanceToSqr(entry.getValue().pos()) <= entry.getValue().radiusSquared())
                return true;
        }

        return false;
    }


}
