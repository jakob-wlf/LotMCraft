package de.jakob.lotm.beyonders.boss;

import java.util.EnumSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class BossAiGoals {
    private BossAiGoals() {
    }

    static Installed install(Mob mob) {
        IdleGoal idleGoal = new IdleGoal(mob);
        PlayerTargetGoal playerTargetGoal = new PlayerTargetGoal(mob);
        mob.goalSelector.addGoal(-1, idleGoal);
        mob.targetSelector.addGoal(-1, playerTargetGoal);
        MeleeAttackGoal meleeFallback = null;
        BossDefinition definition = BossRegistry.getDefinition(mob).orElse(null);
        if (definition != null && definition.meleeFallback() && mob instanceof PathfinderMob pathfinder && !hasCombatGoal(mob)) {
            meleeFallback = new MeleeAttackGoal(pathfinder, 1.0, true);
            mob.goalSelector.addGoal(2, meleeFallback);
        }
        BossRuntime.ensureAnchor(mob);
        return new Installed(idleGoal, playerTargetGoal, meleeFallback);
    }

    static void uninstall(Mob mob, Installed installed) {
        mob.goalSelector.removeGoal(installed.idleGoal());
        mob.targetSelector.removeGoal(installed.playerTargetGoal());
        if (installed.meleeFallback() != null) {
            mob.goalSelector.removeGoal(installed.meleeFallback());
        }
    }

    record Installed(Goal idleGoal, Goal playerTargetGoal, MeleeAttackGoal meleeFallback) {
    }

    private static boolean hasCombatGoal(Mob mob) {
        return mob.goalSelector.getAvailableGoals().stream()
            .map(WrappedGoal::getGoal)
            .map(goal -> goal.getClass().getSimpleName())
            .anyMatch(name -> name.contains("Attack") || name.contains("Swell") || name.contains("ShootFireball"));
    }

    private static final class PlayerTargetGoal extends Goal {
        private final Mob mob;
        private Player candidate;

        private PlayerTargetGoal(Mob mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Optional<BossDefinition> optional = BossRegistry.getDefinition(mob);
            if (optional.isEmpty() || optional.get().hostility() != BossDefinition.Hostility.ALWAYS) {
                return false;
            }
            if (mob.getTarget() instanceof Player player && player.isAlive() && !player.isSpectator()) {
                return false;
            }
            candidate = findNearestPlayer(optional.get().targetRange());
            return candidate != null;
        }

        @Override
        public boolean canContinueToUse() {
            Optional<BossDefinition> optional = BossRegistry.getDefinition(mob);
            if (optional.isEmpty() || optional.get().hostility() != BossDefinition.Hostility.ALWAYS) {
                return false;
            }
            return candidate != null && candidate.isAlive() && !candidate.isSpectator()
                && mob.getTarget() == candidate
                && candidate.distanceToSqr(mob) <= optional.get().targetRange() * optional.get().targetRange();
        }

        @Override
        public void start() {
            if (candidate != null) {
                mob.setTarget(candidate);
            }
        }

        @Override
        public void stop() {
            if (candidate != null && mob.getTarget() == candidate) {
                mob.setTarget(null);
            }
            candidate = null;
        }

        private Player findNearestPlayer(double range) {
            Level level = mob.level();
            List<Player> players = level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(range), player ->
                player.isAlive() && !player.isSpectator() && player.distanceToSqr(mob) <= range * range
            );
            return players.stream().min(Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);
        }
    }

    private static final class IdleGoal extends Goal {
        private final Mob mob;
        private int destinationCooldown;

        private IdleGoal(Mob mob) {
            this.mob = mob;
            this.destinationCooldown = 0;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return BossRegistry.getDefinition(mob).isPresent() && mob.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            destinationCooldown = 0;
        }

        @Override
        public void tick() {
            Optional<BossDefinition> optional = BossRegistry.getDefinition(mob);
            if (optional.isEmpty()) {
                mob.getNavigation().stop();
                return;
            }
            BossDefinition definition = optional.get();
            if (definition.idleBehavior() == BossDefinition.IdleBehavior.STAY || definition.wanderRadius() <= 0.0) {
                mob.getNavigation().stop();
                Vec3 velocity = mob.getDeltaMovement();
                mob.setDeltaMovement(0.0, velocity.y, 0.0);
                return;
            }
            BossRuntime.ensureAnchor(mob);
            double anchorX = mob.getPersistentData().getDouble(BossRuntime.ANCHOR_X);
            double anchorY = mob.getPersistentData().getDouble(BossRuntime.ANCHOR_Y);
            double anchorZ = mob.getPersistentData().getDouble(BossRuntime.ANCHOR_Z);
            double radius = definition.wanderRadius();
            double dx = mob.getX() - anchorX;
            double dz = mob.getZ() - anchorZ;
            if (dx * dx + dz * dz > radius * radius) {
                if (mob.getNavigation().isDone() || ++destinationCooldown >= 20) {
                    mob.getNavigation().moveTo(anchorX, anchorY, anchorZ, 0.9);
                    destinationCooldown = 0;
                }
                return;
            }
            if (mob.getNavigation().isDone() || ++destinationCooldown >= 80) {
                double angle = mob.getRandom().nextDouble() * Math.PI * 2.0;
                double distance = Math.sqrt(mob.getRandom().nextDouble()) * radius;
                double x = anchorX + Math.cos(angle) * distance;
                double z = anchorZ + Math.sin(angle) * distance;
                BlockPos destination = BlockPos.containing(x, anchorY, z);
                mob.getNavigation().moveTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5, 0.8);
                destinationCooldown = 0;
            }
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            destinationCooldown = 0;
        }
    }
}
