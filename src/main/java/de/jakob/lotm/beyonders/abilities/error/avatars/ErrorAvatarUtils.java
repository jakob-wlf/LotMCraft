package de.jakob.lotm.beyonders.abilities.error.avatars;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ParasitationComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.EnumSet;
import java.util.UUID;

/** AI and owner protection for entities carrying a Time Worm of the Error pathway. */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class ErrorAvatarUtils {
    private ErrorAvatarUtils() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || event.getLevel().isClientSide) return;
        ParasitationComponent parasite = mob.getData(ModAttachments.PARASITE_COMPONENT);
        if (!parasite.hasTimeWorm() || parasite.getTimeWormOwnerUUID() == null) return;

        // Reinstall the Error-specific goals when the controlled host is returned to the world.
        mob.goalSelector.removeAllGoals(goal -> goal instanceof TimeWormFollowGoal);
        mob.targetSelector.removeAllGoals(goal -> goal instanceof TimeWormDefendGoal);
        mob.goalSelector.addGoal(0, new TimeWormFollowGoal(mob));
        mob.targetSelector.addGoal(0, new TimeWormDefendGoal(mob));
        mob.setTarget(null);
    }

    @SubscribeEvent
    public static void preventAttackingWormOwner(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target != null && isWormOwner(mob, target)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void preventDamagingWormOwner(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        LivingEntity attacker = getLivingAttacker(event.getSource().getEntity());
        if (attacker == null) attacker = getLivingAttacker(event.getSource().getDirectEntity());
        if (attacker != null && areBoundByTimeWorm(attacker, victim)) event.setCanceled(true);
    }

    private static boolean areBoundByTimeWorm(LivingEntity first, LivingEntity second) {
        return isWormOwner(first, second) || isWormOwner(second, first)
                || (hasWormOwner(first, second) && hasWormOwner(second, first));
    }

    private static boolean isWormOwner(LivingEntity host, LivingEntity possibleOwner) {
        ParasitationComponent parasite = host.getData(ModAttachments.PARASITE_COMPONENT);
        return parasite.hasTimeWorm() && possibleOwner.getUUID().equals(parasite.getTimeWormOwnerUUID());
    }

    private static boolean hasWormOwner(LivingEntity entity, LivingEntity owner) {
        ParasitationComponent parasite = entity.getData(ModAttachments.PARASITE_COMPONENT);
        return parasite.hasTimeWorm() && owner.getUUID().equals(parasite.getTimeWormOwnerUUID());
    }

    private static LivingEntity getLivingAttacker(Entity source) {
        if (source instanceof LivingEntity living) return living;
        if (source instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity living) return living;
        return null;
    }

    private static Player getOwner(Mob mob) {
        ParasitationComponent parasite = mob.getData(ModAttachments.PARASITE_COMPONENT);
        UUID ownerUUID = parasite.getTimeWormOwnerUUID();
        if (!parasite.hasTimeWorm() || ownerUUID == null) return null;
        return mob.level().getPlayerByUUID(ownerUUID);
    }

    private static final class TimeWormFollowGoal extends Goal {
        private final Mob host;
        private Player owner;

        private TimeWormFollowGoal(Mob host) {
            this.host = host;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            owner = getOwner(host);
            return owner != null && owner.isAlive() && host.distanceToSqr(owner) > 36.0;
        }

        @Override
        public boolean canContinueToUse() {
            return getOwner(host) == owner && owner != null && owner.isAlive()
                    && host.distanceToSqr(owner) > 9.0;
        }

        @Override
        public void tick() {
            if (owner != null) host.getNavigation().moveTo(owner, 1.0);
        }

        @Override
        public void stop() {
            host.getNavigation().stop();
        }
    }

    private static final class TimeWormDefendGoal extends TargetGoal {
        private final Mob host;
        private Player owner;
        private LivingEntity threat;

        private TimeWormDefendGoal(Mob host) {
            super(host, false);
            this.host = host;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            owner = getOwner(host);
            if (owner == null || !owner.isAlive()) return false;
            LivingEntity attacker = owner.getLastHurtByMob();
            LivingEntity target = owner.getLastHurtMob();
            threat = attacker != null && attacker.isAlive() && attacker != host ? attacker
                    : target != null && target.isAlive() && target != host ? target : null;
            return threat != null;
        }

        @Override
        public boolean canContinueToUse() {
            return threat != null && threat.isAlive() && getOwner(host) == owner;
        }

        @Override
        public void start() {
            if (threat != null && !isWormOwner(host, threat)) host.setTarget(threat);
        }
    }
}

