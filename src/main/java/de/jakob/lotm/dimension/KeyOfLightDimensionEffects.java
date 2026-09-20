package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class KeyOfLightDimensionEffects {

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "key_of_light"),
                new KeyOfLightEffects()
        );
    }

    public static class KeyOfLightEffects extends DimensionSpecialEffects {

        public KeyOfLightEffects() {
            super(Float.NaN, true, SkyType.NORMAL, false, false);
        }

        @Override
        public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
            return fogColor;
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return false;
        }
    }
}
