package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;

public class MaliceAuthorityAbility extends PassiveAbility {
    public MaliceAuthorityAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 2));}
    @Override
    public void tick(Level level, LivingEntity entity) {
    }
    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(ModDamageTypes.LOOSING_CONTROL)) return;
        LivingEntity entity = event.getEntity();
        PassiveAbility ability = PassiveAbilityHandler.getById("malice_authority_ability");
        assert ability != null;
        if (!ability.shouldApplyTo(entity)) return;
        int entitySeq = BeyonderData.getSequence(entity);
        if (entitySeq >= 3) {
            event.setNewDamage(0);
            return;
        }
        if (entitySeq == 2) {
            event.setNewDamage(event.getNewDamage() * 0.50F);
            return;
        }
        if (entitySeq == 1) {
            event.setNewDamage(event.getNewDamage() * 0.75F);
        }
    }
    public static boolean isActiveFor(LivingEntity entity) {
        PassiveAbility ability = PassiveAbilityHandler.getById("malice_authority_ability");
        return ability != null && ability.shouldApplyTo(entity);
    }
}
