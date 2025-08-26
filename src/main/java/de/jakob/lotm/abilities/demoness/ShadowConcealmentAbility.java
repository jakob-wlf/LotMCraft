package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ClientScheduler;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ShadowConcealmentAbility extends AbilityItem {
    private static final HashSet<UUID> invisiblePlayers = new HashSet<>();

    public ShadowConcealmentAbility(Properties properties) {
        super(properties, 45);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 9));
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
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 20, 20, false, false, false));

            //make visible again
            ServerScheduler.scheduleDelayed(20 * 20, () -> {
                invisiblePlayers.remove(entity.getUUID());
            }, (ServerLevel) level);
        }
        else {
            ClientScheduler.scheduleForDuration(0, 2, 20 * 20, () -> {
                ParticleUtil.spawnParticles((ClientLevel) level, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 3, .4, 1.2, .4, 0);
            }, (ClientLevel) level);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (invisiblePlayers.contains(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && invisiblePlayers.contains(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingTarget(LivingChangeTargetEvent event) {
        if(event.getNewAboutToBeSetTarget() != null && invisiblePlayers.contains(event.getNewAboutToBeSetTarget().getUUID())) {
            event.setCanceled(true);
        }
    }
}
