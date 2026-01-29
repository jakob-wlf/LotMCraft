package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.DisplayShadowParticlesPacket;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ShadowConcealmentAbility extends Ability {
    public static final HashSet<UUID> invisiblePlayers = new HashSet<>();

    public ShadowConcealmentAbility(String id) {
        super(id, 45);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 9));
    }

    @Override
    public float getSpiritualityCost() {
        return 13;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(0, 0, 0),
            2
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!level.isClientSide) {

            // make invisible
            invisiblePlayers.add(entity.getUUID());
            entity.setInvisible(true);
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 20, 20, false, false, false));

            if(entity instanceof ServerPlayer player)
                PacketHandler.sendToPlayer(player, new DisplayShadowParticlesPacket(20 * 20));

            //make visible again
            ServerScheduler.scheduleDelayed(20 * 20, () -> {
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
