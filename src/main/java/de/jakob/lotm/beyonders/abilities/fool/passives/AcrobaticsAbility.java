package de.jakob.lotm.beyonders.abilities.fool.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AcrobaticsAbility extends PassiveAbility {

    public AcrobaticsAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "fool", 8
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if(!(event.getEntity() instanceof Player player) || !((AcrobaticsAbility) PassiveAbilityHandler.getById("acrobatics_ability")).shouldApplyTo(player))
            return;

        if(!player.isShiftKeyDown())
            return;

        player.setDeltaMovement(player.getDeltaMovement().scale(2));
        player.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if(!((AcrobaticsAbility) PassiveAbilityHandler.getById("acrobatics_ability")).shouldApplyTo(entity))
            return;

        if(!event.getSource().is(DamageTypes.FALL)) return;

        if(event.getAmount() <= 2) {
            event.setCanceled(true);
            return;
        }

        event.setAmount(event.getAmount() - 2);
    }
}