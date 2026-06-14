package de.jakob.lotm.beyonders.abilities.death.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SolarSensitivityAbility extends PassiveAbilityItem {

    public static final HashSet<LivingEntity> affectedEntities = new HashSet<>();

    public SolarSensitivityAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 7));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        affectedEntities.removeIf(e -> !this.shouldApplyTo(e));
        affectedEntities.add(entity);

        if (level.isDay()) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0, false, false, true));
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!affectedEntities.contains(event.getEntity())) return;

        var damageTypeKey = event.getSource().typeHolder().unwrapKey();
        if (damageTypeKey.isEmpty()) return;

        var key = damageTypeKey.get();
        if (key.equals(ModDamageTypes.PURIFICATION) || key.equals(ModDamageTypes.PURIFICATION_INDIRECT)) {
            event.setAmount(event.getAmount() * 1.5f);
        }
    }
}
