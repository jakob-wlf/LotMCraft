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

public class CurseResistanceAbility extends PassiveAbility {
    public CurseResistanceAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 6));}
    @Override
    public void tick(Level level, LivingEntity entity) {
        // Reduce LOOSING_CONTROL duration once per 2 second
        MobEffectInstance Wither = entity.getEffect(MobEffects.WITHER);
        if (Wither != null) {
            entity.removeEffect(MobEffects.WITHER);
        }
    }
    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Pre event) {// 30% resistance
        if (!event.getSource().is(ModDamageTypes.DEMONESS_GENERIC))
            return;
        LivingEntity entity = event.getEntity();
        float damage = event.getNewDamage();
        event.setNewDamage(damage * 0.7F);
    }
}
