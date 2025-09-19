package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class InvisibilityAbility extends AbilityItem {
    public static final HashSet<UUID> invisiblePlayers = new HashSet<>();

    public InvisibilityAbility(Properties properties) {
        super(properties, 180);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 13;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(0, 0, 0),
            2
    );

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
