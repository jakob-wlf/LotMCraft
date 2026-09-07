package de.jakob.lotm.util.helper;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

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

    private static void alertAllies(ServerLevel level, LivingEntity defender, LivingEntity target, AllyComponent allyComponent) {
        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(
                null,
                level,
                defender.position(),
                ALLY_RESPONSE_RANGE
        );

        for (LivingEntity nearbyEntity : nearbyEntities) {
            if (!allyComponent.isAlly(nearbyEntity.getUUID())) continue;

            if (nearbyEntity instanceof Mob mob && mob.getTarget() == target) continue;

            if (nearbyEntity == target) continue;

            if (nearbyEntity == defender) continue;

            if (!AbilityUtil.mayTarget(nearbyEntity, target)) continue;

            if (nearbyEntity instanceof Mob mob) {
                mob.setTarget(target);

                if (!hasAllyDefenseGoal(mob)) {
                    mob.targetSelector.addGoal(1, new AllyDefenseGoal(mob, defender, target));
                }
            }
        }
    }

    private static boolean hasAllyDefenseGoal(Mob mob) {
        return mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof AllyDefenseGoal);
    }

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

            if (originalTarget.distanceTo(mob) > ALLY_RESPONSE_RANGE + 10) {
                return false;
            }

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