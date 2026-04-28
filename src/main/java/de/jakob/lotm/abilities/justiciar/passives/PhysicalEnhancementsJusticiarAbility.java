package de.jakob.lotm.abilities.justiciar.passives;

import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.util.BeyonderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalEnhancementsJusticiarAbility extends PhysicalEnhancementsAbility {

    public PhysicalEnhancementsJusticiarAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "justiciar", 9
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
                    new PhysicalEnhancement(EnhancementType.HEALTH, 2)
            );

            case 8 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1 ),
                    new PhysicalEnhancement(EnhancementType.SPEED, 1 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 7),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );
            case 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3 ),
                    new PhysicalEnhancement(EnhancementType.SPEED, 3 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 8),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 2),
                    new PhysicalEnhancement(EnhancementType.SPEED, 3 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 9),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 4),
                    new PhysicalEnhancement(EnhancementType.SPEED, 3 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 11),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 9),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 18),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3)
            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 10),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 19),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3)
            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 5 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 12),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 27),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4)
            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 6 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 14),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 34),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4)
            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 7),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 16),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 47),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 6)
            );

            default -> List.of();
        };
    }

    @Override
    protected int getCurrentSequenceLevel(net.minecraft.world.entity.LivingEntity entity) {
        return BeyonderData.getSequence(entity);
    }
}