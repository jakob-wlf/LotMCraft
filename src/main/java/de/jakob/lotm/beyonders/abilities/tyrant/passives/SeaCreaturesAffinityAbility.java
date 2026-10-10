package de.jakob.lotm.beyonders.abilities.tyrant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility.EnhancementType;
import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility.PhysicalEnhancement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SeaCreaturesAffinityAbility extends PassiveAbility {
    private static final double SEARCH_RADIUS = 32.0;
    private static final String ENHANCEMENT_ID_PREFIX = "sea_creatures_affinity_";
    private static final List<PhysicalEnhancement> MOB_ENHANCEMENTS = List.of(
            new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
            new PhysicalEnhancement(EnhancementType.RESISTANCE, 4),
            new PhysicalEnhancement(EnhancementType.SPEED, 2),
            new PhysicalEnhancement(EnhancementType.HEALTH, 9),
            new PhysicalEnhancement(EnhancementType.DOLPHINS_GRACE, 2)
    );
    private static final int BUFF_DURATION_TICKS = 20 * 30; // 30 seconds
    private static final int ENFORCE_INTERVAL_TICKS = 8; // ~0.4 seconds

    private record RallyInfo(UUID targetUUID, long expiresAtGameTime) {}
    private static final Map<UUID, RallyInfo> rallied = new HashMap<>();
    private static final Map<UUID, Set<WrappedGoal>> originalGoals = new HashMap<>();
    private static final Map<UUID, Set<WrappedGoal>> originalTargetGoals = new HashMap<>();
    private static long tickCounter = 0;

    public SeaCreaturesAffinityAbility(String id) {super(id);}
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("tyrant", 3));}
    @Override
    public void tick(Level level, LivingEntity entity) {}
    @SubscribeEvent
    public static void onCombatDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {return;}
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel serverLevel)) {return;}
        PassiveAbility ability = PassiveAbilityHandler.getById("sea_creatures_affinity_ability");
        if (ability.shouldApplyTo(attacker)) {rallyNearbySeaCreatures(serverLevel, attacker, victim);}
        if (ability.shouldApplyTo(victim)) {rallyNearbySeaCreatures(serverLevel, victim, attacker);}
    }

    private static void rallyNearbySeaCreatures(ServerLevel level, LivingEntity beyonder, LivingEntity opponent) {
        long expiry = level.getGameTime() + BUFF_DURATION_TICKS;
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, beyonder.getBoundingBox().inflate(SEARCH_RADIUS))) {
            if (!nearby.getType().is(EntityTypeTags.AQUATIC)) continue;
            if (nearby == opponent) continue;
            if (nearby instanceof Mob mob) {
                lockDownBehavior(mob);
                mob.setTarget(opponent);
                rallied.put(nearby.getUUID(), new RallyInfo(opponent.getUUID(), expiry));
                applyEnhancements(mob);
            }
        }
    }
    private static String enhancementId(EnhancementType type) {return ENHANCEMENT_ID_PREFIX + type.name().toLowerCase();}
    private static void applyEnhancements(LivingEntity mob) {
        for (PhysicalEnhancement enhancement : MOB_ENHANCEMENTS) {PhysicalEnhancementsAbility.addTemporaryEnhancement(mob, enhancement, enhancementId(enhancement.getType()));}
        mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFF_DURATION_TICKS, 1, false, false));
    }
    private static void removeEnhancements(LivingEntity mob) {
        for (PhysicalEnhancement enhancement : MOB_ENHANCEMENTS) {PhysicalEnhancementsAbility.removeTemporaryEnhancement(mob, enhancementId(enhancement.getType()));}
    }

    private static void lockDownBehavior(Mob mob) {
        if (originalGoals.containsKey(mob.getUUID())) return; // already locked down
        Set<WrappedGoal> goals = new HashSet<>(mob.goalSelector.getAvailableGoals());
        Set<WrappedGoal> targetGoals = new HashSet<>(mob.targetSelector.getAvailableGoals());
        originalGoals.put(mob.getUUID(), goals);
        originalTargetGoals.put(mob.getUUID(), targetGoals);
        for (WrappedGoal goal : goals) {mob.goalSelector.removeGoal(goal.getGoal());}
        for (WrappedGoal goal : targetGoals) {mob.targetSelector.removeGoal(goal.getGoal());}
        mob.goalSelector.addGoal(0, new SimpleAttackGoal(mob, 1.2D, 5.0F));
    }

    private static void restoreBehavior(Mob mob) {
        removeEnhancements(mob);
        Set<WrappedGoal> goals = originalGoals.remove(mob.getUUID());
        Set<WrappedGoal> targetGoals = originalTargetGoals.remove(mob.getUUID());
        if (goals == null && targetGoals == null) return; // was never locked down
        List<WrappedGoal> currentGoals = new ArrayList<>(mob.goalSelector.getAvailableGoals());
        for (WrappedGoal g : currentGoals) {mob.goalSelector.removeGoal(g.getGoal());}
        List<WrappedGoal> currentTargetGoals = new ArrayList<>(mob.targetSelector.getAvailableGoals());
        for (WrappedGoal g : currentTargetGoals) {mob.targetSelector.removeGoal(g.getGoal());}
        if (goals != null) {for (WrappedGoal g : goals) {mob.goalSelector.addGoal(g.getPriority(), g.getGoal());}}
        if (targetGoals != null) {for (WrappedGoal g : targetGoals) {mob.targetSelector.addGoal(g.getPriority(), g.getGoal());}}
    }
    private static class SimpleAttackGoal extends Goal {//had to add this because of without it turtles keep crashing the game
        private final Mob mob;
        private final double speed;
        private final float damage;
        private int attackCooldown = 0;
        public SimpleAttackGoal(Mob mob, double speed, float damage) {this.mob = mob;
            this.speed = speed;
            this.damage = damage;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));}
        @Override
        public boolean canUse() {LivingEntity target = mob.getTarget();
            return target != null && target.isAlive();}
        @Override
        public boolean canContinueToUse() {return canUse();}
        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null) return;
            mob.getNavigation().moveTo(target, speed);
            mob.getLookControl().setLookAt(target);
            double attackRangeSq = (mob.getBbWidth() + target.getBbWidth()) * (mob.getBbWidth() + target.getBbWidth()) + 1.0;
            double distSq = mob.distanceToSqr(target);
            if (attackCooldown > 0) {attackCooldown--;}
            if (distSq <= attackRangeSq && attackCooldown <= 0) {
                mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                target.hurt(mob.damageSources().mobAttack(mob), damage);
                attackCooldown = 20; // 1 attack/sec
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter % ENFORCE_INTERVAL_TICKS != 0) return;
        if (rallied.isEmpty()) return;
        Iterator<Map.Entry<UUID, RallyInfo>> it = rallied.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, RallyInfo> entry = it.next();
            RallyInfo info = entry.getValue();
            boolean stillValid = false;
            Mob foundMob = null;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                Entity mobEntity = level.getEntity(entry.getKey());
                if (mobEntity instanceof Mob mob && mob.isAlive()) {
                    foundMob = mob;
                    if (level.getGameTime() < info.expiresAtGameTime()) {
                        Entity targetEntity = level.getEntity(info.targetUUID());
                        if (targetEntity instanceof LivingEntity target && target.isAlive()) {
                            mob.setTarget(target);
                            stillValid = true;
                        }
                    }
                    break;
                }
            }

            if (!stillValid) {
                if (foundMob != null) {restoreBehavior(foundMob);}
                it.remove();
            }
        }
    }
}