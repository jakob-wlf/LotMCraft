package de.jakob.lotm.network.packets.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gui.custom.CoordinateInputScreen;
import de.jakob.lotm.network.packets.*;
import de.jakob.lotm.rendering.MarionetteOverlayRenderer;
import de.jakob.lotm.rendering.SpectatingOverlayRenderer;
import de.jakob.lotm.rendering.SpiritVisionOverlayRenderer;
import de.jakob.lotm.rendering.TelepathyOverlayRenderer;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Random;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    public static void openCoordinateScreen(Player player, String use) {
        Minecraft.getInstance().setScreen(new CoordinateInputScreen(player, use));
    }

    public static void syncLivingEntityBeyonderData(SyncLivingEntityBeyonderDataPacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living) {
            ClientBeyonderCache.updateData(
                    living.getUUID(),
                    packet.pathway(),
                    packet.sequence(),
                    packet.spirituality(),
                    false,
                    false
            );
        }
    }

    public static void syncSpectatingAbility(SyncSpectatingAbilityPacket packet, Player player) {
        if(packet.active()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = packet.entityId() == -1 ? null : level.getEntity(packet.entityId());
            LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            SpectatingOverlayRenderer.entitiesLookedAtByPlayerWithActiveSpectating.put(player.getUUID(), living);
        }
        else {
            SpectatingOverlayRenderer.entitiesLookedAtByPlayerWithActiveSpectating.remove(player.getUUID());
        }
    }

    public static void syncSpiritVisionAbility(SyncSpiritVisionAbilityPacket packet, Player player) {
        if(packet.active()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = packet.entityId() == -1 ? null : level.getEntity(packet.entityId());
            LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            SpiritVisionOverlayRenderer.entitiesLookedAt.put(player.getUUID(), living);
        }
        else {
            SpiritVisionOverlayRenderer.entitiesLookedAt.remove(player.getUUID());
        }
    }

    public static void handleRingPacket(RingEffectPacket packet) {
        RingExpansionRenderer.handleRingEffectPacket(packet);
    }

    public static void removeDreamDivinationUser(Player player) {
        DivinationAbility.dreamDivinationUsers.remove(player.getUUID());
    }

    public static void syncTelepathyAbility(SyncTelepathyAbilityPacket packet, Player player) {
        if(packet.active()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            TelepathyOverlayRenderer.entitiesLookedAtByPlayerWithActiveTelepathy.put(player.getUUID(), packet.goalNames());
        }
        else {
            TelepathyOverlayRenderer.entitiesLookedAtByPlayerWithActiveTelepathy.remove(player.getUUID());
        }
    }

    private static float shakeIntensity = 0;
    private static int shakeDuration = 0;
    private static final Random random = new Random();

    public static void applyCameraShake(float intensity, int duration) {
        shakeIntensity = intensity;
        shakeDuration = duration;
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shakeDuration > 0) {
            float currentIntensity = shakeIntensity * (shakeDuration / 20f); // Fade out

            float shakeX = (random.nextFloat() - 0.5f) * currentIntensity;
            float shakeY = (random.nextFloat() - 0.5f) * currentIntensity;
            float shakeZ = (random.nextFloat() - 0.5f) * currentIntensity * 0.5f; // Less roll

            event.setPitch((float) (event.getPitch() + shakeX));
            event.setYaw((float) (event.getYaw() + shakeY));
            event.setRoll((float) (event.getRoll() + shakeZ));

            shakeDuration--;
        }
    }

    public static void syncSelectedMarionette(SyncSelectedMarionettePacket packet, Player player) {
        if(packet.active()) {
            MarionetteOverlayRenderer.currentMarionette.put(
                    player.getUUID(),
                    new MarionetteOverlayRenderer.MarionetteInfos(
                            packet.name(),
                            packet.health(),
                            packet.maxHealth()
                    )
            );
        }
        else {
            MarionetteOverlayRenderer.currentMarionette.remove(player.getUUID());
        }
    }

    public static void handleMirrorWorldPacket(SyncMirrorWorldPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getData(ModAttachments.MIRROR_WORLD_COMPONENT.get())
                    .setInMirrorWorld(packet.inMirrorWorld());
        }
    }

    public static void handleTransformationPacket(SyncTransformationPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if(entity == null) {
            return;
        }

        entity.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).setTransformed(packet.isTransformed());
        entity.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).setTransformationIndex(packet.transformationIndex());
    }

    public static void changeToThirdPerson() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }
}