package de.jakob.lotm.dimesion;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpaceDimensionEffects {

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"),
            new SpaceEffects()
        );
    }

    public static class SpaceEffects extends DimensionSpecialEffects {
        public SpaceEffects() {
            super(Float.NaN, true, SkyType.NONE, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
            // Pure black fog
            return new Vec3(0.0, 0.0, 0.0);
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return false;
        }
    }
}