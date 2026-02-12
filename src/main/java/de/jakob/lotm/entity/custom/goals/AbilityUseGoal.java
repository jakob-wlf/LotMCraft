package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;


public class AbilityUseGoal extends Goal {
    private final BeyonderNPCEntity entity;
    private final Random random = new Random();

    // Cooldown management
    private int abilityCooldown = 0;
    private static final int MIN_ABILITY_COOLDOWN = 8;  // 1 second minimum between abilities
    private static final int MAX_ABILITY_COOLDOWN = 35;  // 3 seconds maximum between abilities

    // Out-of-combat usage
    private static final int OUT_OF_COMBAT_CHECK_INTERVAL = 100;  // Check every 5 seconds
    private int outOfCombatTimer = 0;

    public AbilityUseGoal(BeyonderNPCEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.TARGET)); // Only affects targeting, not movement
    }

    @Override
    public boolean canUse() {
        // Can use if in combat OR occasionally out of combat
        if (entity.isInCombat()) {
            return true;
        }

        // Out of combat - check less frequently
        outOfCombatTimer++;
        if (outOfCombatTimer >= OUT_OF_COMBAT_CHECK_INTERVAL) {
            outOfCombatTimer = 0;
            // 2% chance to use out-of-combat ability
            return random.nextInt(100) < 2;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return entity.isInCombat() || canUse();
    }

    @Override
    public void start() {
        // Reset cooldown when starting
        abilityCooldown = 0;
    }

    @Override
    public void tick() {
        // Decrement cooldown
        if (abilityCooldown > 0) {
            abilityCooldown--;
            return;
        }

        // Get usable abilities
        List<Ability> usableAbilities = entity.getUsableAbilities();
        if (usableAbilities == null || usableAbilities.isEmpty()) {
            return;
        }

        boolean inCombat = entity.isInCombat();

        // Different behavior for combat vs non-combat
        if (inCombat) {
            // In combat: use abilities more frequently
            // 60% chance per tick when off cooldown
            if (random.nextInt(100) < 60) {
                entity.tryUseAbility();
                // Set cooldown between min and max
                abilityCooldown = MIN_ABILITY_COOLDOWN + random.nextInt(MAX_ABILITY_COOLDOWN - MIN_ABILITY_COOLDOWN);
            }
        } else {
            // Out of combat: only use non-combat abilities
            entity.tryUseAbility();
            // Longer cooldown for out-of-combat abilities
            abilityCooldown = 100 + random.nextInt(100); // 5-10 seconds
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}