package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SefirahCastleDimensionEffects{

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"),
                new SpaceDimensionEffects.SpaceEffects()
        );
    }
    
    public static class SefirahCastleEffects extends DimensionSpecialEffects {
        public SefirahCastleEffects() {
            super(
                    Float.NaN, // Cloud height (NaN = no clouds)
                    true, // Has sky effects
                    SkyType.NONE, // No sky rendering (no sun, moon, stars)
                    false, // Not bright enough to force bright lighting
                    false  // Not dark enough to force darkness
            );
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
            // Return a constant fog color, unaffected by brightness
            return new Vec3(0.5, 0.5, 0.5); // Gray fog
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return false; // No fog
        }
    }
}