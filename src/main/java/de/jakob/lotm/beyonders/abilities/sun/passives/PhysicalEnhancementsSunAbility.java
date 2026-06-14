package de.jakob.lotm.beyonders.abilities.sun.passives;

import de.jakob.lotm.beyonders.abilities.core.PhysicalEnhancementsAbility;
import de.jakob.lotm.util.BeyonderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PhysicalEnhancementsSunAbility extends PhysicalEnhancementsAbility {

    public PhysicalEnhancementsSunAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "sun", 9
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
                    new PhysicalEnhancement(EnhancementType.SPEED, 1),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 4)
            );

            case 8-> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1),
                    new PhysicalEnhancement(EnhancementType.SPEED, 1),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 5)
            );
            case 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 1),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 6),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 2),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 7),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 1)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 3),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 9),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 1),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1)
            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 8),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 18),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 1),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1)
            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 9),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 19),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 1),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1)
            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 5),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 12),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 27),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 1),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1)
            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 5),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 13),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 32),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 1),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1)
            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 6),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 16),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 47),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 6),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 1),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1)
            );

            default -> List.of();
        };
    }

    @Override
    protected int getCurrentSequenceLevel(net.minecraft.world.entity.LivingEntity entity) {
        return BeyonderData.getSequence(entity);
    }
}