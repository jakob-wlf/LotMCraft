package de.jakob.lotm.beyonders.abilities.abyss.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MentalResistanceAbility extends PassiveAbility{
    public MentalResistanceAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 7));}
    @Override
    public void tick(Level level, LivingEntity entity) {
        // Reduce LOOSING_CONTROL duration once per 2 second
        int EntitySeq = BeyonderData.getSequence(entity);
        if (EntitySeq < 2)return;
        if (entity.tickCount % 40 != 0)
            return;
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
    public static void onEntityDamage(LivingDamageEvent.Pre event) {// 30% resistance
        if (!event.getSource().is(ModDamageTypes.LOOSING_CONTROL)) return;
        LivingEntity entity = event.getEntity();
        int EntitySeq = BeyonderData.getSequence(entity);
        if (EntitySeq < 2)return;
        float damage = event.getNewDamage();
        event.setNewDamage(damage * 0.7F);
    }
}