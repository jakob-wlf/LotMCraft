package de.jakob.lotm.entity.client.ability_entities.twilight_giant.silver_rapier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.SilverRapierEntity;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class SilverRapierRenderer extends EntityRenderer<SilverRapierEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/silver_rapier.png");

    public SilverRapierRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(SilverRapierEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(-45.0F));
        if (entity.isStriking()) {
            float spin = (entity.tickCount + partialTicks) % 10.0F / 10.0F * 360.0F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
            poseStack.scale(1.0F, 1.0F, 1.9F);
        }
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        for (SilverRapierMesh.Cube cube : SilverRapierMesh.CUBES) {
            float[] box = box(cube);
            for (SilverRapierMesh.Face face : cube.faces()) {
                Vector3f normal = rotate(new Vector3f(face.direction().getStepX(), face.direction().getStepY(), face.direction().getStepZ()), new float[]{0.0F, 0.0F, 0.0F}, cube.rotation());
                if (normal.lengthSquared() > 1.0E-6F) normal.normalize();
                for (int vertex = 0; vertex < 4; vertex++) {
                    FaceInfo.VertexInfo info = FaceInfo.fromFacing(face.direction()).getVertexInfo(vertex);
                    Vector3f point = rotate(new Vector3f(box[info.xFace], box[info.yFace], box[info.zFace]), cube.origin(), cube.rotation());
                    point.sub(SilverRapierMesh.PIVOT_X, SilverRapierMesh.PIVOT_Y, SilverRapierMesh.PIVOT_Z).div(16.0F);
                    float u = ((vertex == 0 || vertex == 1) ? face.u0() : face.u1()) / SilverRapierMesh.UV_SIZE;
                    float v = ((vertex == 0 || vertex == 3) ? face.v0() : face.v1()) / SilverRapierMesh.UV_SIZE;
                    consumer.addVertex(pose, point.x, point.y, point.z)
                            .setColor(255, 255, 255, 255)
                            .setUv(u, v)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(packedLight)
                            .setNormal(pose, normal.x, normal.y, normal.z);
                }
            }
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SilverRapierEntity entity) {
        return TEXTURE;
    }

    private static float[] box(SilverRapierMesh.Cube cube) {
        float[] box = new float[6];
        box[Direction.WEST.get3DDataValue()] = Math.min(cube.from()[0], cube.to()[0]);
        box[Direction.EAST.get3DDataValue()] = Math.max(cube.from()[0], cube.to()[0]);
        box[Direction.DOWN.get3DDataValue()] = Math.min(cube.from()[1], cube.to()[1]);
        box[Direction.UP.get3DDataValue()] = Math.max(cube.from()[1], cube.to()[1]);
        box[Direction.NORTH.get3DDataValue()] = Math.min(cube.from()[2], cube.to()[2]);
        box[Direction.SOUTH.get3DDataValue()] = Math.max(cube.from()[2], cube.to()[2]);
        return box;
    }

    private static Vector3f rotate(Vector3f point, float[] origin, float[] rotation) {
        Vector3f vector = new Vector3f(point).sub(origin[0], origin[1], origin[2]);
        if (rotation[0] != 0) vector.rotateX(rotation[0] * Mth.DEG_TO_RAD);
        if (rotation[1] != 0) vector.rotateY(rotation[1] * Mth.DEG_TO_RAD);
        if (rotation[2] != 0) vector.rotateZ(rotation[2] * Mth.DEG_TO_RAD);
        return vector.add(origin[0], origin[1], origin[2]);
    }
}
