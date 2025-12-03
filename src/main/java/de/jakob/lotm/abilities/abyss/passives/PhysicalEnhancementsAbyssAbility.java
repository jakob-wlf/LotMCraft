package de.jakob.lotm.abilities.abyss.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PhysicalEnhancementsAbyssAbility extends PassiveAbilityItem {

    private final HashMap<Integer, List<MobEffectInstance>> effectsPerSequence = new HashMap<>();

    public PhysicalEnhancementsAbyssAbility(Properties properties) {
        super(properties);

        initEffects();
    }

    private void initEffects() {
        effectsPerSequence.put(9, List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 0, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 0, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(7, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 0, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 1, false, false, false)
        ));

        effectsPerSequence.put(6, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 1, false, false, false)
        ));

        effectsPerSequence.put(5, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 4, false, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 1, false, false, false)
        ));

        effectsPerSequence.put(4, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 9, false, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 2, false, false, false)
        ));

        effectsPerSequence.put(3, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 10, false, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 2, false, false, false)
        ));

        effectsPerSequence.put(2, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 5, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 12, false, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 3, false, false, false)
        ));

        effectsPerSequence.put(1, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 5, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 15, false, false, false),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 3, false, false, false)
        ));

    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "abyss", 9
        ));
    }

    private static final HashMap<UUID, Long> reducedRegen = new HashMap<>();

    @Override
    public void tick(Level level, LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);

        if (sequence < 0 || sequence > 9) {
            return;
        }

        ArrayList<MobEffectInstance> effects = new ArrayList<>(getEffectsForSequence(sequence));

        if(reducedRegen.containsKey(entity.getUUID())) {
            applyRegenReduce(effects, entity, reducedRegen);
        }

        applyPotionEffects(entity, effects);
    }

    @SubscribeEvent
    public static void onLivingDamageLiving(LivingDamageEvent.Post event) {
        if(!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!serverLevel.getGameRules().getBoolean(ModGameRules.REDUCE_REGEN_IN_BEYONDER_FIGHT)) {
            return;
        }

        if(!(event.getSource().getEntity() instanceof LivingEntity source)) {
            return;
        }

        LivingEntity target = event.getEntity();
        if(!BeyonderData.isBeyonder(target) || !BeyonderData.isBeyonder(source)) {
            return;
        }

        if(!reducedRegen.containsKey(target.getUUID()) || (reducedRegen.get(target.getUUID()) - System.currentTimeMillis()) <= 0) {
            target.removeEffect(MobEffects.REGENERATION);
        }
        reducedRegen.put(target.getUUID(), System.currentTimeMillis() + 10000);
    }

    private List<MobEffectInstance> getEffectsForSequence(int sequence) {
        if (effectsPerSequence.containsKey(sequence)) {
            return effectsPerSequence.get(sequence);
        } else {
            for (int i = sequence; i < 10; i++) {
                if (effectsPerSequence.containsKey(i)) {
                    return effectsPerSequence.get(i);
                }
            }

            return List.of();
        }
    }
}