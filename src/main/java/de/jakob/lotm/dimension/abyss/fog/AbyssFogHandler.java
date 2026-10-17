package de.jakob.lotm.dimension.abyss.fog;


import com.mojang.blaze3d.shaders.FogShape;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class AbyssFogHandler {

    private static final float NEAR_FRACTION = 0.20f;
    private static boolean inAbyss() {
        var level = Minecraft.getInstance().level;
        return level != null && level.dimension().equals(ModDimensions.ABYSS_DIMENSION_KEY);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!inAbyss() || event.getType() != FogType.NONE) return;

        float far = AbyssFogNetwork.clientFar;
        if (far < 0) return; // sequence 0: leave vanilla fog alone, no mist
        event.setNearPlaneDistance(far * NEAR_FRACTION);
        event.setFarPlaneDistance(far);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!inAbyss()) return;
        event.setRed(0.03f);
        event.setGreen(0.03f);
        event.setBlue(0.045f);
    }
}
