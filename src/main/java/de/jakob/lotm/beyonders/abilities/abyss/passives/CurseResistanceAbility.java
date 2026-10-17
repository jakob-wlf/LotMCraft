package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;

public class CurseResistanceAbility extends PassiveAbility {
    public CurseResistanceAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 6));}
    @Override
    public void tick(Level level, LivingEntity entity) {
    }
    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(ModDamageTypes.CURSE)) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity caster)) return;
        if (caster == entity) return;
        PassiveAbility ability = PassiveAbilityHandler.getById("curse_resistance_ability");
        if (ability == null || !ability.shouldApplyTo(entity)) return;
        int difference = BeyonderData.getSequence(entity) - BeyonderData.getSequence(caster);
        float multiplier = Math.max(0.0F, 1.0F + difference * 0.25f);
        event.setNewDamage(event.getNewDamage() * multiplier);
    }
}
