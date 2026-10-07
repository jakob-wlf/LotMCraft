package de.jakob.lotm.dimension;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class SpiritWorldFogHandler {

    private static final float FOG_NEAR = 80.0f;   // tune these
    private static final float FOG_FAR  = 170.0f;

    private static boolean inSpiritWorld(Camera camera) {
        return camera.getEntity().level() instanceof ClientLevel level
                && level.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!inSpiritWorld(event.getCamera())) return;
        if (event.getType() != FogType.NONE) return;                    // keep water/lava fog vanilla
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return; // don't touch sky fog

        event.setNearPlaneDistance(FOG_NEAR);
        event.setFarPlaneDistance(FOG_FAR);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Camera camera = event.getCamera();
        if (!inSpiritWorld(camera)) return;

        BlockPos pos = camera.getEntity().blockPosition();
        SpiritWorldSky.update(pos.getX(), pos.getZ());

        Vec3 c = SpiritWorldSky.horizon(); // fog == horizon color -> terrain fades into the sky seamlessly
        event.setRed((float) c.x);
        event.setGreen((float) c.y);
        event.setBlue((float) c.z);
    }
}