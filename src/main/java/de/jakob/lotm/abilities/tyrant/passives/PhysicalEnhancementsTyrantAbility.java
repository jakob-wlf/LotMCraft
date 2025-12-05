package de.jakob.lotm.abilities.tyrant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.util.BeyonderData;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;


public class PhysicalEnhancementsTyrantAbility extends PhysicalEnhancementsAbility {

    public PhysicalEnhancementsTyrantAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "tyrant", 9
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
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );

            case 8 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 4),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 5),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 4),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 5),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.UNDERWATER_BREATHING, 1)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 6),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 7),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.UNDERWATER_BREATHING, 1)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 8),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 9),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.UNDERWATER_BREATHING, 1),
                    new PhysicalEnhancement(EnhancementType.DOLPHINS_GRACE, 1)
            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 13),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 16),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.UNDERWATER_BREATHING, 1),
                    new PhysicalEnhancement(EnhancementType.DOLPHINS_GRACE, 5),
                    new PhysicalEnhancement(EnhancementType.CONDUIT, 4)

            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 14),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 17),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.UNDERWATER_BREATHING, 1),
                    new PhysicalEnhancement(EnhancementType.DOLPHINS_GRACE, 7),
                    new PhysicalEnhancement(EnhancementType.CONDUIT, 5)
            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 17),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 25),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.UNDERWATER_BREATHING, 1),
                    new PhysicalEnhancement(EnhancementType.DOLPHINS_GRACE, 8),
                    new PhysicalEnhancement(EnhancementType.CONDUIT, 6)
            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 18),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 30),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 4),
                    new PhysicalEnhancement(EnhancementType.UNDERWATER_BREATHING, 1),
                    new PhysicalEnhancement(EnhancementType.DOLPHINS_GRACE, 9),
                    new PhysicalEnhancement(EnhancementType.CONDUIT, 7)
            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 5),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 18),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 20),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 5)
            );

            default -> List.of();
        };
    }

    @Override
    protected int getCurrentSequenceLevel(net.minecraft.world.entity.LivingEntity entity) {
        return BeyonderData.getSequence(entity);
    }
}