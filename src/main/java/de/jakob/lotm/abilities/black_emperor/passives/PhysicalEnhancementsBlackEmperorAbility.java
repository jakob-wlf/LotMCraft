package de.jakob.lotm.abilities.black_emperor.passives;

import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalEnhancementsBlackEmperorAbility extends PhysicalEnhancementsAbility {

    public PhysicalEnhancementsBlackEmperorAbility(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        super.tick(level, entity);

        if (level.isClientSide() || entity.tickCount % 20 != 0) return;

        int seq = BeyonderData.getSequence(entity);
        if (seq > 8 || seq < 0) return;

        SanityComponent sanity = entity.getData(ModAttachments.SANITY_COMPONENT);
        float current = sanity.getSanity();
        if (current >= 1.0f) return;

        // seq 8 = 10% resistance, seq 0 = 20% resistance
        float resistance = 0.10f + ((8 - seq) * 0.0125f);
        // The event handler drains at least 0.00025f/s when sanity is below 1.0;
        // restore the portion that would have been resisted.
        float compensation = 0.00025f * resistance;
        sanity.increaseSanityAndSync(compensation, entity);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "black_emperor", 9
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