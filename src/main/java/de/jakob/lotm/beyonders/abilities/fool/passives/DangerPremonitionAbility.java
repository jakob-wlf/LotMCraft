package de.jakob.lotm.beyonders.abilities.fool.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class DangerPremonitionAbility extends PassiveAbilityItem {
    public DangerPremonitionAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("fool", 8);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }

    static float[] dodgeChanceForSequence = new float[]{.4f, .35f, .325f, .275f, .25f, .2f, .175f, .15f, .125f, .1f};

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if(!((DangerPremonitionAbility) PassiveAbilityHandler.DANGER_PREMONITION.get()).shouldApplyTo(entity))
            return;

        Entity damager = event.getSource().getEntity();
        if(damager == null ||
                ((damager instanceof LivingEntity damagerLiving) &&
                        BeyonderData.getSequence(damagerLiving) - BeyonderData.getSequence(event.getEntity()) < -2
                ))
            return;

        int sequence = BeyonderData.getSequence(entity);
        if(sequence < 0 || sequence > 9) return;
        if(entity.level().random.nextFloat() <= dodgeChanceForSequence[sequence]) {
            event.setCanceled(true);
            entity.playSound(SoundEvents.ARMOR_STAND_BREAK, 1, 1);
            AbilityUtil.sendActionBar(entity, Component.translatable("lotm.dodged_attack").withColor(getColorForPathway("fool")));
        }

    }
}
