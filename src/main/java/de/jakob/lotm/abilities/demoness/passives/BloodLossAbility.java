package de.jakob.lotm.abilities.demoness.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BloodLossAbility extends PassiveAbilityItem {
    public BloodLossAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 7));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Post event) {
        if(!(event.getSource().getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if(!((BloodLossAbility) PassiveAbilityHandler.BLOOD_LOSS.get()).shouldApplyTo(entity)) {
            return;
        }

        if((new Random()).nextDouble() < .4) {
            ItemStack blood = new ItemStack(ModItems.BLOOD.get());
            blood.set(ModDataComponents.BLOOD_OWNER, event.getEntity().getUUID().toString());
            event.getEntity().spawnAtLocation(blood);
        }
    }
}
