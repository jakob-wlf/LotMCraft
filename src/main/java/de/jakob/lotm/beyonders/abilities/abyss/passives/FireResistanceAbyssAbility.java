package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FireResistanceAbyssAbility extends PassiveAbility {

    public FireResistanceAbyssAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }
    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(ModDamageTypes.FIRE)) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity caster)) return;
        if (caster == entity) return;
        PassiveAbility ability = PassiveAbilityHandler.getById("fire_resistance_abyss_ability");
        if (ability == null || !ability.shouldApplyTo(entity)) return;
        int difference = BeyonderData.getSequence(entity) - BeyonderData.getSequence(caster);
        float multiplier = Math.max(0.0F, 1.0F + difference * 0.25f);
        event.setNewDamage(event.getNewDamage() * multiplier);
    }
}
