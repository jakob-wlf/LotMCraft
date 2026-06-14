package de.jakob.lotm.beyonders.abilities.error.passives;

import de.jakob.lotm.beyonders.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.util.BeyonderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PhysicalEnhancementsErrorAbility extends PhysicalEnhancementsAbility {

    public PhysicalEnhancementsErrorAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "error", 9
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
                    new PhysicalEnhancement(EnhancementType.SPEED, 1),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 1)
            );

            case 8 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 3),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 1)
            );

            case 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1),
                    new PhysicalEnhancement(EnhancementType.SPEED, 3),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 3),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 2)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 1),
                    new PhysicalEnhancement(EnhancementType.SPEED, 3),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 4),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 2)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 2),
                    new PhysicalEnhancement(EnhancementType.SPEED, 3),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 5),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 2)
            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 4),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 12),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 4)
            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 5),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 13),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 4)
            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 8),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 21),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 5)
            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 8),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 26),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 5)
            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 10),
                    new PhysicalEnhancement(EnhancementType.SPEED, 7),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 36),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.MINING_EFFICIENCY, 6)
            );

            default -> List.of();
        };
    }

    @Override
    protected int getCurrentSequenceLevel(net.minecraft.world.entity.LivingEntity entity) {
        return BeyonderData.getSequence(entity);
    }
}