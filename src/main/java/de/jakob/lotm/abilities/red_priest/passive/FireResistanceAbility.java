package de.jakob.lotm.abilities.red_priest.passive;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FireResistanceAbility extends PassiveAbilityItem {

    public FireResistanceAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "red_priest", 7
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Pre event) {
        if(!event.getSource().is(DamageTypeTags.BURN_FROM_STEPPING) && !event.getSource().is(DamageTypeTags.IS_FIRE))
            return;

        LivingEntity victim = event.getEntity();
        if(((FlamingHitAbility) (PassiveAbilityHandler.FLAMING_HIT.get())).shouldApplyTo(victim)) {
            victim.setRemainingFireTicks(0);
        }
    }
}
