package de.jakob.lotm.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class IllusoryScalesRenderTypes extends RenderType {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/illusory_scales.png");

    private static final float TILING = 1.0F;
    private static final float SCROLL_U = 0.015F;
    private static final float SCROLL_V = 0.030F;

    private static final TexturingStateShard SHIMMER = new TexturingStateShard(
            "illusory_scales_shimmer",
            () -> {
                float t = (Util.getMillis() % 1_000_000L) / 1000.0F;
                float u = (t * SCROLL_U) % 1.0F;
                float v = (t * SCROLL_V) % 1.0F;
                RenderSystem.setTextureMatrix(new Matrix4f().translation(u, v, 0.0F).scale(TILING, TILING, 1.0F));
            },
            RenderSystem::resetTextureMatrix
    );

    public static final RenderType SCALES = create(
            "illusory_scales",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)   // supports texture matrix + full-bright
                    .setTextureState(new TextureStateShard(TEXTURE, false, false))
                    .setTexturingState(SHIMMER)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)       // glowy, "illusory" look
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(false)
    );

    public static final RenderType SCALES_GHOST = create(
            "illusory_scales_ghost",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                    .setTextureState(new TextureStateShard(TEXTURE, false, false))
                    .setTexturingState(SHIMMER)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(false)
    );

    private IllusoryScalesRenderTypes() {
        super("unused", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {});
        throw new UnsupportedOperationException("Utility class");
    }
}