package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;


public class MentalResistanceAbility extends PassiveAbility{
    public MentalResistanceAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 7));}
    private final Map<LivingEntity, Integer> processedEffects = new HashMap<>();
    @Override
    public void tick(Level level, LivingEntity entity) {
    }
    public static float reduceSanityDrain(LivingEntity entity, float drain) {
        PassiveAbility mental = PassiveAbilityHandler.getById("mental_resistance_ability");
        boolean hasMental = mental != null && mental.shouldApplyTo(entity) && BeyonderData.getSequence(entity) > 2;
        boolean hasMalice = MaliceAuthorityAbility.isActiveFor(entity);
        if (hasMental && hasMalice) return drain * 0.5f;
        if (hasMental || hasMalice) return drain * 0.75f;
        return drain;
    }
}