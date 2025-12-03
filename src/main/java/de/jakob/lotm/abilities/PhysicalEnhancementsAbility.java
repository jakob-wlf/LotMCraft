package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PhysicalEnhancementsAbility extends PassiveAbilityItem {

    private static final String BASE_MODIFIER_ID = "lotm_physical_enhancement";
    private static final Map<UUID, Map<EnhancementType, Integer>> entityEnhancements = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, TemporaryEnhancement>> temporaryEnhancements = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, EnhancementBoost>> enhancementBoosts = new ConcurrentHashMap<>();

    // Track last calculated enhancements to detect changes
    private static final Map<UUID, Map<EnhancementType, Double>> lastCalculatedValues = new ConcurrentHashMap<>();

    public PhysicalEnhancementsAbility(Properties properties) {
        super(properties);
    }

    /**
     * Override this to provide base enhancements from the sequence/ability
     */
    public abstract List<PhysicalEnhancement> getEnhancements();

    /**
     * Override this to dynamically calculate enhancements based on sequence level
     * This is called every tick to allow for dynamic recalculation
     */
    protected List<PhysicalEnhancement> getEnhancementsForSequence(int sequenceLevel) {
        return getEnhancements();
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;

        // Recalculate and apply enhancements based on current sequence
        recalculateEnhancements(entity);

        applyNightVision(entity);

        // Update temporary enhancements
        updateTemporaryEnhancements(entity);
        updateEnhancementBoosts(entity);
    }

    /**
     * Recalculates all enhancements and updates them if they've changed
     * This allows for dynamic scaling based on sequence level
     */
    private void recalculateEnhancements(LivingEntity entity) {
        int sequenceLevel = getCurrentSequenceLevel(entity);
        List<PhysicalEnhancement> currentEnhancements = getEnhancementsForSequence(sequenceLevel);

        Map<EnhancementType, Double> newValues = new HashMap<>();
        Map<EnhancementType, Integer> enhancementMap = new HashMap<>();

        // Calculate new values
        for (PhysicalEnhancement enhancement : currentEnhancements) {
            double value = enhancement.calculateValue();
            newValues.put(enhancement.getType(), value);
            enhancementMap.put(enhancement.getType(), enhancement.getLevel());
        }

        // Check if values have changed
        Map<EnhancementType, Double> lastValues = lastCalculatedValues.get(entity.getUUID());
        boolean hasChanged = lastValues == null || !newValues.equals(lastValues);

        if (hasChanged) {
            // Remove old enhancements
            if (lastValues != null) {
                for (EnhancementType type : lastValues.keySet()) {
                    removeEnhancement(entity, type);
                }
            }

            // Apply new enhancements
            for (PhysicalEnhancement enhancement : currentEnhancements) {
                applyEnhancement(entity, enhancement);
            }

            // Update tracking maps
            lastCalculatedValues.put(entity.getUUID(), newValues);
            entityEnhancements.put(entity.getUUID(), enhancementMap);
        }
    }

    /**
     * Override this to provide the current sequence level
     * Default implementation returns 0
     */
    protected int getCurrentSequenceLevel(LivingEntity entity) {
        // Default implementation - override in subclasses
        return 0;
    }

    private void applyNightVision(LivingEntity entity) {
        // Check if entity has night vision enhancement
        boolean hasNightVision = false;

        // Check permanent enhancements
        Map<EnhancementType, Integer> enhancements = entityEnhancements.get(entity.getUUID());
        if (enhancements != null && enhancements.containsKey(EnhancementType.NIGHT_VISION)) {
            hasNightVision = true;
        }

        // Check temporary enhancements
        if (!hasNightVision) {
            Map<String, TemporaryEnhancement> temps = temporaryEnhancements.get(entity.getUUID());
            if (temps != null) {
                for (TemporaryEnhancement temp : temps.values()) {
                    if (temp.enhancement.getType() == EnhancementType.NIGHT_VISION) {
                        hasNightVision = true;
                        break;
                    }
                }
            }
        }

        // Apply night vision effect if needed
        if (hasNightVision) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    300, // 15 seconds (300 ticks), since tick is called every 5 ticks
                    0,
                    false,
                    false,
                    true
            ));
        }
    }

    @Override
    public void onPassiveAbilityGained(LivingEntity entity, ServerLevel serverLevel) {
        // Initial application will be handled by tick
        recalculateEnhancements(entity);
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        removeAllEnhancements(entity);
    }

    private void removeAllEnhancements(LivingEntity entity) {
        Map<EnhancementType, Integer> enhancements = entityEnhancements.get(entity.getUUID());
        if (enhancements != null) {
            for (Map.Entry<EnhancementType, Integer> entry : enhancements.entrySet()) {
                removeEnhancement(entity, entry.getKey());
            }
        }
        entityEnhancements.remove(entity.getUUID());
        lastCalculatedValues.remove(entity.getUUID());
        temporaryEnhancements.remove(entity.getUUID());
        enhancementBoosts.remove(entity.getUUID());
    }

    private void applyEnhancement(LivingEntity entity, PhysicalEnhancement enhancement) {
        if (enhancement.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(enhancement.getAttribute());
            if (instance != null) {
                String modifierId = BASE_MODIFIER_ID + "_" + enhancement.getType().name().toLowerCase();

                // Remove existing modifier if present
                instance.removeModifier(ResourceLocation.parse(modifierId));

                // Calculate and apply new modifier
                double value = enhancement.calculateValue();
                AttributeModifier modifier = new AttributeModifier(
                        ResourceLocation.parse(modifierId),
                        value,
                        enhancement.getOperation()
                );
                instance.addPermanentModifier(modifier);
            }
        }
    }

    private void removeEnhancement(LivingEntity entity, EnhancementType type) {
        Holder<Attribute> attribute = type.getAttribute();
        if (attribute != null) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                String modifierId = BASE_MODIFIER_ID + "_" + type.name().toLowerCase();
                instance.removeModifier(ResourceLocation.parse(modifierId));
            }
        }
    }

    // Temporary enhancement methods
    public static void addTemporaryEnhancement(LivingEntity entity, PhysicalEnhancement enhancement, String id, long duration) {
        temporaryEnhancements.computeIfAbsent(entity.getUUID(), k -> new ConcurrentHashMap<>())
                .put(id, new TemporaryEnhancement(enhancement, System.currentTimeMillis() + duration));

        applyTemporaryEnhancement(entity, enhancement, id);
    }

    public static void addTemporaryEnhancement(LivingEntity entity, PhysicalEnhancement enhancement, String id) {
        temporaryEnhancements.computeIfAbsent(entity.getUUID(), k -> new ConcurrentHashMap<>())
                .put(id, new TemporaryEnhancement(enhancement, -1)); // -1 means permanent until removed

        applyTemporaryEnhancement(entity, enhancement, id);
    }

    public static void removeTemporaryEnhancement(LivingEntity entity, String id) {
        Map<String, TemporaryEnhancement> enhancements = temporaryEnhancements.get(entity.getUUID());
        if (enhancements != null) {
            TemporaryEnhancement temp = enhancements.remove(id);
            if (temp != null) {
                removeTemporaryEnhancementEffect(entity, temp.enhancement, id);
            }
        }
    }

    private static void applyTemporaryEnhancement(LivingEntity entity, PhysicalEnhancement enhancement, String id) {
        if (enhancement.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(enhancement.getAttribute());
            if (instance != null) {
                String modifierId = BASE_MODIFIER_ID + "_temp_" + id;
                instance.removeModifier(ResourceLocation.parse(modifierId));

                double value = enhancement.calculateValue();
                AttributeModifier modifier = new AttributeModifier(
                        ResourceLocation.parse(modifierId),
                        value,
                        enhancement.getOperation()
                );
                instance.addPermanentModifier(modifier);
            }
        }
    }

    private static void removeTemporaryEnhancementEffect(LivingEntity entity, PhysicalEnhancement enhancement, String id) {
        if (enhancement.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(enhancement.getAttribute());
            if (instance != null) {
                String modifierId = BASE_MODIFIER_ID + "_temp_" + id;
                instance.removeModifier(ResourceLocation.parse(modifierId));
            }
        }
    }

    // Enhancement boost methods
    public static void addEnhancementBoost(LivingEntity entity, PhysicalEnhancement enhancement, String id, int amount, long duration) {
        enhancementBoosts.computeIfAbsent(entity.getUUID(), k -> new ConcurrentHashMap<>())
                .put(id, new EnhancementBoost(enhancement, amount, System.currentTimeMillis() + duration));

        applyEnhancementBoost(entity, enhancement, id, amount);
    }

    public static void addEnhancementBoost(LivingEntity entity, PhysicalEnhancement enhancement, String id, int amount) {
        enhancementBoosts.computeIfAbsent(entity.getUUID(), k -> new ConcurrentHashMap<>())
                .put(id, new EnhancementBoost(enhancement, amount, -1));

        applyEnhancementBoost(entity, enhancement, id, amount);
    }

    public static void removeEnhancementBoost(LivingEntity entity, String id) {
        Map<String, EnhancementBoost> boosts = enhancementBoosts.get(entity.getUUID());
        if (boosts != null) {
            EnhancementBoost boost = boosts.remove(id);
            if (boost != null) {
                removeEnhancementBoostEffect(entity, boost.enhancement, id);
            }
        }
    }

    private static void applyEnhancementBoost(LivingEntity entity, PhysicalEnhancement enhancement, String id, int amount) {
        if (enhancement.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(enhancement.getAttribute());
            if (instance != null) {
                String modifierId = BASE_MODIFIER_ID + "_boost_" + id;
                instance.removeModifier(ResourceLocation.parse(modifierId));

                int effectiveLevel = Math.max(0, enhancement.getLevel() + amount);
                PhysicalEnhancement boosted = new PhysicalEnhancement(
                        enhancement.getType(),
                        effectiveLevel
                );
                double value = boosted.calculateValue() - enhancement.calculateValue();

                AttributeModifier modifier = new AttributeModifier(
                        ResourceLocation.parse(modifierId),
                        value,
                        enhancement.getOperation()
                );
                instance.addPermanentModifier(modifier);
            }
        }
    }

    private static void removeEnhancementBoostEffect(LivingEntity entity, PhysicalEnhancement enhancement, String id) {
        if (enhancement.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(enhancement.getAttribute());
            if (instance != null) {
                String modifierId = BASE_MODIFIER_ID + "_boost_" + id;
                instance.removeModifier(ResourceLocation.parse(modifierId));
            }
        }
    }

    private void updateTemporaryEnhancements(LivingEntity entity) {
        Map<String, TemporaryEnhancement> enhancements = temporaryEnhancements.get(entity.getUUID());
        if (enhancements != null) {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<String, TemporaryEnhancement>> iterator = enhancements.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, TemporaryEnhancement> entry = iterator.next();
                TemporaryEnhancement temp = entry.getValue();

                if (temp.expiryTime != -1 && currentTime >= temp.expiryTime) {
                    removeTemporaryEnhancementEffect(entity, temp.enhancement, entry.getKey());
                    iterator.remove();
                }
            }
        }
    }

    private void updateEnhancementBoosts(LivingEntity entity) {
        Map<String, EnhancementBoost> boosts = enhancementBoosts.get(entity.getUUID());
        if (boosts != null) {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<String, EnhancementBoost>> iterator = boosts.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, EnhancementBoost> entry = iterator.next();
                EnhancementBoost boost = entry.getValue();

                if (boost.expiryTime != -1 && currentTime >= boost.expiryTime) {
                    removeEnhancementBoostEffect(entity, boost.enhancement, entry.getKey());
                    iterator.remove();
                }
            }
        }
    }

    public static int getResistanceLevel(UUID entityId) {
        Map<EnhancementType, Integer> enhancements = entityEnhancements.get(entityId);
        int level = 0;

        if (enhancements != null && enhancements.containsKey(EnhancementType.RESISTANCE)) {
            level = enhancements.get(EnhancementType.RESISTANCE);
        }

        // Add temporary enhancements
        Map<String, TemporaryEnhancement> temps = temporaryEnhancements.get(entityId);
        if (temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.RESISTANCE) {
                    level += temp.enhancement.getLevel();
                }
            }
        }

        // Add boosts
        Map<String, EnhancementBoost> boosts = enhancementBoosts.get(entityId);
        if (boosts != null) {
            for (EnhancementBoost boost : boosts.values()) {
                if (boost.enhancement.getType() == EnhancementType.RESISTANCE) {
                    level += boost.amount;
                }
            }
        }

        return Math.max(0, level);
    }

    @EventBusSubscriber(modid = LOTMCraft.MOD_ID)
    public static class EnhancementEventHandler {
        @SubscribeEvent
        public static void onLivingDamage(LivingIncomingDamageEvent event) {
            if (event.getEntity() instanceof LivingEntity entity) {
                int resistanceLevel = getResistanceLevel(entity.getUUID());

                if (resistanceLevel > 0) {
                    float reductionPercent = Math.min(resistanceLevel * 5f, 100f);
                    float damageMultiplier = 1f - (reductionPercent / 100f);

                    event.setAmount(event.getAmount() * damageMultiplier);
                }
            }
        }
    }

    private static class TemporaryEnhancement {
        final PhysicalEnhancement enhancement;
        final long expiryTime;

        TemporaryEnhancement(PhysicalEnhancement enhancement, long expiryTime) {
            this.enhancement = enhancement;
            this.expiryTime = expiryTime;
        }
    }

    private static class EnhancementBoost {
        final PhysicalEnhancement enhancement;
        final int amount;
        final long expiryTime;

        EnhancementBoost(PhysicalEnhancement enhancement, int amount, long expiryTime) {
            this.enhancement = enhancement;
            this.amount = amount;
            this.expiryTime = expiryTime;
        }
    }

    public static class PhysicalEnhancement {
        private final EnhancementType type;
        private final int level;

        public PhysicalEnhancement(EnhancementType type, int level) {
            this.type = type;
            this.level = level;
        }

        public EnhancementType getType() {
            return type;
        }

        public int getLevel() {
            return level;
        }

        public Holder<Attribute> getAttribute() {
            return type.getAttribute();
        }

        public AttributeModifier.Operation getOperation() {
            return type.getOperation();
        }

        public double calculateValue() {
            return level * type.getValuePerLevel();
        }
    }

    public enum EnhancementType {
        STRENGTH(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE, 2.0),
        SPEED(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.05),
        DEFENSE(Attributes.ARMOR, AttributeModifier.Operation.ADD_VALUE, 1.0),
        TOUGHNESS(Attributes.ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_VALUE, 0.5),
        HEALTH(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE, 2.0),
        KNOCKBACK_RESISTANCE(Attributes.KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE, 0.05),
        ATTACK_SPEED(Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.05),
        RESISTANCE(null, null, 0), // Special: handled via event
        NIGHT_VISION(null, null, 0); // Special: handled via effects

        private final Holder<Attribute> attribute;
        private final AttributeModifier.Operation operation;
        private final double valuePerLevel;

        EnhancementType(Holder<Attribute> attribute, AttributeModifier.Operation operation, double valuePerLevel) {
            this.attribute = attribute;
            this.operation = operation;
            this.valuePerLevel = valuePerLevel;
        }

        public Holder<Attribute> getAttribute() {
            return attribute;
        }

        public AttributeModifier.Operation getOperation() {
            return operation;
        }

        public double getValuePerLevel() {
            return valuePerLevel;
        }
    }
}