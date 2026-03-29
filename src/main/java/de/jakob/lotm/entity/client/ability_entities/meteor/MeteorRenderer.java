package de.jakob.lotm.entity.client.ability_entities.meteor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
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

import java.util.*;

public class MeteorRenderer extends EntityRenderer<MeteorEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/meteor/meteor.png");
    private static final ResourceLocation TRAIL_TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/entity/meteor/meteor_trail.png");
    private final MeteorModel<MeteorEntity> model;

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
        float brightness;

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
            velocity = velocity.scale(0.94);
            rotation += rotationSpeed * partialTick;

            if (type == ParticleType.FIRE) {
                brightness = 0.85f + (float)Math.sin(age * 0.5) * 0.15f;
            }

            float progress = age / maxAge;
            if (progress < 0.3f) {
                alpha = 1.0f;
            } else {
                float fadeProgress = (progress - 0.3f) / 0.7f;
                alpha = 1.0f - (fadeProgress * fadeProgress * fadeProgress);
            }

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

    private enum ParticleType { FIRE, SMOKE, TRAIL, EMBER, SPARK }

    private static class EntityParticleData {
        List<ParticleData> particles = new ArrayList<>();
        float particleAccumulator = 0f;
    }

    private final Map<UUID, EntityParticleData> entityParticles = new HashMap<>();

    public MeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MeteorModel<>(context.bakeLayer(MeteorModel.LAYER_LOCATION));
    }

    @Override
    public void render(MeteorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        boolean petrified = entity.getTags().contains("petrified");

        poseStack.pushPose();

        poseStack.scale(entity.getSize(), entity.getSize(), entity.getSize());
        poseStack.translate(0, -.6, 0);

        var vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        // Gray tint when petrified
        int modelColor = petrified ? 0xFF808080 : 0xFFFFFFFF;
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, modelColor);

        poseStack.popPose();

        if (entity.getLifeTicks() > 2) {
            EntityParticleData data = entityParticles.computeIfAbsent(entity.getUUID(), k -> new EntityParticleData());
            spawnParticles(entity, partialTicks, data);
            updateParticles(partialTicks, data);
            renderParticles(entity, poseStack, buffer, packedLight, partialTicks, data, petrified);
        }

        cleanupOldEntities();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void cleanupOldEntities() {
        entityParticles.entrySet().removeIf(entry -> entry.getValue().particles.isEmpty());
    }

    private void spawnParticles(MeteorEntity entity, float partialTicks, EntityParticleData data) {
        Vec3 entityPos = entity.position();
        Vec3 motion = entity.getDeltaMovement();
        float size = entity.getSize();

        data.particleAccumulator += partialTicks;

        while (data.particleAccumulator >= 0.25f) {
            data.particleAccumulator -= 0.25f;

            for (int i = 0; i < 4; i++) {
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
                data.particles.add(new ParticleData(
                        entityPos.add(offset), particleVel,
                        size * (0.4f + entity.level().random.nextFloat() * 0.30f),
                        20 + entity.level().random.nextInt(15), ParticleType.FIRE
                ));
            }

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
                data.particles.add(new ParticleData(
                        entityPos.add(offset), particleVel,
                        size * (0.35f + entity.level().random.nextFloat() * 0.25f),
                        35 + entity.level().random.nextInt(25), ParticleType.SMOKE
                ));
            }

            for (int i = 0; i < 3; i++) {
                Vec3 offset = new Vec3(
                        (entity.level().random.nextFloat() - 0.5) * size * 0.5,
                        (entity.level().random.nextFloat() - 0.5) * size * 0.5,
                        (entity.level().random.nextFloat() - 0.5) * size * 0.5
                );
                Vec3 particleVel = motion.scale(-0.15);
                data.particles.add(new ParticleData(
                        entityPos.add(offset), particleVel,
                        size * (0.2f + entity.level().random.nextFloat() * 0.15f),
                        18 + entity.level().random.nextInt(12), ParticleType.TRAIL
                ));
            }
        }
    }

    private void updateParticles(float partialTicks, EntityParticleData data) {
        Iterator<ParticleData> iterator = data.particles.iterator();
        while (iterator.hasNext()) {
            ParticleData particle = iterator.next();
            particle.tick(partialTicks);
            if (particle.isExpired()) iterator.remove();
        }
    }

    private void renderParticles(MeteorEntity entity, PoseStack poseStack, MultiBufferSource buffer,
                                 int packedLight, float partialTicks, EntityParticleData data, boolean petrified) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.itemEntityTranslucentCull(TRAIL_TEXTURE));

        for (ParticleData particle : data.particles) {
            int quadCount = particle.type == ParticleType.SMOKE ? 3 : 1;

            for (int q = 0; q < quadCount; q++) {
                poseStack.pushPose();

                Vec3 relativePos = particle.position.subtract(entity.position());
                poseStack.translate(relativePos.x, relativePos.y, relativePos.z);

                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                float smoothRotation = particle.rotation + (particle.rotationSpeed * partialTicks);
                poseStack.mulPose(Axis.ZP.rotation(smoothRotation + (q * (float)Math.PI / 1.5f)));

                if (particle.type == ParticleType.SMOKE) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(q * 60f));
                }

                Matrix4f matrix = poseStack.last().pose();
                Matrix3f normal = poseStack.last().normal();

                float size = particle.size;
                float alpha = particle.alpha;

                if (particle.type == ParticleType.SMOKE) alpha *= 0.5f;

                int r, g, b, a;
                a = (int)(alpha * 255);

                boolean customColor = entity.isCustomColor();
                float cr = entity.getColorR(), cg = entity.getColorG(), cb = entity.getColorB();

                if (petrified) {
                    r = g = b = 128;
                } else {
                    switch (particle.type) {
                        case FIRE -> {
                            if (customColor) {
                                r = (int) (255 * cr * particle.brightness);
                                g = (int) (255 * cg * particle.brightness);
                                b = (int) (255 * cb * particle.brightness);
                            } else {
                                r = (int) (255 * particle.brightness);
                                g = (int) ((100 + 80 * (1.0f - particle.alpha * 0.5f)) * particle.brightness);
                                b = (int) (30 * (1.0f - particle.alpha) * particle.brightness);
                            }
                            a = (int)(alpha * 240);
                        }
                        case SMOKE -> {
                            int grayValue = (int) (10 + 25 * particle.alpha);
                            r = grayValue;
                            g = grayValue;
                            b = grayValue;
                            a = (int) (alpha * 200);
                        }
                        case TRAIL -> {
                            if (customColor) {
                                r = (int)(100 * cr);
                                g = (int)(Math.min(255, 100 * cg + 155));
                                b = (int)(100 * cb);
                            } else {
                                r = 255;
                                g = 255;
                                b = (int)(200 + 55 * particle.alpha);
                            }
                            a = (int) (alpha * 230);
                        }
                        case EMBER -> {
                            if (customColor) {
                                r = (int)(255 * cr);
                                g = (int)(255 * cg);
                                b = (int)(255 * cb);
                            } else {
                                r = 255;
                                g = (int)(120 + 80 * particle.alpha);
                                b = 40;
                            }
                            a = (int) (alpha * 255);
                        }
                        case SPARK -> {
                            if (customColor) {
                                r = (int)(200 * cr + 55);
                                g = (int)(200 * cg + 55);
                                b = (int)(200 * cb + 55);
                            } else {
                                r = 255;
                                g = 255;
                                b = (int)(180 + 75 * particle.alpha);
                            }
                            a = (int) (alpha * 255);
                        }
                        default -> r = g = b = 255;
                    }
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
                .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, -size,  size, 0).setColor(r, g, b, a)
                .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix,  size,  size, 0).setColor(r, g, b, a)
                .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix,  size, -size, 0).setColor(r, g, b, a)
                .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }

    @Override
    protected int getSkyLightLevel(MeteorEntity entity, BlockPos pos)   { return 15; }
    @Override
    protected int getBlockLightLevel(MeteorEntity entity, BlockPos pos) { return 15; }

    @Override
    public ResourceLocation getTextureLocation(MeteorEntity entity) { return TEXTURE; }
}