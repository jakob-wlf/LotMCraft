package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ApotheosisComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ApotheosisTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        ApotheosisComponent component = event.getEntity().getData(ModAttachments.APOTHEOSIS_COMPONENT);
        if(component.getApotheosisTicksLeft() <= 0) return;

        Player player = event.getEntity();

        if(player.level().isClientSide) {
            ClientHandler.applyCameraShakeToPlayersInRadius(4, 20, (ClientLevel) player.level(), player.position(), 1064);
            return;
        }

        player.setDeltaMovement(new Vec3(0, 0.02, 0));
        player.hurtMarked = true;

        player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).disableAbilityUsageForTime("apotheosis", 20, player);

        int colorInt = BeyonderData.pathwayInfos.get(component.getPathway()).color();
        float red   = ((colorInt >> 16) & 0xFF) / 255.0f;
        float green = ((colorInt >>  8) & 0xFF) / 255.0f;
        float blue  = ( colorInt        & 0xFF) / 255.0f;

        DustParticleOptions dustParticle = new DustParticleOptions(new Vector3f(red, green, blue), 2.5f);

        Vec3 currentCenter = player.position().add(0, player.getBbHeight() / 2, 0);
        ParticleUtil.spawnSphereParticles((ServerLevel) player.level(), dustParticle, currentCenter, 1.5, 60);

        if(component.getApotheosisTicksLeft() % (120) == 0) {
            MovableEffectManager.playEffect(MovableEffectManager.MovableEffect.BEAMS_OF_LIGHT, new EntityLocation(player), 120, false, (ServerLevel) player.level(), player);
        }

        component.setApotheosisTicksLeftAndSync(component.getApotheosisTicksLeft() - 1, (ServerLevel) player.level(), player);
        if(component.getApotheosisTicksLeft() == 0) {
            if(BeyonderData.getSequence(player) == 1)
                BeyonderData.setBeyonder(player, BeyonderData.getPathway(player), 0);
            else
                BeyonderData.addCharacteristic(player,0,component.getPathway());

            UniquenessComponent comp = player.getData(ModAttachments.UNIQUENESS_COMPONENT);

            // Reset uniqueness component
            comp.setHasUniqueness(false);
            comp.setUniquenessPathway("");
            comp.resetKillCount();
            BeyonderData.playerMap.setUniqueness(player, "none");

            // Sync to client
            PacketHandler.syncUniquenessToPlayer((ServerPlayer) player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        if(player.level().isClientSide) return;
        player.getData(ModAttachments.APOTHEOSIS_COMPONENT).setApotheosisTicksLeftAndSync(0, (ServerLevel) player.level(), player);
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if(player.level().isClientSide) return;
        player.getData(ModAttachments.APOTHEOSIS_COMPONENT).setApotheosisTicksLeftAndSync(0, (ServerLevel) player.level(), player);
    }

}
