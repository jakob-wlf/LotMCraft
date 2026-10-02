package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FloatingWindAbility extends ToggleAbility {
    private static final float HOVER_FLYING_SPEED = 0.01f; // used only for players entity's
    private static final double MAX_HORIZONTAL_SPEED = 0.15; // used only for non-player entity's
    private static final Map<UUID, Double> lockedY = new HashMap<>();
    private static final Map<UUID, Boolean> savedFlying = new HashMap<>();
    private static final Map<UUID, Boolean> savedMayfly = new HashMap<>();
    private static final Map<UUID, Float> savedFlyingSpeed = new HashMap<>();

    public FloatingWindAbility(String id) {super(id);tickRate = 1;}
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("tyrant", 6));}
    @Override
    protected float getSpiritualityCost() {return 1f;}
    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (entity instanceof Player player) {
            UUID id = player.getUUID();
            savedFlying.put(id, player.getAbilities().flying);
            savedMayfly.put(id, player.getAbilities().mayfly);
            savedFlyingSpeed.put(id, player.getAbilities().getFlyingSpeed());
        }
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        UUID id = entity.getUUID();
        if (entity.onGround()) {
            entity.setNoGravity(false);
            lockedY.remove(id);
            if (entity instanceof Player player) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            return;
        }
        double target = lockedY.computeIfAbsent(id, k -> entity.getY());
        if (entity instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(HOVER_FLYING_SPEED);
            player.onUpdateAbilities();
            if (Math.abs(player.getY() - target) > 0.001) {
                if (player instanceof ServerPlayer serverPlayer) {serverPlayer.connection.teleport(player.getX(), target, player.getZ(), player.getYRot(), player.getXRot());}
                else {player.setPos(player.getX(), target, player.getZ());}
            }
        } else {
            entity.setNoGravity(true);
            Vec3 motion = entity.getDeltaMovement();
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            double newX = motion.x;
            double newZ = motion.z;
            if (horizontalSpeed > MAX_HORIZONTAL_SPEED) {
                double scale = MAX_HORIZONTAL_SPEED / horizontalSpeed;
                newX = motion.x * scale;
                newZ = motion.z * scale;
            }
            entity.setDeltaMovement(newX, 0, newZ);
            entity.fallDistance = 0;
            if (Math.abs(entity.getY() - target) > 0.001) {entity.setPos(entity.getX(), target, entity.getZ());}
        }
    }
    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        UUID id = entity.getUUID();
        lockedY.remove(id);
        entity.setNoGravity(false);
        if (entity instanceof Player player) {
            Boolean prevFlying = savedFlying.remove(id);
            Boolean prevMayfly = savedMayfly.remove(id);
            Float prevSpeed = savedFlyingSpeed.remove(id);
            if (prevFlying != null) player.getAbilities().flying = prevFlying;
            if (prevMayfly != null) player.getAbilities().mayfly = prevMayfly;
            if (prevSpeed != null) player.getAbilities().setFlyingSpeed(prevSpeed);
            player.onUpdateAbilities();
        }
    }
}