package de.jakob.lotm.network.packets.handlers;

import com.zigythebird.playeranimcore.math.Vec3f;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.door.PlayerTeleportationAbility;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.gui.custom.CoordinateInput.CoordinateInputScreen;
import de.jakob.lotm.gui.custom.Introspect.IntrospectScreen;
import de.jakob.lotm.gui.custom.Quest.QuestAcceptanceScreen;
import de.jakob.lotm.gui.custom.SelectionGui.PlayerSelectionGui;
import de.jakob.lotm.gui.custom.SelectionGui.ShapeShiftingSelectionGui;
import de.jakob.lotm.gui.custom.SelectionGui.StructureSelectionGui;
import de.jakob.lotm.network.packets.toClient.*;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestRegistry;
import de.jakob.lotm.rendering.*;
import de.jakob.lotm.rendering.effectRendering.impl.VFXRenderer;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Random;

import java.util.ArrayList;
import java.util.List;
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

    public static void reloadChunks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        Minecraft.getInstance().levelRenderer.setBlocksDirty(minX, minY, minZ, maxX, maxY, maxZ);
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

    public static void applyCameraShakeToPlayersInRadius(float intensity, int duration, ClientLevel level, Vec3 center, float radius) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.position().distanceTo(new Vec3(center.x(), center.y(), center.z())) <= radius && level == Minecraft.getInstance().level) {
            applyCameraShake(intensity, duration);
        }
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

    public static void changeToThirdPerson(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.player == entity)
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    public static void changeToFirstPerson(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if(minecraft.player == entity)
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
        entity.getData(ModAttachments.FOG_COMPONENT.get()).setColor(new Vec3f(packet.red(), packet.green(), packet.blue()));
    }

    public static void addEffect(int index, double x, double y, double z) {
        VFXRenderer.addActiveEffect(index, x, y, z);
    }

    public static void addDirectionalEffect(int index,
                                            double startX, double startY, double startZ,
                                            double endX, double endY, double endZ,
                                            int duration) {
        VFXRenderer.addActiveDirectionalEffect(index, startX, startY, startZ, endX, endY, endZ, duration);
    }

    public static void addMovableEffect(UUID effectId, int index,
                                        double x, double y, double z,
                                        int duration, boolean infinite) {
        VFXRenderer.addActiveMovableEffect(effectId, index, x, y, z, duration, infinite);
    }

    public static void updateMovableEffectPosition(UUID effectId, double x, double y, double z) {
        VFXRenderer.updateMovableEffectPosition(effectId, x, y, z);
    }

    public static void removeMovableEffect(UUID effectId) {
        VFXRenderer.removeMovableEffect(effectId);
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

    public static void handleAllyPacket(SyncAllyDataPacket packet) {
        if (Minecraft.getInstance().player != null) {
            AllyComponent newComponent = new AllyComponent(packet.allies());
            Minecraft.getInstance().player.setData(ModAttachments.ALLY_COMPONENT.get(), newComponent);
        }
    }

    public static void handleDarknessEffectPacket(boolean restore, List<BlockPos> blockPositions, int waveNumber) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        if (!restore) {
            // Turn blocks black client-side
            for (BlockPos pos : blockPositions) {
                level.setBlock(pos, ModBlocks.SOLID_VOID.get().defaultBlockState(),
                        Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            }
        } else {
            // Force client to re-sync these blocks from server
            for (BlockPos pos : blockPositions) {
                // Remove the fake block and request update from server
                level.setBlock(pos, level.getBlockState(pos), Block.UPDATE_ALL);
            }
        }
    }

    public static void handleHotGroundEffectPacket(boolean restore, List<BlockPos> blockPositions, int waveNumber) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        if (!restore) {
            // Turn blocks black client-side
            for (BlockPos pos : blockPositions) {
                level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(),
                        Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            }
        } else {
            // Force client to re-sync these blocks from server
            for (BlockPos pos : blockPositions) {
                // Remove the fake block and request update from server
                level.setBlock(pos, level.getBlockState(pos), Block.UPDATE_ALL);
            }
        }
    }

    public static void hideGUI() {
        Minecraft.getInstance().options.hideGui = true;
    }

    public static void showGui() {
        Minecraft.getInstance().options.hideGui = false;
    }

    public static void useAbility(UseAbilityPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if(!(entity instanceof LivingEntity living)) {
            return;
        }

        Ability ability = LOTMCraft.abilityHandler.getById(packet.abilityId());
        if(ability == null) {
            return;
        }

        ability.onAbilityUse(level, living);
    }

    public static void handleSyncAbilityWheelDataPacket(SyncAbilityWheelDataPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof IntrospectScreen screen) {
            screen.setAbilityWheelSlots(packet.abilityIds());
        }
    }

    public static void handleUpdateAbilityBarPacket(ArrayList<String> abilities) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof IntrospectScreen screen) {
            screen.setAbilityBarSlots(abilities);
        }
    }

    public static void handleFireEffectPacket(boolean restore, List<BlockPos> blockPositions, int waveNumber) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        if (!restore) {
            // Turn blocks black client-side
            for (BlockPos pos : blockPositions) {
                level.setBlock(pos, Blocks.FIRE.defaultBlockState(),
                        Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            }
        } else {
            // Force client to re-sync these blocks from server
            for (BlockPos pos : blockPositions) {
                // Remove the fake block and request update from server
                level.setBlock(pos, level.getBlockState(pos), Block.UPDATE_ALL);
            }
        }
    }

    public static void handleQuestScreenPacket(OpenQuestAcceptanceScreenPacket packet) {
        Quest quest = QuestRegistry.getQuest(packet.questId());
        if (quest == null) {
            return;
        }

        Component questName = quest.getName();
        Component questDescription = quest.getDescription();

        Minecraft.getInstance().setScreen(new QuestAcceptanceScreen(
                packet.questId(),
                questName,
                questDescription,
                packet.rewards(),
                packet.digestionReward(),
                packet.questSequence(),
                packet.npcId()
        ));
    }

    public static void handlePlayerDivinationScreenPacket(OpenPlayerDivinationScreenPacket packet) {
        Minecraft.getInstance().setScreen(new PlayerSelectionGui(packet.players()));
    }

    public static void handleStructureDivinationScreenPacket(OpenStructureDivinationScreenPacket packet) {
        Minecraft.getInstance().setScreen(new StructureSelectionGui(packet.structureIds()));
    }

    public static void handleShapeShiftingScreenPacket(OpenShapeShiftingScreenPacket payload) {
        Minecraft.getInstance().setScreen(new ShapeShiftingSelectionGui(payload.entityTypes()));
    }
}