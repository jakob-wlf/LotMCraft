package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;
import java.util.UUID;

/**
 * Handles ally defense - when an ally is attacked or attacks someone,
 * nearby allies will come to their aid
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AllyDefenseHandler {

    private static final double ALLY_RESPONSE_RANGE = 50.0;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        LivingEntity victim = event.getEntity();
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;

        if (attacker == null || victim == null) return;
        if (attacker == victim) return;

        // Get allies of the victim
        AllyComponent victimAllies = victim.getData(ModAttachments.ALLY_COMPONENT.get());
        if (victimAllies.hasAllies()) {
            alertAllies(serverLevel, victim, attacker, victimAllies);
        }

        // Get allies of the attacker
        AllyComponent attackerAllies = attacker.getData(ModAttachments.ALLY_COMPONENT.get());
        if (attackerAllies.hasAllies()) {
            alertAllies(serverLevel, attacker, victim, attackerAllies);
        }
    }

    /**
     * Alert nearby allies to come to defense
     * @param level The server level
     * @param defender The entity whose allies should be alerted
     * @param target The enemy to target
     * @param allyComponent The ally component containing ally UUIDs
     */
    private static void alertAllies(ServerLevel level, LivingEntity defender, LivingEntity target, AllyComponent allyComponent) {
        // Get all nearby entities
        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(
                null,
                level,
                defender.position(),
                ALLY_RESPONSE_RANGE
        );

        for (LivingEntity nearbyEntity : nearbyEntities) {
            // Check if this entity is an ally
            if (!allyComponent.isAlly(nearbyEntity.getUUID())) continue;

            // Skip if already targeting this entity
            if (nearbyEntity instanceof Mob mob && mob.getTarget() == target) continue;

            // Don't make allies of the target attack the target (they're on same side)
            if (nearbyEntity == target) continue;

            // Don't make the defender target themselves
            if (nearbyEntity == defender) continue;

            // Check if the ally can actually target this entity
            if (!AbilityUtil.mayTarget(nearbyEntity, target)) continue;

            // Make the ally target the enemy
            if (nearbyEntity instanceof Mob mob) {
                mob.setTarget(target);

                // Add a custom goal to maintain aggression
                if (!hasAllyDefenseGoal(mob)) {
                    mob.targetSelector.addGoal(1, new AllyDefenseGoal(mob, defender, target));
                }
            }
        }
    }

    /**
     * Check if a mob already has an ally defense goal
     */
    private static boolean hasAllyDefenseGoal(Mob mob) {
        return mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof AllyDefenseGoal);
    }

    /**
     * Custom AI goal for defending allies
     */
    private static class AllyDefenseGoal extends TargetGoal {
        private final LivingEntity defender;
        private final LivingEntity originalTarget;
        private int ticksWithoutTarget;

        public AllyDefenseGoal(Mob mob, LivingEntity defender, LivingEntity target) {
            super(mob, false);
            this.defender = defender;
            this.originalTarget = target;
            this.ticksWithoutTarget = 0;
        }

        @Override
        public boolean canUse() {
            // Continue targeting if the target is still valid
            if (originalTarget.isAlive() && originalTarget.distanceTo(mob) < ALLY_RESPONSE_RANGE) {
                return AbilityUtil.mayTarget(mob, originalTarget);
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (!originalTarget.isAlive()) {
                return false;
            }

            // Stop if target is too far away
            if (originalTarget.distanceTo(mob) > ALLY_RESPONSE_RANGE + 10) {
                return false;
            }

            // Stop if we haven't had a valid target for 5 seconds
            if (mob.getTarget() != originalTarget) {
                ticksWithoutTarget++;
                if (ticksWithoutTarget > 100) { // 5 seconds
                    return false;
                }
            } else {
                ticksWithoutTarget = 0;
            }

            return AbilityUtil.mayTarget(mob, originalTarget);
        }

        @Override
        public void start() {
            mob.setTarget(originalTarget);
            ticksWithoutTarget = 0;
            super.start();
        }

        @Override
        public void stop() {
            ticksWithoutTarget = 0;
            super.stop();
        }

        @Override
        public void tick() {
            if (mob.getTarget() != originalTarget && canUse()) {
                mob.setTarget(originalTarget);
            }
        }
    }
}