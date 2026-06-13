package de.jakob.lotm.abilities;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PhysicalEnhancementsAbility extends PassiveAbilityItem {

    private static final String BASE_MODIFIER_ID = "lotm_physical_enhancement";

    private final Map<EnhancementType, ResourceLocation> modifierIdCache = new EnumMap<>(EnhancementType.class);
    private String cachedPathwayName = null;
    private static final Map<EnhancementType, ResourceLocation> PERMANENT_MODIFIER_IDS = new EnumMap<>(EnhancementType.class);
    static {
        for (EnhancementType type : EnhancementType.values()) {
            PERMANENT_MODIFIER_IDS.put(type, ResourceLocation.parse(BASE_MODIFIER_ID + "_" + type.name().toLowerCase()));
        }
    }

    private static final Map<UUID, Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>>> entityEnhancements = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, TemporaryEnhancement>> temporaryEnhancements = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, EnhancementBoost>> enhancementBoosts = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> reducedRegen = new ConcurrentHashMap<>();

    /** Suppresses passive regen for the given entity for the specified duration in milliseconds. */
    public static void suppressRegen(LivingEntity entity, long durationMs) {
        long expiry = System.currentTimeMillis() + durationMs;
        if (!reducedRegen.containsKey(entity.getUUID()) || reducedRegen.get(entity.getUUID()) < expiry) {
            reducedRegen.put(entity.getUUID(), expiry);
        }
        entity.removeEffect(MobEffects.REGENERATION);
    }

    // FIX 2: Track the last known sequence level per entity so that attribute modifiers
    // (health, speed, strength, etc.) are only removed/re-added when the sequence actually
    // changes, rather than unconditionally every 5 ticks. Attribute changes trigger
    // Minecraft's internal attribute recalculation, so eliminating unnecessary churn here
    // is a significant server-side win.
    private static final Map<UUID, Integer> lastKnownSequence = new ConcurrentHashMap<>();

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

    public int getEnhancementLevelOfEnhancementTypeForSequence(int sequenceLevel, EnhancementType type) {
        return getEnhancementsForSequence(sequenceLevel).stream()
                .filter(enh -> enh.getType() == type)
                .mapToInt(PhysicalEnhancement::getLevel)
                .sum();
    }

    protected List<PhysicalEnhancement> getEnhancementsForSequence(int sequenceLevel, LivingEntity entity) {
        return getEnhancementsForSequence(sequenceLevel);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;

        recalculateEnhancements(entity);

        // FIX 3: Fetch all three maps once here and pass them down to every apply method.
        // Previously each of the 8+ apply methods independently called
        // entityEnhancements.get(uuid), temporaryEnhancements.get(uuid), and
        // enhancementBoosts.get(uuid), resulting in ~19 ConcurrentHashMap lookups per tick
        // per entity for the same data. Now we do 3 lookups total.
        UUID uuid = entity.getUUID();
        Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements = entityEnhancements.get(uuid);
        Map<String, TemporaryEnhancement> temps = temporaryEnhancements.get(uuid);
        Map<String, EnhancementBoost> boosts = enhancementBoosts.get(uuid);

        applyConcealment(entity, allEnhancements, temps);
        applyNightVision(entity, allEnhancements, temps);
        applyConduit(entity, allEnhancements, temps);
        applyDolphinsGrace(entity, allEnhancements, temps);
        applySaturation(entity, allEnhancements, temps);
        applyWaterBreathing(entity, allEnhancements, temps);
        applyFireRes(entity, allEnhancements, temps);
        applyRegeneration(entity, allEnhancements, temps, boosts);

        updateTemporaryEnhancements(entity);
        updateEnhancementBoosts(entity);
    }

    private void recalculateEnhancements(LivingEntity entity) {
        int sequenceLevel = getCurrentSequenceLevel(entity);

        Integer cached = lastKnownSequence.get(entity.getUUID());
        if (cached != null && cached == sequenceLevel) {
            return;
        }
        lastKnownSequence.put(entity.getUUID(), sequenceLevel);

        List<PhysicalEnhancement> currentEnhancements = getEnhancementsForSequence(sequenceLevel, entity);

        if(entity instanceof ServerPlayer player){
            var dataOp = BeyonderData.playerMap.get(entity);

            if(dataOp.isPresent()) {
                var data = dataOp.get();

                ControllingDataComponent controllingData = player.getData(ModAttachments.CONTROLLING_DATA);
                List<Characteristic> charList = data.chars();
                if (!charList.isEmpty() && controllingData.getTargetUUID() == null && !controllingData.isControlling()) {

                    if (sequenceLevel < 9) {
                        currentEnhancements = currentEnhancements.stream()
                                        .map(obj -> obj.type.equals(EnhancementType.HEALTH) ?
                                        new PhysicalEnhancement(EnhancementType.HEALTH,
                                                recalculateHealthLevelWithStacks(sequenceLevel, obj.level, charList, data.uniqueness(), null))
                                        : obj)
                                .toList();
                    }
                }
            }
        }

        Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> previousMapForEntity = entityEnhancements.getOrDefault(entity.getUUID(), Collections.emptyMap());
        Map<EnhancementType, Integer> previousEnhancements = previousMapForEntity.getOrDefault(this, Collections.emptyMap());

        Map<EnhancementType, Integer> enhancementMap = new HashMap<>();
        for (PhysicalEnhancement enhancement : currentEnhancements) {
            if (enhancement.type.equals(EnhancementType.SPEED)) {
                if (!Objects.equals(this.getPathwayName(), BeyonderData.getPathway(entity))) {
                    continue;
                }
            }
            applyEnhancement(entity, enhancement);
            enhancementMap.put(enhancement.getType(), enhancement.getLevel());
        }

        for (EnhancementType previousType : previousEnhancements.keySet()) {
            if (!enhancementMap.containsKey(previousType)) {
                removeEnhancement(entity, previousType);
            }
        }

        entityEnhancements.compute(entity.getUUID(), (k, v) -> {
            if (v == null) v = new ConcurrentHashMap<>();
            v.put(this, enhancementMap);
            return v;
        });

        reapplyTemporaryEnhancements(entity);
        reapplyEnhancementBoosts(entity);
    }

    private static void reapplyTemporaryEnhancements(LivingEntity entity) {
        Map<String, TemporaryEnhancement> temps = temporaryEnhancements.get(entity.getUUID());
        if (temps == null) return;
        for (Map.Entry<String, TemporaryEnhancement> entry : temps.entrySet()) {
            applyTemporaryEnhancement(entity, entry.getValue().enhancement, entry.getKey());
        }
    }

    private static void reapplyEnhancementBoosts(LivingEntity entity) {
        Map<String, EnhancementBoost> boosts = enhancementBoosts.get(entity.getUUID());
        if (boosts == null) return;
        for (Map.Entry<String, EnhancementBoost> entry : boosts.entrySet()) {
            EnhancementBoost boost = entry.getValue();
            applyEnhancementBoost(entity, boost.type, entry.getKey(), boost.amount);
        }
    }

    protected int getCurrentSequenceLevel(LivingEntity entity) {
        return BeyonderData.getSequence(entity, getPathwayName());
    }

    protected int recalculateHealthLevelWithStacks(int seq, int prevLevel, List<Characteristic> characteristics, String uniqueness, String pathwayToConsider){
        int result = prevLevel;
        String myPathway = pathwayToConsider; // null means consider all pathways

        for(int i = 9; i >= seq; i--){
            final int s = i;
            float buff;

            if (i == 1) {
                buff = (int) characteristics.stream()
                        .filter(c -> (myPathway == null || c.pathway().equals(myPathway)) && c.sequence() == 1)
                        .count();
            } else {
                buff = (int) characteristics.stream()
                        .filter(c -> (myPathway == null || c.pathway().equals(myPathway)) && c.sequence() == s)
                        .count();
            }
            int stacks = characteristics.stream()
                    .filter(c -> (myPathway == null || c.pathway().equals(myPathway)) && c.sequence() == s)
                    .mapToInt(c -> Math.max(0, c.stack() - 1))
                    .sum();
            buff *=  ((stacks*4f)/(stacks+18f) + 1);

            //buff = (i == 1 && buff >= 0) ? buff : (buff > 0) ? 1 : 0;

            switch (i) {
                case 8 -> result += buff;
                case 7 -> result += buff * 2;
                case 6, 5 -> result += buff * 3;
                case 4, 3 -> result += buff * 5;
                case 2 -> result += buff * 7;
                case 1 -> result += buff * 7;
                case 0 -> result += buff * 15;
            }
        }

        if (myPathway != null && uniqueness != null && uniqueness.equalsIgnoreCase(myPathway)) {
            result += 20;
        }

        return result;
    }

    // All apply* methods below now accept pre-fetched maps (FIX 3) instead of
    // looking them up from the ConcurrentHashMaps individually.

    private void applyConcealment(LivingEntity entity,
                                  Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements,
                                  Map<String, TemporaryEnhancement> temps) {
        boolean hasConcealment = false;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.CONCEALMENT)) {
                    hasConcealment = true;
                    break;
                }
            }
        }

        if (!hasConcealment && temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.CONCEALMENT) {
                    hasConcealment = true;
                    break;
                }
            }
        }

        if (hasConcealment) {
            entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 300, 1, false, false, false));
        }
    }

    private void applyNightVision(LivingEntity entity,
                                  Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements,
                                  Map<String, TemporaryEnhancement> temps) {
        boolean hasNightVision = false;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.NIGHT_VISION)) {
                    hasNightVision = true;
                    break;
                }
            }
        }

        if (!hasNightVision && temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.NIGHT_VISION) {
                    hasNightVision = true;
                    break;
                }
            }
        }

        if (hasNightVision) {
            entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, false));
        }
    }

    private void applyFireRes(LivingEntity entity,
                              Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements,
                              Map<String, TemporaryEnhancement> temps) {
        boolean hasFireRes = false;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.FIRE_RESISTANCE)) {
                    hasFireRes = true;
                    break;
                }
            }
        }

        if (!hasFireRes && temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.FIRE_RESISTANCE) {
                    hasFireRes = true;
                    break;
                }
            }
        }

        if (hasFireRes) {
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, false, false, false));
        }
    }

    private void applyConduit(LivingEntity entity,
                              Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements,
                              Map<String, TemporaryEnhancement> temps) {
        boolean hasEffect = false;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.CONDUIT)) {
                    hasEffect = true;
                    break;
                }
            }
        }

        if (!hasEffect && temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.CONDUIT) {
                    hasEffect = true;
                    break;
                }
            }
        }

        if (hasEffect) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 300, 0, false, false, false));
        }
    }

    private void applySaturation(LivingEntity entity,
                                 Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements,
                                 Map<String, TemporaryEnhancement> temps) {
        boolean hasEffect = false;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.SATURATION)) {
                    hasEffect = true;
                    break;
                }
            }
        }

        if (!hasEffect && temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.SATURATION) {
                    hasEffect = true;
                    break;
                }
            }
        }

        if (hasEffect || (BeyonderData.isBeyonder(entity) && BeyonderData.getSequence(entity) <= 2)) {
            if (entity instanceof Player player) {
                player.getFoodData().setSaturation(20);
                player.getFoodData().setFoodLevel(20);
            }
        }
    }

    private void applyDolphinsGrace(LivingEntity entity,
                                    Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements,
                                    Map<String, TemporaryEnhancement> temps) {
        boolean hasEffect = false;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.DOLPHINS_GRACE)) {
                    hasEffect = true;
                    break;
                }
            }
        }

        if (!hasEffect && temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.DOLPHINS_GRACE) {
                    hasEffect = true;
                    break;
                }
            }
        }

        if (hasEffect) {
            entity.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 300, 0, false, false, false));
        }
    }

    private void applyWaterBreathing(LivingEntity entity,
                                     Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements,
                                     Map<String, TemporaryEnhancement> temps) {
        boolean hasEffect = false;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.UNDERWATER_BREATHING)) {
                    hasEffect = true;
                    break;
                }
            }
        }

        if (!hasEffect && temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.UNDERWATER_BREATHING) {
                    hasEffect = true;
                    break;
                }
            }
        }

        if (hasEffect) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }
    }

    private void applyRegeneration(LivingEntity entity,
                                   Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements,
                                   Map<String, TemporaryEnhancement> temps,
                                   Map<String, EnhancementBoost> boosts) {
        int regenLevel = 0;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.REGENERATION)) {
                    regenLevel += enhancements.get(EnhancementType.REGENERATION);
                }
            }
        }

        if (temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.REGENERATION) {
                    regenLevel += temp.enhancement.getLevel();
                }
            }
        }

        if (boosts != null) {
            for (EnhancementBoost boost : boosts.values()) {
                if (boost.type == EnhancementType.REGENERATION) {
                    regenLevel += boost.amount;
                }
            }
        }

        if (regenLevel <= 0) return;

        if (reducedRegen.containsKey(entity.getUUID())) {
            long expiryTime = reducedRegen.get(entity.getUUID());
            if (System.currentTimeMillis() >= expiryTime) {
                reducedRegen.remove(entity.getUUID());
            } else {
                regenLevel = regenLevel - 5;
                if (regenLevel <= 0) return;
            }
        }

        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, regenLevel - 1, false, false, false));
    }

    @Override
    public void onPassiveAbilityGained(LivingEntity entity, ServerLevel serverLevel) {
        recalculateEnhancements(entity);
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        removeMyEnhancements(entity);
    }

    private void removeMyEnhancements(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        // Clear the sequence cache so callPassiveEffectsOnAdd will re-apply modifiers
        // after a remove+add cycle (e.g. double setBeyonder call on NPC spawn).
        lastKnownSequence.remove(uuid);
        Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements = entityEnhancements.get(uuid);
        if (allEnhancements != null) {
            Map<EnhancementType, Integer> myEnhancements = allEnhancements.remove(this);
            if (myEnhancements != null) {
                for (EnhancementType type : myEnhancements.keySet()) {
                    removeEnhancement(entity, type);
                }
            }
            if (allEnhancements.isEmpty()) {
                entityEnhancements.remove(uuid);
            }
        }
    }

    public static void resetEnhancements(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements = entityEnhancements.remove(uuid);
        if (allEnhancements != null) {
            for (Map.Entry<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> entry : allEnhancements.entrySet()) {
                PhysicalEnhancementsAbility ability = entry.getKey();
                for (EnhancementType type : entry.getValue().keySet()) {
                    ability.removeEnhancement(entity, type);
                }
            }
        }
        temporaryEnhancements.remove(uuid);
        enhancementBoosts.remove(uuid);
        reducedRegen.remove(uuid);
    }

    private void removeAllEnhancements(LivingEntity entity) {
        Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> enhancements = entityEnhancements.get(entity.getUUID());
        if (enhancements != null) {
            for (Map<EnhancementType, Integer> map : enhancements.values()) {
                for (EnhancementType type : map.keySet()) {
                    removeEnhancement(entity, type);
                }
            }
        }

        Map<String, TemporaryEnhancement> temps = temporaryEnhancements.get(entity.getUUID());
        if (temps != null) {
            for (Map.Entry<String, TemporaryEnhancement> entry : temps.entrySet()) {
                removeTemporaryEnhancementEffect(entity, entry.getValue().enhancement, entry.getKey());
            }
        }

        Map<String, EnhancementBoost> boosts = enhancementBoosts.get(entity.getUUID());
        if (boosts != null) {
            for (Map.Entry<String, EnhancementBoost> entry : boosts.entrySet()) {
                removeEnhancementBoostEffect(entity, entry.getValue().type, entry.getKey());
            }
        }

        System.out.println("removeAllEnhancements called for " + entity.getUUID());
        entityEnhancements.remove(entity.getUUID());
        temporaryEnhancements.remove(entity.getUUID());
        enhancementBoosts.remove(entity.getUUID());
        reducedRegen.remove(entity.getUUID());
        lastKnownSequence.remove(entity.getUUID());
    }
    private String getPathwayName() {
        if (cachedPathwayName == null) {
            cachedPathwayName = getRequirements().keySet().stream().findFirst().orElse("none");
        }
        return cachedPathwayName;
    }

    private ResourceLocation getModifierId(EnhancementType type) {
        return modifierIdCache.computeIfAbsent(type, t ->
                ResourceLocation.parse(BASE_MODIFIER_ID + "_" + getPathwayName() + "_" + t.name().toLowerCase()));
    }

    private void applyEnhancement(LivingEntity entity, PhysicalEnhancement enhancement) {
        if (enhancement.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(enhancement.getAttribute());
            if (instance != null) {
                // Use per-ability/per-pathway modifier id to avoid collisions between abilities
                ResourceLocation modifierId = getModifierId(enhancement.getType());

                double value = enhancement.calculateValue();
                AttributeModifier existing = instance.getModifier(modifierId);
                if (existing != null) {
                    if (Math.abs(existing.amount() - value) < 0.0001 && existing.operation() == enhancement.getOperation()) {
                        return; // Already has the correct modifier, avoid flickering
                    }
                    instance.removeModifier(modifierId);
                }

                AttributeModifier modifier = new AttributeModifier(modifierId, value, enhancement.getOperation());
                instance.addPermanentModifier(modifier);
            }
        }
    }

    private void removeEnhancement(LivingEntity entity, EnhancementType type) {
        Holder<Attribute> attribute = type.getAttribute();
        if (attribute != null) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
            instance.removeModifier(getModifierId(type));
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
                .put(id, new TemporaryEnhancement(enhancement, -1));
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
                ResourceLocation modifierId = tempModifierId(id);
                instance.removeModifier(modifierId);

                double value = enhancement.calculateValue();
                AttributeModifier modifier = new AttributeModifier(modifierId, value, enhancement.getOperation());
                instance.addPermanentModifier(modifier);
            }
        }
    }

    private static void removeTemporaryEnhancementEffect(LivingEntity entity, PhysicalEnhancement enhancement, String id) {
        if (enhancement.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(enhancement.getAttribute());
            if (instance != null) {
                instance.removeModifier(tempModifierId(id));
                lastKnownSequence.remove(entity.getUUID()); // force recalculation next tick
            }
        }
    }

    public static void addEnhancementBoost(LivingEntity entity, EnhancementType type, String id, int amount, long durationMillis) {
        enhancementBoosts.computeIfAbsent(entity.getUUID(), k -> new ConcurrentHashMap<>())
                .put(id, new EnhancementBoost(type, amount, System.currentTimeMillis() + durationMillis));
        applyEnhancementBoost(entity, type, id, amount);
    }

    public static void addEnhancementBoost(LivingEntity entity, EnhancementType type, String id, int amount) {
        enhancementBoosts.computeIfAbsent(entity.getUUID(), k -> new ConcurrentHashMap<>())
                .put(id, new EnhancementBoost(type, amount, -1));
        applyEnhancementBoost(entity, type, id, amount);
    }

    public static void removeEnhancementBoost(LivingEntity entity, String id) {
        Map<String, EnhancementBoost> boosts = enhancementBoosts.get(entity.getUUID());
        if (boosts != null) {
            EnhancementBoost boost = boosts.remove(id);
            if (boost != null) {
                removeEnhancementBoostEffect(entity, boost.type, id);
                lastKnownSequence.remove(entity.getUUID()); // force recalculation next tick
            }
        }
    }

    private static void applyEnhancementBoost(LivingEntity entity, EnhancementType type, String id, int amount) {
        if (type.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(type.getAttribute());
            if (instance != null) {
                ResourceLocation modifierId = boostModifierId(id);
                instance.removeModifier(modifierId);

                double value = amount * type.getValuePerLevel();
                AttributeModifier modifier = new AttributeModifier(modifierId, value, type.getOperation());
                instance.addTransientModifier(modifier);
            }
        }
    }

    private static void removeEnhancementBoostEffect(LivingEntity entity, EnhancementType type, String id) {
        if (type.getAttribute() != null) {
            AttributeInstance instance = entity.getAttribute(type.getAttribute());
            if (instance != null) {
                instance.removeModifier(boostModifierId(id));
            }
        }
    }

    private static ResourceLocation boostModifierId(String id) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, BASE_MODIFIER_ID + "_boost_" + id);
    }

    private static ResourceLocation tempModifierId(String id) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, BASE_MODIFIER_ID + "_temp_" + id);
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
                    removeEnhancementBoostEffect(entity, boost.type, entry.getKey());
                    iterator.remove();
                }
            }
        }
    }

    public static int getResistanceLevel(UUID entityId) {
        Map<PhysicalEnhancementsAbility, Map<EnhancementType, Integer>> allEnhancements = entityEnhancements.get(entityId);
        int level = 0;

        if (allEnhancements != null) {
            for (Map<EnhancementType, Integer> enhancements : allEnhancements.values()) {
                if (enhancements.containsKey(EnhancementType.RESISTANCE)) {
                    level += enhancements.get(EnhancementType.RESISTANCE);
                }
            }
        }

        Map<String, TemporaryEnhancement> temps = temporaryEnhancements.get(entityId);
        if (temps != null) {
            for (TemporaryEnhancement temp : temps.values()) {
                if (temp.enhancement.getType() == EnhancementType.RESISTANCE) {
                    level += temp.enhancement.getLevel();
                }
            }
        }

        Map<String, EnhancementBoost> boosts = enhancementBoosts.get(entityId);
        if (boosts != null) {
            for (EnhancementBoost boost : boosts.values()) {
                if (boost.type == EnhancementType.RESISTANCE) {
                    level += boost.amount;
                }
            }
        }

        return Math.max(0, level);
    }

    @EventBusSubscriber(modid = LOTMCraft.MOD_ID)
    public static class EnhancementEventHandler {
        @SubscribeEvent(priority = EventPriority.LOWEST)
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

        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            UUID uuid = event.getEntity().getUUID();
            Long expiry = reducedRegen.get(uuid);
            if (expiry == null) return;
            if (System.currentTimeMillis() >= expiry) {
                reducedRegen.remove(uuid);
                return;
            }
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onLivingDamagePost(LivingDamageEvent.Post event) {
            if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
            if (!serverLevel.getGameRules().getBoolean(ModGameRules.REDUCE_REGEN_IN_BEYONDER_FIGHT)) return;
            if (!(event.getSource().getEntity() instanceof LivingEntity source)) return;

            LivingEntity target = event.getEntity();
            if (!BeyonderData.isBeyonder(target) || !BeyonderData.isBeyonder(source)) return;

            if (!reducedRegen.containsKey(target.getUUID()) ||
                    (reducedRegen.get(target.getUUID()) - System.currentTimeMillis()) <= 0) {
                target.removeEffect(MobEffects.REGENERATION);
            }

            reducedRegen.put(target.getUUID(), System.currentTimeMillis() + 10000);
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
        final EnhancementType type;
        final int amount;
        final long expiryTime;

        EnhancementBoost(EnhancementType type, int amount, long expiryTime) {
            this.type = type;
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

        public EnhancementType getType() { return type; }
        public int getLevel() { return level; }
        public Holder<Attribute> getAttribute() { return type.getAttribute(); }
        public AttributeModifier.Operation getOperation() { return type.getOperation(); }
        public double calculateValue() { return level * type.getValuePerLevel(); }
    }

    public enum EnhancementType {
        STRENGTH(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE, 3.0),
        SPEED(Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_VALUE, 0.02),
        HEALTH(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE, 4.0),
        MINING_EFFICIENCY(Attributes.MINING_EFFICIENCY,AttributeModifier.Operation.ADD_VALUE,1.0),
        KNOCKBACK_RESISTANCE(Attributes.KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_VALUE, 0.05),
        ATTACK_SPEED(Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, 0.05),
        RESISTANCE(null, null, 0),
        NIGHT_VISION(null, null, 0),
        FIRE_RESISTANCE(null, null, 0),
        LUCK(null, null, 0),
        REGENERATION(null, null, 0),
        CONDUIT(null, null, 0),
        DOLPHINS_GRACE(Attributes.WATER_MOVEMENT_EFFICIENCY, AttributeModifier.Operation.ADD_VALUE, 1),
        OXYGEN_BONUS(Attributes.OXYGEN_BONUS, AttributeModifier.Operation.ADD_VALUE, 1),
        UNDERWATER_BREATHING(null, null, 0),
        SATURATION(null, null, 0),
        CONCEALMENT(null, null, 0);

        private final Holder<Attribute> attribute;
        private final AttributeModifier.Operation operation;
        private final double valuePerLevel;

        EnhancementType(Holder<Attribute> attribute, AttributeModifier.Operation operation, double valuePerLevel) {
            this.attribute = attribute;
            this.operation = operation;
            this.valuePerLevel = valuePerLevel;
        }

        public Holder<Attribute> getAttribute() { return attribute; }
        public AttributeModifier.Operation getOperation() { return operation; }
        public double getValuePerLevel() { return valuePerLevel; }
    }

    public static void resetEnhancements(UUID uuid, LivingEntity entity, boolean removeBoosts) {
        entityEnhancements.remove(uuid);
        temporaryEnhancements.remove(uuid);

        if (removeBoosts) {
            Map<String, EnhancementBoost> boosts = enhancementBoosts.get(uuid);

            if (boosts != null && entity != null) {
                for (Map.Entry<String, EnhancementBoost> entry : boosts.entrySet()) {
                    removeEnhancementBoostEffect(
                            entity,
                            entry.getValue().type,
                            entry.getKey()
                    );
                }
            }

            enhancementBoosts.remove(uuid);
        }

        lastKnownSequence.remove(uuid);
    }
}