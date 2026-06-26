package de.jakob.lotm.entity.custom.goals.avatar;

import de.jakob.lotm.entity.custom.AvatarEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;
import java.util.UUID;

public class AvatarTargetGoal extends TargetGoal {

    private final AvatarEntity avatar;

    public AvatarTargetGoal(AvatarEntity avatar) {
        super(avatar, false);
        this.avatar = avatar;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return isOwnerValid() && avatar.getTarget() == null && findValidTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        LivingEntity target = findValidTarget();
        if (target != null) avatar.setTarget(target);
    }

    @Override
    public void tick() {
        LivingEntity target = avatar.getTarget();
        if (target == null) return;

        UUID owner = avatar.getOriginalOwner();
        if (!target.isAlive() || target.isRemoved()
                || (owner != null && target.getUUID().equals(owner))) {
            avatar.setTarget(null);
            avatar.setLastHurtByMob(null);
        }
    }

    private LivingEntity findValidTarget() {
        UUID ownerUUID = avatar.getOriginalOwner();
        if (ownerUUID == null) return null;

        var owner = avatar.level().getPlayerByUUID(ownerUUID);
        if (owner == null || !owner.isAlive()) return null;

        LivingEntity attacker = owner.getLastHurtByMob();
        if (isValidTarget(attacker, owner)) return attacker;

        LivingEntity ownerTarget = owner.getLastHurtMob();
        if (isValidTarget(ownerTarget, owner)) return ownerTarget;

        return null;
    }

    private boolean isValidTarget(LivingEntity candidate, LivingEntity owner) {
        return candidate != null && candidate.isAlive()
                && candidate != avatar && candidate != owner;
    }

    private boolean isOwnerValid() {
        UUID ownerUUID = avatar.getOriginalOwner();
        if (ownerUUID == null) return false;
        var owner = avatar.level().getPlayerByUUID(ownerUUID);
        return owner != null && owner.isAlive();
    }
}