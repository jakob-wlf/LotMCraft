package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.darkness.NightmareAbility;
import de.jakob.lotm.artifacts.SealedArtifactData;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.InventoryOpenedPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
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
        LOTMCraft.useAbilityBarAbility1 = new KeyMapping("key.beyonders.use_ability_bar_ability_1", GLFW.GLFW_KEY_1, "key.categories.beyonders");
        LOTMCraft.useAbilityBarAbility2 = new KeyMapping("key.beyonders.use_ability_bar_ability_2", GLFW.GLFW_KEY_2, "key.categories.beyonders");
        LOTMCraft.useAbilityBarAbility3 = new KeyMapping("key.beyonders.use_ability_bar_ability_3", GLFW.GLFW_KEY_3, "key.categories.beyonders");
        LOTMCraft.useAbilityBarAbility4 = new KeyMapping("key.beyonders.use_ability_bar_ability_4", GLFW.GLFW_KEY_4, "key.categories.beyonders");
        LOTMCraft.useAbilityBarAbility5 = new KeyMapping("key.beyonders.use_ability_bar_ability_5", GLFW.GLFW_KEY_5, "key.categories.beyonders");
        LOTMCraft.useAbilityBarAbility6 = new KeyMapping("key.beyonders.use_ability_bar_ability_6", GLFW.GLFW_KEY_6, "key.categories.beyonders");
        LOTMCraft.returnToMainBody = new KeyMapping("key.beyonders.return_to_main_body", GLFW.GLFW_KEY_P, "key.categories.beyonders");


        event.register(LOTMCraft.pathwayInfosKey);
        event.register(LOTMCraft.toggleGriefingKey);
        event.register(LOTMCraft.nextAbilityKey);
        event.register(LOTMCraft.previousAbilityKey);
        event.register(LOTMCraft.enterSefirotKey);
        event.register(LOTMCraft.openWheelToggleKey);
        event.register(LOTMCraft.openWheelHoldKey);
        event.register(LOTMCraft.useSelectedAbilityKey);
        event.register(LOTMCraft.useAbilityBarAbility1);
        event.register(LOTMCraft.useAbilityBarAbility2);
        event.register(LOTMCraft.useAbilityBarAbility3);
        event.register(LOTMCraft.useAbilityBarAbility4);
        event.register(LOTMCraft.useAbilityBarAbility5);
        event.register(LOTMCraft.useAbilityBarAbility6);
        event.register(LOTMCraft.returnToMainBody);
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
                        return 0xFFFFFFFF; // white = no tint
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
}