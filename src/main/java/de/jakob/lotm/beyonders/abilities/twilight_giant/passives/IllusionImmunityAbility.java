package de.jakob.lotm.beyonders.abilities.twilight_giant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class IllusionImmunityAbility extends PassiveAbility {

    private static final float RESIST_LOSS_PER_SEQUENCE = 0.25f;

    private static IllusionImmunityAbility instance;

    public IllusionImmunityAbility(String id) {
        super(id);
        instance = this;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 5));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void onPassiveAbilityGained(LivingEntity entity, ServerLevel serverLevel) {
        illusions().forEach(entity::removeEffect);
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity target = event.getEntity();
        if (instance == null || target.level().isClientSide()) return;
        if (!illusions().contains(event.getEffectInstance().getEffect()) || !instance.shouldApplyTo(target)) return;
        if (target.getRandom().nextFloat() < resistChance(event.getEffectSource(), target)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    private static float resistChance(Object source, LivingEntity target) {
        if (!(source instanceof LivingEntity caster) || caster == target) return 1f;
        int difference = BeyonderData.getSequence(target) - BeyonderData.getSequence(caster);
        if (difference <= 0) return 1f;
        return Math.max(0f, 1f - RESIST_LOSS_PER_SEQUENCE * difference);
    }

    private static List<Holder<MobEffect>> illusions() {
        return List.of(MobEffects.CONFUSION, MobEffects.BLINDNESS, MobEffects.DARKNESS, ModEffects.FOOLING);
    }
}
