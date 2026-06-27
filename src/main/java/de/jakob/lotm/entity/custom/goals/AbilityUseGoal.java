package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;


public class AbilityUseGoal extends Goal {
    private final Mob entity;
    private final Random random = new Random();

    private int abilityCooldown = 0;
    private static final int MIN_ABILITY_COOLDOWN = 8;
    private static final int MAX_ABILITY_COOLDOWN = 35;

    private static final int OUT_OF_COMBAT_CHECK_INTERVAL = 100;
    private int outOfCombatTimer = 0;

    private final boolean ignoreUsageConditions;
    private List<Ability> presetAbilities = null;

    public AbilityUseGoal(Mob entity) {
        this.entity = entity;
        this.ignoreUsageConditions = false;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public AbilityUseGoal(Mob entity, List<Ability> usableAbilities) {
        this.entity = entity;
        this.presetAbilities = usableAbilities;
        this.ignoreUsageConditions = true;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (entity.getTarget() != null) {
            return true;
        }

        outOfCombatTimer++;
        if (outOfCombatTimer >= OUT_OF_COMBAT_CHECK_INTERVAL) {
            outOfCombatTimer = 0;
            return random.nextInt(100) < 2;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return entity.getTarget() != null || canUse();
    }

    @Override
    public void start() {
        abilityCooldown = 0;
    }

    @Override
    public void tick() {
        if (abilityCooldown > 0) {
            abilityCooldown--;
            return;
        }

        List<Ability> usableAbilities = presetAbilities == null ? getUsableAbilities(entity) : presetAbilities;
        if (usableAbilities.isEmpty()) {
            return;
        }

        boolean inCombat = entity.getTarget() != null;

        if (inCombat) {
            if (random.nextInt(100) < 60) {
                tryUseAbility(usableAbilities);
                abilityCooldown = MIN_ABILITY_COOLDOWN + random.nextInt(MAX_ABILITY_COOLDOWN - MIN_ABILITY_COOLDOWN);
            }
        } else {
            tryUseAbility(usableAbilities);
            abilityCooldown = 100 + random.nextInt(100);
        }
    }

    private void tryUseAbility(List<Ability> usableAbilities) {
        if(!usableAbilities.isEmpty()) {
            useAbility(entity.level(), usableAbilities);
        }
    }

    private void useAbility(Level level, List<Ability> usableAbilities) {
        if (level.isClientSide) {
            return;
        }

        List<Ability> availableAbilities = ignoreUsageConditions ? usableAbilities : usableAbilities.stream()
                .filter(a -> a.canUse(entity))
                .toList();

        if (availableAbilities.isEmpty()) {
            return;
        }

        List<Ability> combatAbilities = availableAbilities.stream()
                .filter(a -> a.shouldUseAbility(entity))
                .sorted(Comparator.comparing(Ability::lowestSequenceUsable))
                .toList();

        List<Ability> toSelect;
        if (entity.getTarget() != null && !combatAbilities.isEmpty()) {
            toSelect = combatAbilities;
        } else if (entity.getTarget() == null) {
            List<Ability> nonCombatAbilities = availableAbilities.stream()
                    .filter(a -> !a.shouldUseAbility(entity))
                    .toList();

            if (nonCombatAbilities.isEmpty()) {
                return;
            }
            toSelect = nonCombatAbilities;
        } else {
            toSelect = availableAbilities;
        }

        Ability selectedAbility = selectWeightedAbility(toSelect, new Random());
        if(selectedAbility == null) return;

        if(!ignoreUsageConditions)
            selectedAbility.useAbility((ServerLevel) level, entity);
        else
            selectedAbility.useAbility((ServerLevel) level, entity, false, false, false, false);
    }

    private Ability selectWeightedAbility(List<Ability> abilities, Random random) {
        if (abilities.isEmpty()) {
            return null;
        }

        if (entity.getTarget() != null) {
            double distance = entity.distanceTo(entity.getTarget());

            List<Ability> distanceAppropriate = abilities.stream()
                    .filter(a -> !a.hasOptimalDistance ||
                            Math.abs(distance - a.optimalDistance) <= 5.0)
                    .toList();

            if (!distanceAppropriate.isEmpty()) {
                abilities = distanceAppropriate;
            }
        }

        int size = abilities.size();
        int totalWeight = (size * (size + 1)) / 2;
        int randomValue = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (int i = 0; i < size; i++) {
            int weight = size - i;
            cumulativeWeight += weight;

            if (randomValue < cumulativeWeight) {
                return abilities.get(i);
            }
        }

        return abilities.getFirst();
    }

    public static boolean hasRangedOption(Mob mob) {
        List<Ability> usableAbilities = getUsableAbilities(mob);
        if (usableAbilities.isEmpty()) {
            return false;
        }

        return usableAbilities.stream().anyMatch(ability -> ability.hasOptimalDistance);
    }


    private static ArrayList<Ability> getUsableAbilities(LivingEntity entity) {

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);

        ArrayList<Ability> usableAbilities = new ArrayList<>();

        LOTMCraft.abilityHandler.getByPathwayAndSequence(pathway, sequence).stream()
                .filter(a -> a.canBeUsedByNPC)
                .forEach(usableAbilities::add);

        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (component.isMarionette()) {
            Player controller = getController(entity);
            if (controller != null && BeyonderData.isBeyonder(controller) && BeyonderData.getSequence(controller) <= 4) {
                String controllerPathway = BeyonderData.getPathway(controller);
                int controllerSequence = BeyonderData.getSequence(controller);
                LOTMCraft.abilityHandler.getByPathwayAndSequence(controllerPathway, controllerSequence).stream()
                        .filter(a -> a.canBeUsedByNPC)
                        .forEach(usableAbilities::add);
            }
        }
        return usableAbilities;
    }

    private static Player getController(LivingEntity entity) {
        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) {
            return null;
        }

        try {
            UUID controllerUUID = UUID.fromString(component.getControllerUUID());
            Player controller = entity.level().getPlayerByUUID(controllerUUID);
            return (controller != null && controller.isAlive()) ? controller : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}