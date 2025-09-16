package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Random;

public class AbilityUseGoal extends Goal {
    private final BeyonderNPCEntity entity;
    public AbilityUseGoal(BeyonderNPCEntity entity) {
        this.entity = entity;
    }

    private final Random random = new Random();

    @Override
    public boolean canUse() {
        return entity.isInCombat();
    }

    @Override
    public void tick() {
        if(random.nextInt(100) >= 12) {
            return;
        }
        System.out.println("Attempting to use ability");
        
        entity.tryUseAbility();
    }
}