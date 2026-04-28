package de.jakob.lotm.abilities.death.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PhysicalEnhancementsDeathAbility extends PhysicalEnhancementsAbility {

    public static final HashSet<LivingEntity> activeEntities = new HashSet<>();

    public PhysicalEnhancementsDeathAbility(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        super.tick(level, entity);
        activeEntities.removeIf(e -> !this.shouldApplyTo(e));
        activeEntities.add(entity);
    }

    @SubscribeEvent
    public static void onPoisonFreezeDamage(LivingIncomingDamageEvent event) {
        if (!activeEntities.contains(event.getEntity())) return;

        var damageTypeKey = event.getSource().typeHolder().unwrapKey();
        if (damageTypeKey.isEmpty()) return;

        ResourceKey<?> key = damageTypeKey.get();

        boolean isPoison = key.equals(DamageTypes.MAGIC) && event.getEntity().hasEffect(MobEffects.POISON);
        boolean isFreeze = key.equals(DamageTypes.FREEZE);

        if (isPoison || isFreeze) {
            event.setAmount(event.getAmount() * 0.5f);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "death", 9
        ));
    }

    @Override
    public List<PhysicalEnhancement> getEnhancements() {
        return List.of();
    }

    @Override
    protected List<PhysicalEnhancement> getEnhancementsForSequence(int sequenceLevel, LivingEntity entity) {

        return switch (sequenceLevel) {
            case 9 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1 )
            );

            case 8-> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1),
                    new PhysicalEnhancement(EnhancementType.SPEED, 1),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 5)
            );
            case 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.SPEED, 1),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 6),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 1 ),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 7),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 2 ),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 9),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 7),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 18),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3)
            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 9),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 19),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3)
            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 12 ),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 27),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4)
            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 13 ),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 34),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4)
            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 6 ),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 16 ),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6 ),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 47),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 6)
            );

            default -> List.of();
        };
    }

    @Override
    protected int getCurrentSequenceLevel(LivingEntity entity) {
        return BeyonderData.getSequence(entity);
    }
}
