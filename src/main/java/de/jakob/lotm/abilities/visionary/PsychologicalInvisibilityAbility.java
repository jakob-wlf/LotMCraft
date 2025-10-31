package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PsychologicalInvisibilityAbility extends AbilityItem {
    public static final HashSet<UUID> invisiblePlayers = new HashSet<>();

    public PsychologicalInvisibilityAbility(Properties properties) {
        super(properties, 180);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 13;
    }


    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!level.isClientSide) {

            // make invisible
            invisiblePlayers.add(entity.getUUID());
            entity.setInvisible(true);
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60, 20, false, false, false));

            //make visible again
            ServerScheduler.scheduleDelayed(20 * 60, () -> {
                invisiblePlayers.remove(entity.getUUID());
            }, (ServerLevel) level);
        }
    }

    @SubscribeEvent
    public static void onLivingTarget(LivingChangeTargetEvent event) {
        if(event.getNewAboutToBeSetTarget() != null && invisiblePlayers.contains(event.getNewAboutToBeSetTarget().getUUID())) {
            event.setCanceled(true);
        }
    }
}
