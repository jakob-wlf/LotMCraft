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
public class RiverOfEternalDarknessDimensionEffects {

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_of_eternal_darkness"),
                new RiverEffects()
        );
    }

    public static class RiverEffects extends DimensionSpecialEffects {
        public RiverEffects() {
            super(
                    Float.NaN,
                    false,
                    SkyType.NONE,
                    false,
                    false
            );
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
            return new Vec3(0.02, 0.02, 0.02);
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return true;
        }
    }
}
