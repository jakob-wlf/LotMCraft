package de.jakob.lotm.network.packets.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.door.PlayerTeleportationAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gui.custom.CoordinateInput.CoordinateInputScreen;
import de.jakob.lotm.network.packets.toClient.*;
import de.jakob.lotm.rendering.*;
import de.jakob.lotm.rendering.effectRendering.impl.VFXRenderer;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Random;

import java.util.UUID;

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
                    false,
                    0.0f
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

    public static void changeToFirstPerson() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
    }

    public static void handleShaderPacket(SyncShaderPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if(entity == null) {
            return;
        }

        entity.getData(ModAttachments.SHADER_COMPONENT.get()).setShaderActive(packet.shaderActive());
        entity.getData(ModAttachments.SHADER_COMPONENT.get()).setShaderIndex(packet.shaderIndex());
    }

    public static void handleFogPacket(SyncFogPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if(entity == null) {
            return;
        }

        entity.getData(ModAttachments.FOG_COMPONENT.get()).setActive(packet.active());
        entity.getData(ModAttachments.FOG_COMPONENT.get()).setFogIndex(packet.index());
    }

    public static void addEffect(int index, double x, double y, double z) {
        VFXRenderer.addActiveEffect(index, x, y, z);
    }

    public static void handleHotbarPacket(SyncAbilityHotbarPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getData(ModAttachments.ABILITY_HOTBAR.get()).setCurrentHotbarIndex(packet.hotbarIndex());
        }
    }

    public static void addPlayerToList(int id, String playerName, UUID playerUUID) {
        PlayerTeleportationAbility.allPlayers.add(new PlayerTeleportationAbility.PlayerInfo(id, playerName, playerUUID));
    }

    public static void handleToggleAbilityPacket(ToggleAbilityPacket packet, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level == null) return;

        Entity entity = mc.level.getEntity(packet.entityId());

        // Try to find by UUID if hash code lookup fails
        if(entity == null) {
            for(Entity e : mc.level.entitiesForRendering()) {
                if(e.getId() == packet.entityId()) {
                    entity = e;
                    break;
                }
            }
        }

        if(!(entity instanceof LivingEntity livingEntity)) return;

        // Get the ability instance from registry
        ResourceLocation abilityLocation = ResourceLocation.parse(packet.abilityId());
        Item item = BuiltInRegistries.ITEM.get(abilityLocation);

        if(!(item instanceof ToggleAbilityItem ability)) return;

        // Update client-side state
        ability.handleClientSync(mc.level, livingEntity, packet.active());
    }

    public static void syncDecryptionAbility(SyncDecryptionLookedAtEntitiesAbilityPacket packet, Player player) {
        if(packet.active()) {
            DecryptionRenderLayer.activeDecryption.add(player.getUUID());
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = packet.entityId() == -1 ? null : level.getEntity(packet.entityId());
            LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            DecryptionOverlayRenderer.entitiesLookedAtByPlayerWithActiveDecryption.put(player.getUUID(), living);
        }
        else {
            DecryptionRenderLayer.activeDecryption.remove(player.getUUID());
            DecryptionOverlayRenderer.entitiesLookedAtByPlayerWithActiveDecryption.remove(player.getUUID());
        }
    }

    public static void handleSanityPacket(SyncSanityPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if(entity == null) {
            return;
        }
        entity.getData(ModAttachments.SANITY_COMPONENT.get()).setSanity(packet.sanity());
    }
}