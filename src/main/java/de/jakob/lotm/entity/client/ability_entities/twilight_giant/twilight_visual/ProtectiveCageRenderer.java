package de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.client.ability_entities.twilight_giant.twilight_visual.TwilightDomeGeo.UV;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.ProtectiveCageEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ProtectiveCageRenderer extends EntityRenderer<ProtectiveCageEntity> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/twilight_visual/cage.png");
    private static final UV FULL = new UV(0.0F, 0.0F, 1.0F, 1.0F);
    private static final UV SOLID = new UV(0.010F, 0.30F, 0.040F, 0.70F);
    private static final float HEIGHT = 5.0F;

    public ProtectiveCageRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ProtectiveCageEntity entity) {
        return TEX;
    }

    @Override
    public boolean shouldRender(ProtectiveCageEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(ProtectiveCageEntity cage, float yaw, float partial, PoseStack ps, MultiBufferSource buffers, int packedLight) {
        int light = TwilightDomeGeo.lift(packedLight);
        VertexConsumer c = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
        float fit = cage.radius() / ProtectiveCageEntity.NATIVE_HALF;
        float s = ProtectiveCageEntity.NATIVE_HALF;

        ps.pushPose();
        ps.scale(fit, fit * cage.rise(partial), fit);
        PoseStack.Pose p = ps.last();

        TwilightDomeGeo.quad(c, p, light, TwilightDomeGeo.WHITE, FULL, s, HEIGHT, s, s, 0, s, s, 0, -s, s, HEIGHT, -s, 1, 0, 0);
        TwilightDomeGeo.quad(c, p, light, TwilightDomeGeo.WHITE, FULL, -s, HEIGHT, -s, -s, 0, -s, -s, 0, s, -s, HEIGHT, s, -1, 0, 0);
        TwilightDomeGeo.quad(c, p, light, TwilightDomeGeo.WHITE, FULL, -s, HEIGHT, s, -s, 0, s, s, 0, s, s, HEIGHT, s, 0, 0, 1);
        TwilightDomeGeo.quad(c, p, light, TwilightDomeGeo.WHITE, FULL, s, HEIGHT, -s, s, 0, -s, -s, 0, -s, -s, HEIGHT, -s, 0, 0, -1);
        TwilightDomeGeo.quad(c, p, light, TwilightDomeGeo.WHITE, FULL, -s, HEIGHT, -s, -s, HEIGHT, s, s, HEIGHT, s, s, HEIGHT, -s, 0, 1, 0);

        float t = 0.15F;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                float cx = sx * (s - t);
                float cz = sz * (s - t);
                TwilightDomeGeo.box(c, p, light, SOLID, SOLID, cx - t, 0.0F, cz - t, cx + t, HEIGHT + 0.1F, cz + t);
            }
        }
        ps.popPose();
    }
}
