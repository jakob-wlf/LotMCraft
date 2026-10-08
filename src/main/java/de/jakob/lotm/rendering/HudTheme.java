package de.jakob.lotm.rendering;

import com.mojang.blaze3d.platform.NativeImage;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public final class HudTheme {

    private HudTheme() {}

    
    public static final int BRASS = 0xFFB8893B;
    public static final int IRON = 0xFF1B1A1F;
    public static final int PARCHMENT = 0xFFE8DCC0;
    public static final int PARCHMENT_DIM = 0xFF9A8F78;
    public static final int EMBER = 0xFFD9381E;
    public static final int EMBER_BRIGHT = 0xFFFF9A3C;
    public static final int MARIONETTE_ACCENT = 0xFFA742F5;
    public static final int KEYCAP_TEXT = PARCHMENT; 

    
    
    private static final String HUD_DIR = "textures/gui/hud/";

    public static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, HUD_DIR + name + ".png");
    }

    public static final ResourceLocation FRAME = tex("frame_9slice");
    public static final ResourceLocation GLYPH_TILE = tex("glyph_tile");
    public static final ResourceLocation TUBE_FRAME = tex("tube_frame");
    public static final ResourceLocation TUBE_GLASS = tex("tube_glass");
    public static final ResourceLocation MEDALLION = tex("medallion");
    public static final ResourceLocation MEDALLION_COG = tex("medallion_cog");
    public static final ResourceLocation DIAL = tex("dial");
    public static final ResourceLocation DIAL_GLASS = tex("dial_glass");
    public static final ResourceLocation NEEDLE = tex("needle");
    public static final ResourceLocation SOCKET = tex("socket");
    public static final ResourceLocation SOCKET_RING = tex("socket_ring");
    public static final ResourceLocation PLAQUE = tex("plaque");
    public static final ResourceLocation JAR = tex("jar");
    public static final ResourceLocation JAR_GLASS = tex("jar_glass");
    public static final ResourceLocation COUNTER_PLATE = tex("counter_plate");
    public static final ResourceLocation KEYCAP = tex("keycap");

    
    
    public static final int TUBE_INSET_X = 3;
    public static final int TUBE_INSET_Y = 2;

    
    public static final float NEEDLE_ANGLE_EMPTY = -70f;
    public static final float NEEDLE_ANGLE_FULL = 70f;

    
    private static final Map<ResourceLocation, int[]> SIZE_CACHE = new HashMap<>();

    
    public static int[] size(ResourceLocation tex) {
        int[] cached = SIZE_CACHE.get(tex);
        if (cached != null) return cached;

        int[] result = new int[]{16, 16};
        try (InputStream in = Minecraft.getInstance().getResourceManager().open(tex);
             NativeImage img = NativeImage.read(in)) {
            result = new int[]{img.getWidth(), img.getHeight()};
        } catch (Exception e) {
            LOTMCraft.LOGGER.warn("HUD: could not read size of texture {}", tex);
        }
        SIZE_CACHE.put(tex, result);
        return result;
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> SIZE_CACHE.clear());
    }

    
    
    public static float time(Minecraft mc, DeltaTracker dt) {
        if (mc.level == null) return 0f;
        return mc.level.getGameTime() + dt.getGameTimeDeltaPartialTick(false);
    }

    
    public static int pathwayColor(String pathway) {
        var info = BeyonderData.pathwayInfos.get(pathway);
        if (info == null) return BRASS;
        return info.color() | 0xFF000000;
    }

    
    public static float clamp01(float v) {
        return v < 0f ? 0f : Math.min(v, 1f);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float smoothstep(float t) {
        t = clamp01(t);
        return t * t * (3f - 2f * t);
    }

    
    public static float pulse(float time, float speed) {
        return 0.5f + 0.5f * (float) Math.sin(time * speed);
    }

    public static float frac(float v) {
        return v - (float) Math.floor(v);
    }

    
    public static float smoothFactor(float realtimeDeltaTicks, float perTickKeep) {
        return 1f - (float) Math.pow(perTickKeep, Math.max(realtimeDeltaTicks, 0.01f));
    }

    
    public static int withAlpha(int argb, float alpha) {
        int a = (int) (clamp01(alpha) * 255f);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int scaleAlpha(int argb, float factor) {
        int a = (int) (((argb >>> 24) & 0xFF) * clamp01(factor));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int lighten(int argb, float amount) {
        return interpolate(argb, (argb & 0xFF000000) | 0xFFFFFF, amount);
    }

    public static int darken(int argb, float amount) {
        return interpolate(argb, (argb & 0xFF000000), amount);
    }

    public static int interpolate(int c1, int c2, float t) {
        t = clamp01(t);
        int a = (int) (((c1 >>> 24) & 0xFF) + (((c2 >>> 24) & 0xFF) - ((c1 >>> 24) & 0xFF)) * t);
        int r = (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t);
        int g = (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t);
        int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    
    public static int healthColor(float fill) {
        if (fill > 0.6f) return 0xFF3DDC84;
        if (fill > 0.3f) return 0xFFF5A623;
        return 0xFFD0202E;
    }
}