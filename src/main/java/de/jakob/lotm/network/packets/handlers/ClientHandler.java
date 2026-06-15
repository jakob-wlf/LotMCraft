package de.jakob.lotm.network.packets.handlers;

import com.zigythebird.playeranimcore.math.Vec3f;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.visionary.prophecy.VisionaryAbilityMenus;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.entity.custom.ability_entities.OriginalBodyEntity;
import de.jakob.lotm.gui.custom.AbilitySeal.AbilitySealScreen;
import de.jakob.lotm.gui.custom.AboveSeqAuthority.AboveSeqAuthorityScreen;
import de.jakob.lotm.gui.custom.CharSlotRoll.CharSlotRollScreen;
import de.jakob.lotm.gui.custom.CoordinateInput.CoordinateInputScreen;
import de.jakob.lotm.gui.custom.InternalUnderworld.InternalUnderworldAbilityScreen;
import de.jakob.lotm.gui.custom.Introspect.IntrospectScreen;
import de.jakob.lotm.gui.custom.Quest.QuestAcceptanceScreen;
import de.jakob.lotm.gui.custom.RiverVault.RiverVaultScreen;
import de.jakob.lotm.gui.custom.SelectionGui.*;
import de.jakob.lotm.network.packets.toClient.*;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestRegistry;
import de.jakob.lotm.rendering.*;
import de.jakob.lotm.rendering.effectRendering.impl.VFXRenderer;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.ClientSacrificeCache;
import de.jakob.lotm.util.ClientAccommodationCache;
import de.jakob.lotm.util.helper.AnimationUtil;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
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

import de.jakob.lotm.network.packets.toClient.SyncAnchorsPacket;
import de.jakob.lotm.util.data.ClientData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    public static void handleSyncAnchors(SyncAnchorsPacket packet) {
        ClientData.setAnchors(packet.anchors());
    }

    public static void openCoordinateScreen(Player player, String use) {
        Minecraft.getInstance().setScreen(new CoordinateInputScreen(player, use));
    }

    public static void handleOpenInternalUnderworldAbilityScreenPacket(boolean isRiverOwner) {
        // Swap generic chest UI with the custom Internal Underworld screen.
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) return;
        if (containerScreen instanceof InternalUnderworldAbilityScreen) return;
        if (!(containerScreen.getMenu() instanceof ChestMenu chestMenu)) return;

        String title = containerScreen.getTitle().getString();
        // Strip Minecraft formatting codes for comparison (§ followed by any char).
        String cleanTitle = title.replaceAll("§.", "");
        String selectSoulTitle = Component.translatable("ability.lotmcraft.internal_underworld.select_soul").getString();
        boolean isUnderworldTitle = title.startsWith("Internal Underworld - ")
                || title.equals(selectSoulTitle)
                || cleanTitle.equals("River Soul Vault");
        if (!isUnderworldTitle) return;

        mc.setScreen(new InternalUnderworldAbilityScreen(chestMenu, mc.player.getInventory(), containerScreen.getTitle(), isRiverOwner));
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

    public static void syncEyeOfDeathAbility(SyncEyeOfDeathAbilityPacket packet, Player player) {
        if (packet.active()) {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = packet.entityId() == -1 ? null : level.getEntity(packet.entityId());
            LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            EyeOfDeathOverlayRenderer.entitiesLookedAt.put(player.getUUID(), living);
        } else {
            EyeOfDeathOverlayRenderer.entitiesLookedAt.remove(player.getUUID());
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
        entity.getData(ModAttachments.TRANSFORMATION_COMPONENT.get()).setAdditionalData(packet.additionalData());
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

    public static void handleGreyFogStatus(boolean inside) {
        de.jakob.lotm.rendering.GreyFogOverlayRenderer.insideGreyFog = inside;
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

    public static void addEffect(int index, double x, double y, double z, int entityId) {
        if (entityId == AddEffectPacket.NO_ENTITY) {
            VFXRenderer.addActiveEffect(index, x, y, z);
        } else {
            VFXRenderer.addActiveEffect(index, x, y, z, entityId);
        }
    }

    public static void addDirectionalEffect(int index,
                                            double startX, double startY, double startZ,
                                            double endX, double endY, double endZ,
                                            int duration, int entityId) {
        if (entityId == AddDirectionalEffectPacket.NO_ENTITY) {
            VFXRenderer.addActiveDirectionalEffect(index, startX, startY, startZ, endX, endY, endZ, duration);
        } else {
            VFXRenderer.addActiveDirectionalEffect(index, startX, startY, startZ, endX, endY, endZ, duration, entityId);
        }
    }

    public static void addMovableEffect(UUID effectId, int index,
                                        double x, double y, double z,
                                        int duration, boolean infinite,
                                        int entityId) {
        if (entityId == AddMovableEffectPacket.NO_ENTITY) {
            VFXRenderer.addActiveMovableEffect(effectId, index, x, y, z, duration, infinite);
        } else {
            VFXRenderer.addActiveMovableEffect(effectId, index, x, y, z, duration, infinite, entityId);
        }
    }

    public static void updateMovableEffectPosition(UUID effectId, double x, double y, double z) {
        VFXRenderer.updateMovableEffectPosition(effectId, x, y, z);
    }

    public static void removeMovableEffect(UUID effectId) {
        VFXRenderer.removeMovableEffect(effectId);
    }

    public static void cancelEffectsNear(double x, double y, double z, double radius) {
        VFXRenderer.cancelEffectsNear(x, y, z, radius);
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

    public static void handleCorruptionPacket(SyncCorruptionPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if(entity == null) {
            return;
        }
        entity.getData(ModAttachments.CORRUPTION_COMPONENT.get()).setCorruption(packet.corruption());
    }

    public static void handleSkillScalingPacket(SyncSkillScalingPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if(entity == null) {
            return;
        }
        var component = entity.getData(ModAttachments.SKILL_SCALING_COMPONENT.get());

        component.setScalingToSkill(packet.scaleToSkill())
                .setPath(packet.path())
                .setSeq(packet.seq());
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

    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    public static void handlePlayerDivinationScreenPacket(OpenPlayerDivinationScreenPacket packet) {
        Minecraft.getInstance().setScreen(new PlayerSelectionGui(packet.players(), packet.types()));
    }

    public static void openPsychologicalCueExplanation() {
        Minecraft.getInstance().setScreen(VisionaryAbilityMenus.createPsychologicalCueAbilityMenu(Minecraft.getInstance().screen));
    }

    public static void openStoryWritingExplanation() {
        Minecraft.getInstance().setScreen(VisionaryAbilityMenus.createStoryWritingAbilityMenu(Minecraft.getInstance().screen));
    }

    public static void handleStructureDivinationScreenPacket(OpenStructureDivinationScreenPacket packet) {
        Minecraft.getInstance().setScreen(new StructureSelectionGui(packet.structureIds()));
    }

    public static void handleBiomeDivinationScreenPacket(OpenBiomeDivinationScreenPacket packet) {
        Minecraft.getInstance().setScreen(new BiomeSelectionGui(packet.biomeIds()));
    }

    public static void handleShapeShiftingScreenPacket(OpenShapeShiftingScreenPacket packet) {
        Minecraft.getInstance().setScreen(new ShapeShiftingSelectionGui(packet.entityTypes()));
    }

    public static void handleDiscernmentScreenPacket(OpenDiscernmentScreenPacket packet) {
        Minecraft.getInstance().setScreen(new DiscernmentSelectionGui(packet.saved()));
    }

    public static void handleHistoricalVoidBorrowingScreenPacket(OpenHistoricalVoidBorrowingScreenPacket packet) {
        Minecraft.getInstance().setScreen(new HistoricalVoidBorrowingSelectionGui(packet.options()));
    }

    public static void handleOriginalBodyOwnerSyncPacket(SyncOriginalBodyOwnerPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getEntity(packet.entityId()) instanceof OriginalBodyEntity body) {
            body.getData(ModAttachments.CONTROLLING_DATA).setOwnerUUID(packet.ownerUUID());
            body.getData(ModAttachments.CONTROLLING_DATA).setOwnerName(packet.ownerName());
        }
    }

    public static void handleDisableAbilityUsageForTimePacket(DisableAbilityUsageForTimePacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        DisabledAbilitiesComponent disabledComponent = living.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        disabledComponent.disableAbilityUsageForTime(packet.cause(), packet.ticks(), living);
    }

    public static void handleSyncToggleAbility(SyncToggleAbilityPacket packet, IPayloadContext context) {
        Entity entity = context.player().level().getEntity(packet.entityId());
        Ability ability = LOTMCraft.abilityHandler.getById(packet.abilityId());
        if(!(ability instanceof ToggleAbility toggleAbility) || !(entity instanceof LivingEntity living)) {
            return;
        }

        if(entity != Minecraft.getInstance().player) return;

        switch (packet.action()) {
            case 0 -> {
                ActiveToggleAbilitiesRenderer.activeToggleAbilities.add(packet.abilityId());
                toggleAbility.start(living.level(), living);
                toggleAbility.updateClientCache(living, true);
            }
            case 1 -> toggleAbility.prepareTick(living.level(), living);
            case 2 -> {
                ActiveToggleAbilitiesRenderer.activeToggleAbilities.remove(packet.abilityId());
                toggleAbility.stop(living.level(), living);
                toggleAbility.updateClientCache(living, true);
            }

        }
    }

    public static void handleAddClientSideTagPacket(AddClientSideTagPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if (entity != null) {
            entity.getTags().add(packet.tag());
        }
    }

    public static void handleSyncWeaknessDetectionPacket(SyncWeaknessDetectionTargetsAbilityPacket packet) {
        WeaknessDetectionRenderLayer.activeWeaknessDetection.clear();
        if (packet.active()) {
            WeaknessDetectionRenderLayer.activeWeaknessDetection.putAll(packet.targets());
        }
    }

    public static void handleControllingDataPacket(SyncControllingDataPacket packet) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if(entity == null) {
            return;
        }
        entity.getData(ModAttachments.CONTROLLING_DATA.get()).setControlling(packet.isControlling());
        entity.getData(ModAttachments.CONTROLLING_DATA.get()).setBodyEntity(packet.bodyEntity());

        if (Minecraft.getInstance().screen instanceof IntrospectScreen screen) {
            screen.refreshAvailableAbilities();
        }
    }

    public static void handleDiscernmentDataPacket(SyncDiscernmentDataPacket packet) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if(entity == null) {
            return;
        }
        entity.getData(ModAttachments.DISCERNMENT_DATA.get()).setDiscerning(packet.isDiscerning());
    }

    public static void syncKillCount(int killCount) {
        ClientSacrificeCache.setKillCount(killCount);
        if (Minecraft.getInstance().screen instanceof IntrospectScreen screen) {
            screen.updateKillCount(killCount);
        }
    }

    public static void syncSacrificeDuration(int totalTicks) {
        ClientSacrificeCache.setTotalTicks(totalTicks);
        ClientSacrificeCache.setRemainingTicks(totalTicks);
    }

    public static void syncSefirotAccommodation(int progressTicks, int totalTicks) {
        if (totalTicks <= 0 || progressTicks <= 0) {
            ClientAccommodationCache.reset();
            return;
        }

        ClientAccommodationCache.setProgress(progressTicks, totalTicks);
    }

    public static void syncCullAbility(boolean active, UUID playerUUID) {
        if (active) {
            CullOverlay.playersWithCullActivated.add(playerUUID);
        } else {
            CullOverlay.playersWithCullActivated.remove(playerUUID);
        }
    }

    public static void handleSpiritChannelingPacket(de.jakob.lotm.network.packets.toClient.SyncSpiritChannelingPacket packet) {
        de.jakob.lotm.util.ClientSpiritCache.setSpiritTypeOrdinal(packet.spiritType());
    }

    public static void handleSyncIntrospectMenuPacket(SyncIntrospectMenuPacket packet, UUID playerUUID) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof IntrospectScreen screen) {
            screen.updateMenuData(packet.sequence(), packet.pathway(), ClientBeyonderCache.getDigestionProgress(playerUUID), packet.sanity(), packet.corruption());
            screen.refreshAvailableAbilities();
        }
    }

    public static void handleSyncBeyonderData(SyncBeyonderDataPacket packet, IPayloadContext context) {
        Player player = context.player();
        ClientBeyonderCache.updateData(
                player.getUUID(),
                packet.pathway(),
                packet.sequence(),
                packet.spirituality(),
                packet.griefingEnabled(),
                true,
                packet.digestionProgress(),
                packet.pathwayHistory(),
                packet.charList()
        );

        // Update received blessings
        de.jakob.lotm.attachments.ReceivedBlessingComponent receivedBlessings = player.getData(de.jakob.lotm.attachments.ModAttachments.RECEIVED_BLESSING_COMPONENT);
        receivedBlessings.getBlessings().clear();
        receivedBlessings.getBlessings().addAll(packet.blessings());

        if (Minecraft.getInstance().screen instanceof IntrospectScreen screen) {
            screen.refreshAvailableAbilities();
        }
    }

    public static void handleSyncLivingEntityBeyonderData(SyncLivingEntityBeyonderDataPacket packet, IPayloadContext context) {
        if (!context.flow().isClientbound()) return;
        syncLivingEntityBeyonderData(packet);

        if (Minecraft.getInstance().screen instanceof IntrospectScreen screen) {
            screen.refreshAvailableAbilities();
        }
    }

    public static void handleSyncIntrospectMenu(SyncIntrospectMenuPacket packet, IPayloadContext context) {
        handleSyncIntrospectMenuPacket(packet, context.player().getUUID());
    }

    public static void handleApotheosisPacket(SyncApotheosisPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living) {
            living.getData(ModAttachments.APOTHEOSIS_COMPONENT).setApotheosisTicksLeft(packet.ticks());
            living.getData(ModAttachments.APOTHEOSIS_COMPONENT).setPathway(packet.pathway());
        }
    }

    public static void playAnimation(PlayAnimationPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(packet.playerId());
        if(!(entity instanceof AbstractClientPlayer player)) {
            return;
        }

        AnimationUtil.playAnimation(player, AnimationUtil.getResourceLocationById(packet.animId()));
    }

    public static void handleAbilitySelectionPacket(SyncAbilitySelectionPacket packet) {
        if (!(LOTMCraft.abilityHandler.getById(packet.abilityId()) instanceof SelectableAbility selectable)) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        selectable.setSelectedAbilityClient(player.getUUID(), packet.selectedIndex());
    }

    public static void openCharSlotRollScreen(OpenCharSlotRollPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        // If a CharSlotRollScreen is already open (server responding to a reroll), update
        // it in-place instead of creating a new one — this preserves Konami bonus rerolls
        // and the konamiUsed counter which would otherwise reset to 0 on the new screen.
        if (mc.screen instanceof CharSlotRollScreen existing) {
            existing.serverAcknowledgedReroll(packet.rerollsLeft());
        } else {
            mc.setScreen(new CharSlotRollScreen(packet.pathways(), packet.charNames(), packet.rerollsLeft()));
        }
    }

    public static void openAbilitySealScreen(OpenAbilitySealScreenPacket packet) {
        Minecraft.getInstance().setScreen(
                new AbilitySealScreen(
                        packet.targetUUIDStr(),
                        packet.targetName(),
                        packet.abilityIds(),
                        packet.abilityNames(),
                        packet.currentlySealed()));
    }

    public static void openAboveSeqAuthorityScreen() {
        Minecraft.getInstance().setScreen(new AboveSeqAuthorityScreen());
    }

    public static void openDailySpinScreen(de.jakob.lotm.network.packets.toClient.OpenDailySpinScreenPacket packet) {
        Minecraft.getInstance().setScreen(
                new de.jakob.lotm.gui.custom.DailySpin.DailySpinScreen(
                        packet.reelNames(), packet.landingIndex(), packet.canSpin()));
    }

    public static void openSellYourSoulScreen(de.jakob.lotm.network.packets.toClient.OpenSellYourSoulScreenPacket packet) {
        Minecraft.getInstance().setScreen(
                new de.jakob.lotm.gui.custom.SellYourSoul.SellYourSoulScreen(
                        packet.outcome(), packet.rewardName()));
    }

    public static void openSellYourSoulGateScreen(de.jakob.lotm.network.packets.toClient.OpenSellYourSoulGatePacket packet) {
        Minecraft.getInstance().setScreen(
                new de.jakob.lotm.gui.custom.SellYourSoul.SellYourSoulGateScreen(packet.cooldownEndMillis()));
    }

    public static void openCharExchangeWheelScreen(de.jakob.lotm.network.packets.toClient.OpenCharExchangeWheelPacket packet) {
        Minecraft.getInstance().setScreen(
                new de.jakob.lotm.gui.custom.CharExchange.CharExchangeWheelScreen(
                        packet.reelNames(), packet.landingIndex(), packet.outcome(), packet.rewardName(), packet.title()));
    }

    public static void handleOpenRiverVaultScreen(de.jakob.lotm.network.packets.toClient.OpenRiverVaultScreenPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new de.jakob.lotm.gui.custom.RiverVault.RiverVaultScreen(
                    packet.vaultItems(), packet.iuItems(),
                    packet.maxIU(), packet.vaultCapacity()));
        }
    }

    public static void openCharacteristicSplittingScreen() {
        Minecraft.getInstance().setScreen(new de.jakob.lotm.gui.custom.CharacteristicSplittingScreen());
    }

    public static void openSlateHalfPathwayScreen(de.jakob.lotm.item.custom.BlasphemySlateHalfItem.HalfType halfType) {
        Minecraft.getInstance().setScreen(new de.jakob.lotm.gui.custom.BlasphemySlate.SlateHalfPathwayScreen(halfType));
    }
}