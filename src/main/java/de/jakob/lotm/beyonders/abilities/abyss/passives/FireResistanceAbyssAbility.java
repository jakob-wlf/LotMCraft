package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;

public class FireResistanceAbyssAbility extends PassiveAbility {

    public FireResistanceAbyssAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "abyss", 6
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }
    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Pre event) {// 30% resistance
        if (!event.getSource().is(ModDamageTypes.HUNTER_FIRE))
            return;
        LivingEntity entity = event.getEntity();
        float damage = event.getNewDamage();
        event.setNewDamage(damage * 0.7F);
    }
}
