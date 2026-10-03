package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class WorldBorderEventHandler {

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceLocation dimensionId = serverLevel.dimension().location();
        String dimensionPath = dimensionId.getPath();

        switch (dimensionPath) {
            case "concealment_world" -> {
                serverLevel.getWorldBorder().setSize(100000);
                serverLevel.getWorldBorder().setCenter(0, 0);
            }
            case "dream_maze" -> {
                serverLevel.getWorldBorder().setSize(800000);
                serverLevel.getWorldBorder().setCenter(0, 0);
            }
            case "nature" -> {
                serverLevel.getWorldBorder().setSize(1200000);
                serverLevel.getWorldBorder().setCenter(0, 0);
            }
            case "space" -> {
                serverLevel.getWorldBorder().setSize(150000);
                serverLevel.getWorldBorder().setCenter(0, 0);
            }
            case "spirit_world" -> {
                serverLevel.getWorldBorder().setSize(200000);
                serverLevel.getWorldBorder().setCenter(0, 0);
            }
        }
    }
}