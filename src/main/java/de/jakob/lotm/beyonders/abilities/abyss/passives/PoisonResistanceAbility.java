package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;

public class PoisonResistanceAbility extends PassiveAbility {
    public PoisonResistanceAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
        // No POISON
        MobEffectInstance Poison = entity.getEffect(MobEffects.POISON);
        if (Poison != null) {entity.removeEffect(MobEffects.POISON);}
    }
}
