package de.jakob.lotm.entity.custom.goals.avatar;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.entity.custom.ErrorAvatarEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Random;


public class AvatarAbilityUseGoal extends Goal {
    private final ErrorAvatarEntity entity;
    public AvatarAbilityUseGoal(ErrorAvatarEntity entity) {
        this.entity = entity;
    }

    private final Random random = new Random();

    @Override
    public boolean canUse() {
        return entity.isInCombat() || random.nextInt(100) < 2;
    }

    @Override
    public void tick() {
        if(random.nextInt(100) <= 20)
            entity.tryUseAbility();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}