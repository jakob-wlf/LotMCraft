package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.darkness.NightmareAbility;
import de.jakob.lotm.util.data.ClientCorrosionFovCache;
import de.jakob.lotm.beyonders.artifacts.SealedArtifactData;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.InventoryOpenedPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import de.jakob.lotm.util.data.PathwayInfos;
import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        LOTMCraft.pathwayInfosKey = new KeyMapping("key.beyonders.pathway_infos", GLFW.GLFW_KEY_J, "key.categories.beyonders");
        LOTMCraft.toggleGriefingKey = new KeyMapping("key.beyonders.toggle_griefing", GLFW.GLFW_KEY_K, "key.categories.beyonders");
        LOTMCraft.nextAbilityKey = new KeyMapping("key.beyonders.next_ability", GLFW.GLFW_KEY_V, "key.categories.beyonders");
        LOTMCraft.previousAbilityKey = new KeyMapping("key.beyonders.previous_ability", GLFW.GLFW_KEY_X, "key.categories.beyonders");
        LOTMCraft.enterSefirotKey = new KeyMapping("key.beyonders.enter_sefirot", GLFW.GLFW_KEY_U, "key.categories.beyonders");
        LOTMCraft.openWheelToggleKey = new KeyMapping("key.beyonders.open_wheel_toggle", GLFW.GLFW_KEY_LEFT_ALT, "key.categories.beyonders");
        LOTMCraft.openWheelHoldKey = new KeyMapping("key.beyonders.open_wheel_hold", GLFW.GLFW_KEY_Y, "key.categories.beyonders");
        LOTMCraft.useSelectedAbilityKey = new KeyMapping("key.beyonders.use_ability", GLFW.GLFW_KEY_M, "key.categories.beyonders");
        LOTMCraft.toggleAbilityHotbarKey = new KeyMapping("key.beyonders.toggle_ability_hotbar", GLFW.GLFW_KEY_R, "key.categories.beyonders");
        // Slots 1-9 default to the number keys, slots 10-18 to Ctrl + number keys
        LOTMCraft.useAbilityBarKeys = new KeyMapping[AbilityBarHelper.MAX_SLOTS];
        for (int i = 0; i < LOTMCraft.useAbilityBarKeys.length; i++) {
            int key = GLFW.GLFW_KEY_1 + (i % 9);
            KeyModifier modifier = i < 9 ? KeyModifier.NONE : KeyModifier.CONTROL;
            LOTMCraft.useAbilityBarKeys[i] = new KeyMapping("key.beyonders.use_ability_bar_ability_" + (i + 1),
                    KeyConflictContext.IN_GAME, modifier, InputConstants.Type.KEYSYM, key, "key.categories.beyonders");
        }
        LOTMCraft.nextArtifactAbilityKey = new KeyMapping("key.beyonders.next_artifact_ability", GLFW.GLFW_KEY_P, "key.categories.beyonders");
        LOTMCraft.returnToMainBody = new KeyMapping("key.beyonders.return_to_main_body", GLFW.GLFW_KEY_P, "key.categories.beyonders");
        LOTMCraft.openArtifactWheel = new KeyMapping("key.beyonders.openArtifactWheel", GLFW.GLFW_KEY_L, "key.categories.beyonders");
        LOTMCraft.openSharedAbilityWheelKey = new KeyMapping("key.beyonders.open_shared_ability_wheel", GLFW.GLFW_KEY_H, "key.categories.beyonders");
        LOTMCraft.useSharedAbilityKey = new KeyMapping("key.beyonders.use_shared_ability", GLFW.GLFW_KEY_N, "key.categories.beyonders");
        LOTMCraft.nextSharedAbilityKey = new KeyMapping("key.beyonders.next_shared_ability", GLFW.GLFW_KEY_PERIOD, "key.categories.beyonders");
        LOTMCraft.previousSharedAbilityKey = new KeyMapping("key.beyonders.previous_shared_ability", GLFW.GLFW_KEY_COMMA, "key.categories.beyonders");
        LOTMCraft.openMarionetteMenuKey = new KeyMapping("key.beyonders.open_marionette_menu", GLFW.GLFW_KEY_B, "key.categories.beyonders");
        LOTMCraft.nextMarionetteKey = new KeyMapping("key.beyonders.next_marionette", GLFW.GLFW_KEY_X, "key.categories.beyonders");


        event.register(LOTMCraft.pathwayInfosKey);
        event.register(LOTMCraft.toggleGriefingKey);
        event.register(LOTMCraft.nextAbilityKey);
        event.register(LOTMCraft.previousAbilityKey);
        event.register(LOTMCraft.enterSefirotKey);
        event.register(LOTMCraft.openWheelToggleKey);
        event.register(LOTMCraft.openWheelHoldKey);
        event.register(LOTMCraft.useSelectedAbilityKey);
        event.register(LOTMCraft.toggleAbilityHotbarKey);
        for (KeyMapping keyMapping : LOTMCraft.useAbilityBarKeys) {
            event.register(keyMapping);
        }
        event.register(LOTMCraft.returnToMainBody);
        event.register(LOTMCraft.openArtifactWheel);
        event.register(LOTMCraft.nextArtifactAbilityKey);
        event.register(LOTMCraft.openSharedAbilityWheelKey);
        event.register(LOTMCraft.useSharedAbilityKey);
        event.register(LOTMCraft.nextSharedAbilityKey);
        event.register(LOTMCraft.previousSharedAbilityKey);
        event.register(LOTMCraft.openMarionetteMenuKey);
        event.register(LOTMCraft.nextMarionetteKey);
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) return;

        var font = screen.getMinecraft().font;
        var widget = new StringWidget(
                0, 4, screen.width, 10,
                Component.translatable("lotmcraft.epilepsy_warning").withStyle(ChatFormatting.RED),
                font
        );
        event.addListener(widget);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        registerSealedArtifactTint(ModItems.SEALED_ARTIFACT.get(), event);
        registerSealedArtifactTint(ModItems.SEALED_ARTIFACT_BELL.get(), event);
        registerSealedArtifactTint(ModItems.SEALED_ARTIFACT_STAR.get(), event);
        registerSealedArtifactTint(ModItems.SEALED_ARTIFACT_GEM.get(), event);
        registerSealedArtifactTint(ModItems.SEALED_ARTIFACT_CHAIN.get(), event);
    }

    private static void registerSealedArtifactTint(Item item, RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> {
                    if (tintIndex == 1) {
                        // Read data from the stack however you like
                        SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
                        if (data != null) {
                            String pathway = data.pathway();
                            return BeyonderData.pathwayInfos.get(pathway).color();
                        }
                        // return random color
                        int randomColor = BeyonderData.pathwayInfos.values().stream()
                                .map(PathwayInfos::color)
                                .findAny()
                                .orElse(0xFFFFFF);
                        return randomColor;
                    }
                    return -1; // -1 = no tint for this layer
                },
                item
        );
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (NightmareAbility.hasActiveNightmare(mc.player) || NightmareAbility.isAffectedByNightmare(mc.player))) {
            int color = 0xFFff2b4f; // ARGB (0xAARRGGBB)

            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            event.setRed(r);
            event.setGreen(g);
            event.setBlue(b);
        }
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (!(event.getScreen() instanceof InventoryScreen)) return;

        PacketHandler.sendToServer(new InventoryOpenedPacket());
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float multiplier = ClientCorrosionFovCache.getFovMultiplier();
        if (multiplier != 1.0f) {
            event.setFOV(event.getFOV() * multiplier);
        }
    }
}