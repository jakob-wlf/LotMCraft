package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
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
        // Reduce LOOSING_CONTROL duration once per 1 second
        if (entity.tickCount % 20 != 0) return;
        MobEffectInstance loosingControl = entity.getEffect(ModEffects.LOOSING_CONTROL);
        if (loosingControl != null) {
            int remaining = loosingControl.getDuration();
            if (remaining <= 20) {
                entity.removeEffect(ModEffects.LOOSING_CONTROL);
            } else {
                entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, remaining - 20, loosingControl.getAmplifier(), loosingControl.isAmbient(), loosingControl.isVisible(), loosingControl.showIcon()
                ));
            }
        }
    }
    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(ModDamageTypes.LOOSING_CONTROL)) return;
        LivingEntity entity = event.getEntity();
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
            return;
        }
    }
}
