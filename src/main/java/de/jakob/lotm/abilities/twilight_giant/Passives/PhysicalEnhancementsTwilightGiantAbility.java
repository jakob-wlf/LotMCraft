package de.jakob.lotm.beyonders.abilities.twilight_giant.Passives;


import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PhysicalEnhancementsTwilightGiantAbility extends PhysicalEnhancementsAbility {

    public PhysicalEnhancementsTwilightGiantAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "twilight_giant", 9
        ));
    }

    @Override
    public List<PhysicalEnhancement> getEnhancements() {
        return List.of();
    }

    @Override
    protected List<PhysicalEnhancement> getEnhancementsForSequence(int sequenceLevel) {
        return switch (sequenceLevel) {
            case 9 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 1),
                    new PhysicalEnhancement(EnhancementType.SPEED, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 2)
            );

            case 8 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 4),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );
            case 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 3),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 6),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 6),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 3),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 7),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 7),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 4),
                    new PhysicalEnhancement(EnhancementType.SPEED, 3),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 9),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)

            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 9),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 9),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 16),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3)

            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 10),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 10),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 17),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3)

            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 12),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 13),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 25),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4)

            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 15),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 15),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 30),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4)

            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 20),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 17),
                    new PhysicalEnhancement(EnhancementType.SPEED, 7),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 45),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 6)

            );

            default -> List.of();
        };
    }

}

