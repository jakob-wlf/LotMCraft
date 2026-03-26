package de.jakob.lotm.abilities.abyss.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class WordImmunityAbility extends PassiveAbilityItem {

    public static final HashSet<LivingEntity> IMMUNE_ENTITIES = new HashSet<>();

    public WordImmunityAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 3));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        IMMUNE_ENTITIES.removeIf(e -> !this.shouldApplyTo(e));
        IMMUNE_ENTITIES.add(entity);

        // Actively remove nausea and blindness as a fallback
        if (entity.hasEffect(MobEffects.CONFUSION)) {
            entity.removeEffect(MobEffects.CONFUSION);
        }
        if (entity.hasEffect(MobEffects.BLINDNESS)) {
            entity.removeEffect(MobEffects.BLINDNESS);
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!IMMUNE_ENTITIES.contains(event.getEntity())) {
            return;
        }

        WordImmunityAbility ability = (WordImmunityAbility) PassiveAbilityHandler.WORD_IMMUNITY_ABYSS.get();
        if (!ability.shouldApplyTo(event.getEntity())) {
            IMMUNE_ENTITIES.remove(event.getEntity());
            return;
        }

        var effectHolder = event.getEffectInstance().getEffect();
        if (effectHolder == MobEffects.CONFUSION || effectHolder == MobEffects.BLINDNESS) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}
