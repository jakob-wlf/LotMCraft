package de.jakob.lotm.abilities.error.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SendPassiveTheftEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PassiveTheftAbility extends PassiveAbilityItem {
    public PassiveTheftAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 9));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }

    @SubscribeEvent
    public static void onDamageEntity(LivingDamageEvent.Post event) {
        if(!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }


        PassiveTheftAbility ability = (PassiveTheftAbility) PassiveAbilityHandler.PASSIVE_THEFT.get();

        if(!(event.getSource().getEntity() instanceof ServerPlayer player) || !ability.shouldApplyTo(player)) {
            return;
        }

        if(event.getEntity().distanceToSqr(player) > 16) {
            return;
        }

        if((new Random()).nextDouble() > .4) {
            return;
        }

        LivingEntity target = event.getEntity();
        TheftHandler.stealItemsFromEntity(target, player);

        PacketHandler.sendToPlayer(player, new SendPassiveTheftEffectPacket(target.getEyePosition().x, target.getEyePosition().y, target.getEyePosition().z));
    }
}
