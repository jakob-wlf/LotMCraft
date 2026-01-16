package de.jakob.lotm.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.MeteorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MeteorRenderer extends EntityRenderer<MeteorEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/meteor/meteor.png");
    private static final ResourceLocation TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/meteor/meteor_trail.png");
    private final MeteorModel<MeteorEntity> model;

    // Particle storage per entity
    private static class ParticleData {
        Vec3 position;
        Vec3 velocity;
        float size;
        float alpha;
        float age;
        int maxAge;
        ParticleType type;
        float rotation;
        float rotationSpeed;
        float brightness; // For flickering effect

        ParticleData(Vec3 pos, Vec3 vel, float size, int maxAge, ParticleType type) {
            this.position = pos;
            this.velocity = vel;
            this.size = size;
            this.alpha = 1.0f;
            this.age = 0;
            this.maxAge = maxAge;
            this.type = type;
            this.rotation = (float)(Math.random() * Math.PI * 2);
            this.rotationSpeed = (float)((Math.random() - 0.5) * 0.3);
            this.brightness = 0.9f + (float)Math.random() * 0.1f;
        }

        void tick(float partialTick) {
            age += partialTick;
            position = position.add(velocity.scale(partialTick));
            velocity = velocity.scale(0.94); // Smoother deceleration
            rotation += rotationSpeed * partialTick;

            // Smooth flickering for fire
            if (type == ParticleType.FIRE) {
                brightness = 0.85f + (float)Math.sin(age * 0.5) * 0.15f;
            }

            // Smoother fade out curve with partial tick
            float progress = age / maxAge;
            if (progress < 0.3f) {
                alpha = 1.0f;
            } else {
                // Smooth cubic ease-out
                float fadeProgress = (progress - 0.3f) / 0.7f;
                alpha = 1.0f - (fadeProgress * fadeProgress * fadeProgress);
            }

            // Smoother size scaling
            if (type == ParticleType.FIRE) {
                size *= 1.0f + (0.012f * partialTick);
            } else if (type == ParticleType.SMOKE) {
                size *= 1.0f + (0.035f * partialTick);
            }
        }

        boolean isExpired() {
            return age >= maxAge;
        }
    }

    private enum ParticleType {
        FIRE, SMOKE, TRAIL, EMBER, SPARK
    }

    private final List<ParticleData> particles = new ArrayList<>();
    private float particleAccumulator = 0f;

    public MeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MeteorModel<>(context.bakeLayer(MeteorModel.LAYER_LOCATION));
    }

    @Override
    public void render(MeteorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Scale the model up
        poseStack.scale(entity.getSize(), entity.getSize(), entity.getSize());
        poseStack.translate(0, -.6, 0);

        // Render the main meteor model
        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();

        if(entity.getLifeTicks() > 2) {
            spawnParticles(entity, partialTicks);
            updateParticles(partialTicks);

            renderParticles(entity, poseStack, buffer, packedLight, partialTicks);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void spawnParticles(MeteorEntity entity, float partialTicks) {
        Vec3 entityPos = entity.position();
        Vec3 motion = entity.getDeltaMovement();
        float size = entity.getSize();

        // Use accumulator for smoother spawning
        particleAccumulator += partialTicks;

        // Spawn particles based on accumulator to smooth out frame rate variations
        while (particleAccumulator >= 0.25f) {
            particleAccumulator -= 0.25f;

            // Spawn fire particles (bright, medium-lived)
            for (int i = 0; i < 4; i++) { // Increased count for denser trail
                Vec3 offset = new Vec3(
                        (entity.level().random.nextFloat() - 0.5) * size * 0.7,
                        (entity.level().random.nextFloat() - 0.5) * size * 0.7,
                        (entity.level().random.nextFloat() - 0.5) * size * 0.7
                );
                Vec3 particleVel = motion.scale(-0.25).add(
                        (entity.level().random.nextFloat() - 0.5) * 0.08,
                        (entity.level().random.nextFloat() - 0.5) * 0.08,
                        (entity.level().random.nextFloat() - 0.5) * 0.08
                );
                particles.add(new ParticleData(
                        entityPos.add(offset),
                        particleVel,
                        size * (0.4f + entity.level().random.nextFloat() * 0.30f),
                        20 + entity.level().random.nextInt(15),
                        ParticleType.FIRE
                ));
            }

            // Spawn smoke particles
            for (int i = 0; i < 2; i++) {
                Vec3 offset = new Vec3(
                        (entity.level().random.nextFloat() - 0.5) * size * 0.6,
                        (entity.level().random.nextFloat() - 0.5) * size * 0.6,
                        (entity.level().random.nextFloat() - 0.5) * size * 0.6
                );
                Vec3 particleVel = motion.scale(-0.15).add(
                        (entity.level().random.nextFloat() - 0.5) * 0.05,
                        (entity.level().random.nextFloat() - 0.5) * 0.05,
                        (entity.level().random.nextFloat() - 0.5) * 0.05
                );
                particles.add(new ParticleData(
                        entityPos.add(offset),
                        particleVel,
                        size * (0.35f + entity.level().random.nextFloat() * 0.25f),
                        35 + entity.level().random.nextInt(25),
                        ParticleType.SMOKE
                ));
            }

            // Spawn trail particles
            for (int i = 0; i < 3; i++) {
                Vec3 offset = new Vec3(
                        (entity.level().random.nextFloat() - 0.5) * size * 0.5,
                        (entity.level().random.nextFloat() - 0.5) * size * 0.5,
                        (entity.level().random.nextFloat() - 0.5) * size * 0.5
                );
                Vec3 particleVel = motion.scale(-0.15);
                particles.add(new ParticleData(
                        entityPos.add(offset),
                        particleVel,
                        size * (0.2f + entity.level().random.nextFloat() * 0.15f),
                        18 + entity.level().random.nextInt(12),
                        ParticleType.TRAIL
                ));
            }
        }
    }

    private void updateParticles(float partialTicks) {
        Iterator<ParticleData> iterator = particles.iterator();
        while (iterator.hasNext()) {
            ParticleData particle = iterator.next();
            particle.tick(partialTicks);
            if (particle.isExpired()) {
                iterator.remove();
            }
        }
    }

    private void renderParticles(MeteorEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTicks) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.itemEntityTranslucentCull(TRAIL_TEXTURE));

        for (ParticleData particle : particles) {
            // For smoke, render multiple rotated quads to create spherical appearance
            int quadCount = particle.type == ParticleType.SMOKE ? 3 : 1;

            for (int q = 0; q < quadCount; q++) {
                poseStack.pushPose();

                // Interpolate position for smoother movement
                Vec3 relativePos = particle.position.subtract(entity.position());
                poseStack.translate(relativePos.x, relativePos.y, relativePos.z);

                // Billboard rotation to face camera
                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                // Smooth rotation interpolation
                float smoothRotation = particle.rotation + (particle.rotationSpeed * partialTicks);
                poseStack.mulPose(Axis.ZP.rotation(smoothRotation + (q * (float)Math.PI / 1.5f)));

                // Additional rotation for smoke to make it spherical
                if (particle.type == ParticleType.SMOKE) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(q * 60f));
                }

                Matrix4f matrix = poseStack.last().pose();
                Matrix3f normal = poseStack.last().normal();

                float size = particle.size;
                float alpha = particle.alpha;

                // Reduce alpha for smoke when using multiple quads
                if (particle.type == ParticleType.SMOKE) {
                    alpha *= 0.5f;
                }

                // Color based on particle type with brightness variation
                int r, g, b, a;
                a = (int)(alpha * 255);

                switch (particle.type) {
                    case FIRE:
                        r = 255;
                        g = (int)(100 + 80 * (1.0f - particle.alpha * 0.5f));
                        b = (int)(30 * (1.0f - particle.alpha));
                        // Apply brightness variation for flickering
                        r = (int)(r * particle.brightness);
                        g = (int)(g * particle.brightness);
                        b = (int)(b * particle.brightness);
                        a = (int)(alpha * 240);
                        break;
                    case SMOKE:
                        int grayValue = (int)(10 + 25 * particle.alpha);
                        r = grayValue;
                        g = grayValue;
                        b = grayValue;
                        a = (int)(alpha * 200);
                        break;
                    case TRAIL:
                        r = 255;
                        g = 255;
                        b = (int)(200 + 55 * particle.alpha);
                        a = (int)(alpha * 230);
                        break;
                    case EMBER:
                        r = 255;
                        g = (int)(120 + 80 * particle.alpha);
                        b = 40;
                        a = (int)(alpha * 255);
                        break;
                    case SPARK:
                        r = 255;
                        g = 255;
                        b = (int)(180 + 75 * particle.alpha);
                        a = (int)(alpha * 255);
                        break;
                    default:
                        r = g = b = 255;
                }

                int particleLight = particle.type == ParticleType.FIRE ||
                        particle.type == ParticleType.TRAIL ||
                        particle.type == ParticleType.EMBER ||
                        particle.type == ParticleType.SPARK ?
                        15728880 : packedLight;

                renderCircularQuad(consumer, matrix, normal, size, r, g, b, a, particleLight);

                poseStack.popPose();
            }
        }
    }

    private void renderCircularQuad(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                    float size, int r, int g, int b, int a, int light) {
        consumer.addVertex(matrix, -size, -size, 0).setColor(r, g, b, a)
                .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(0, 1, 0);
        consumer.addVertex(matrix, -size, size, 0).setColor(r, g, b, a)
                .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(0, 1, 0);
        consumer.addVertex(matrix, size, size, 0).setColor(r, g, b, a)
                .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(0, 1, 0);
        consumer.addVertex(matrix, size, -size, 0).setColor(r, g, b, a)
                .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(0, 1, 0);
    }

    @Override
    protected int getSkyLightLevel(MeteorEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getBlockLightLevel(MeteorEntity entity, BlockPos pos) {
        return 15;
    }

    private void renderTrail(MeteorEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float size = entity.getSize();

        poseStack.translate(0f, .75f * size, -.2f * size);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-65f));

        int segments = 8;
        float trailLength = 3.0f * size;
        float segmentLength = trailLength / segments;

        VertexConsumer trailConsumer = buffer.getBuffer(RenderType.entityTranslucent(TRAIL_TEXTURE));

        for (int i = 0; i < segments; i++) {
            float progress = (float) i / segments;

            float alpha = (1.0f - (progress * 0.8f)) * 0.5f;

            float time = entity.tickCount + entity.level().getGameTime();
            float sineWave = (float)Math.sin(time * 0.1 + progress * 3.0) * 0.08f;
            float width = size * 0.6f * (1.0f - progress * 0.7f) * (1.0f + sineWave);

            float z = segmentLength * i;
            float nextZ = segmentLength * (i + 1);

            float nextProgress = (float)(i + 1) / segments;
            float nextSineWave = (float)Math.sin(time * 0.1 + nextProgress * 3.0) * 0.08f;
            float nextWidth = size * 0.6f * (1.0f - nextProgress * 0.7f) * (1.0f + nextSineWave);

            renderTrailSegment(poseStack, trailConsumer, z, nextZ, width, nextWidth, alpha, packedLight);
        }

        poseStack.popPose();
    }

    private void renderTrailSegment(PoseStack poseStack, VertexConsumer consumer, float z1, float z2,
                                    float width1, float width2, float alpha, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int color = (int)(alpha * 255) << 24 | 0xFFFFFF;
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        int sides = 8;
        for (int i = 0; i < sides; i++) {
            float angle1 = (float) (2 * Math.PI * i / sides);
            float angle2 = (float) (2 * Math.PI * (i + 1) / sides);

            float x1 = (float) Math.cos(angle1);
            float y1 = (float) Math.sin(angle1);
            float x2 = (float) Math.cos(angle2);
            float y2 = (float) Math.sin(angle2);

            float fx1 = x1 * width1;
            float fy1 = y1 * width1;
            float fx2 = x2 * width1;
            float fy2 = y2 * width1;

            float bx1 = x1 * width2;
            float by1 = y1 * width2;
            float bx2 = x2 * width2;
            float by2 = y2 * width2;

            Vec3 v1 = new Vec3(fx1, fy1, z1);
            Vec3 v2 = new Vec3(fx2, fy2, z1);
            Vec3 v3 = new Vec3(bx2, by2, z2);
            Vec3 edge1 = v2.subtract(v1);
            Vec3 edge2 = v3.subtract(v1);
            Vec3 norm = edge1.cross(edge2).normalize();

            consumer.addVertex(matrix, fx1, fy1, z1).setColor(r, g, b, a)
                    .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal((float)norm.x, (float)norm.y, (float)norm.z);
            consumer.addVertex(matrix, fx2, fy2, z1).setColor(r, g, b, a)
                    .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal((float)norm.x, (float)norm.y, (float)norm.z);
            consumer.addVertex(matrix, bx2, by2, z2).setColor(r, g, b, a)
                    .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal((float)norm.x, (float)norm.y, (float)norm.z);
            consumer.addVertex(matrix, bx1, by1, z2).setColor(r, g, b, a)
                    .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal((float)norm.x, (float)norm.y, (float)norm.z);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(MeteorEntity entity) {
        return TEXTURE;
    }
}