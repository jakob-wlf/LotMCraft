package de.jakob.lotm.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.effect.ModEffects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class MushroomRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation MUSHROOM_TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/mushroom_overlay.png");
    private final Random random = new Random();

    public MushroomRenderLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // Check your condition here
        if (!shouldRenderMushrooms(entity)) {
            return;
        }

        // Use entity UUID as seed for consistent mushroom placement
        random.setSeed(entity.getUUID().getLeastSignificantBits());

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(MUSHROOM_TEXTURE));

        float entityHeight = entity.getBbHeight();
        float entityWidth = entity.getBbWidth();

        // Entity center point (for stem orientation)
        float centerX = 0f;
        float centerY = entityHeight / 2.0f;
        float centerZ = 0f;

        // Calculate mushroom count based on entity surface area
        float entitySize = (entityWidth + entityHeight) / 2.0f;

        // Base mushroom count scales with size
        int baseCount = (int) (entitySize * 5);
        int variation = Math.max(2, (int) (entitySize * 2));
        int mushroomCount = baseCount + random.nextInt(variation);

        // Ensure minimum and reasonable maximum
        mushroomCount = Math.max(3, Math.min(mushroomCount, 30));

        for (int i = 0; i < mushroomCount; i++) {
            poseStack.pushPose();

            // Distribute evenly across surfaces
            float totalSurfaceArea = 2 * (entityWidth * entityWidth) + // top + bottom
                    4 * (entityWidth * entityHeight);  // 4 sides

            float faceChoice = random.nextFloat();

            float x = 0, y = 0, z = 0;
            float nx = 0, ny = 0, nz = 0; // Surface normal (points outward)

            float topBottomArea = (entityWidth * entityWidth) / totalSurfaceArea;
            float sideArea = (entityWidth * entityHeight) / totalSurfaceArea;

            // Decide which surface based on relative area
            // Reduce bottom surface spawn rate by 60%
            float adjustedTopBottomArea = topBottomArea * 0.4f; // Bottom gets 40% of its normal spawns

            if (faceChoice < topBottomArea) {
                // Top surface
                x = (random.nextFloat() - 0.5f) * entityWidth * 0.95f;
                z = (random.nextFloat() - 0.5f) * entityWidth * 0.95f;
                y = entityHeight;
                ny = 1;
            } else if (faceChoice < topBottomArea + adjustedTopBottomArea) {
                // Bottom surface (reduced frequency)
                x = (random.nextFloat() - 0.5f) * entityWidth * 0.95f;
                z = (random.nextFloat() - 0.5f) * entityWidth * 0.95f;
                y = 0.1f + random.nextFloat() * 0.1f; // Keep well above the bottom edge
                ny = -1;
            } else {
                // Side surfaces - distributed among 4 sides
                float sideChoice = (faceChoice - topBottomArea * 2) / (sideArea * 4);
                y = random.nextFloat() * entityHeight * 0.95f;

                if (sideChoice < 0.25f) {
                    // Front face (+Z)
                    x = (random.nextFloat() - 0.5f) * entityWidth * 0.95f;
                    z = entityWidth / 2.0f;
                    nz = 1;
                } else if (sideChoice < 0.5f) {
                    // Back face (-Z)
                    x = (random.nextFloat() - 0.5f) * entityWidth * 0.95f;
                    z = -entityWidth / 2.0f;
                    nz = -1;
                } else if (sideChoice < 0.75f) {
                    // Right face (+X)
                    z = (random.nextFloat() - 0.5f) * entityWidth * 0.95f;
                    x = entityWidth / 2.0f;
                    nx = 1;
                } else {
                    // Left face (-X)
                    z = (random.nextFloat() - 0.5f) * entityWidth * 0.95f;
                    x = -entityWidth / 2.0f;
                    nx = -1;
                }
            }

            // Position at the surface
            poseStack.translate(x, y, z);

            // Calculate direction from mushroom position to entity center (for stem orientation)
            float towardCenterX = centerX - x;
            float towardCenterY = centerY - y;
            float towardCenterZ = centerZ - z;

            // Normalize the direction vector
            float length = Mth.sqrt(towardCenterX * towardCenterX + towardCenterY * towardCenterY + towardCenterZ * towardCenterZ);
            if (length > 0.01f) {
                towardCenterX /= length;
                towardCenterY /= length;
                towardCenterZ /= length;
            }

            // We want stem to point toward center, but the mushroom model has stem at y=0 pointing down
            // So we need to negate the direction to point the stem (not cap) toward center
            towardCenterX = -towardCenterX;
            towardCenterY = -towardCenterY;
            towardCenterZ = -towardCenterZ;

            // Rotate mushroom so stem points toward entity center
            // Calculate yaw (rotation around Y axis)
            float yaw = (float) Math.toDegrees(Mth.atan2(-towardCenterX, -towardCenterZ));

            // Calculate pitch (rotation to tilt toward center)
            float horizontalDist = Mth.sqrt(towardCenterX * towardCenterX + towardCenterZ * towardCenterZ);
            float pitch = -(float) Math.toDegrees(Mth.atan2(towardCenterY, horizontalDist));

            // Apply rotations
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

            // Random rotation around the stem axis for variety
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360));

            // Scale
            float scale = 0.5f + random.nextFloat() * 0.4f;

            // After rotation, translate along the rotated Y axis to keep cap at surface
            // The mushroom is 1 unit tall, so translate by -scale to embed it properly
            poseStack.translate(0, -scale * 0.9f, 0);

            poseStack.scale(scale, scale, scale);

            // Render cross-shaped mushroom
            renderMushroomCross(poseStack, vertexConsumer, packedLight);

            poseStack.popPose();
        }
    }

    private void renderMushroomCross(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();

        // First quad (aligned with X axis)
        vertex(consumer, pose, -0.5f, 0, 0, 0, 1, light);  // Bottom left
        vertex(consumer, pose, 0.5f, 0, 0, 1, 1, light);   // Bottom right
        vertex(consumer, pose, 0.5f, 1, 0, 1, 0, light);   // Top right
        vertex(consumer, pose, -0.5f, 1, 0, 0, 0, light);  // Top left

        // Render backface of first quad
        vertex(consumer, pose, -0.5f, 1, 0, 0, 0, light);  // Top left
        vertex(consumer, pose, 0.5f, 1, 0, 1, 0, light);   // Top right
        vertex(consumer, pose, 0.5f, 0, 0, 1, 1, light);   // Bottom right
        vertex(consumer, pose, -0.5f, 0, 0, 0, 1, light);  // Bottom left

        // Second quad (rotated 90 degrees - aligned with Z axis)
        vertex(consumer, pose, 0, 0, -0.5f, 0, 1, light);  // Bottom left
        vertex(consumer, pose, 0, 0, 0.5f, 1, 1, light);   // Bottom right
        vertex(consumer, pose, 0, 1, 0.5f, 1, 0, light);   // Top right
        vertex(consumer, pose, 0, 1, -0.5f, 0, 0, light);  // Top left

        // Render backface of second quad
        vertex(consumer, pose, 0, 1, -0.5f, 0, 0, light);  // Top left
        vertex(consumer, pose, 0, 1, 0.5f, 1, 0, light);   // Top right
        vertex(consumer, pose, 0, 0, 0.5f, 1, 1, light);   // Bottom right
        vertex(consumer, pose, 0, 0, -0.5f, 0, 1, light);  // Bottom left
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                        float x, float y, float z, float u, float v, int light) {
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0, 1, 0);
    }

    private boolean shouldRenderMushrooms(T entity) {
        if(!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        if(!livingEntity.hasEffect(ModEffects.MUTATED)) {
            return false;
        }

        return true;
    }
}