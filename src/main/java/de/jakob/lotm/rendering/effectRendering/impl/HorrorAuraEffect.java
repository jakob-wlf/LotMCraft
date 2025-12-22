package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.rendering.effectRendering.ActiveMovableEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class HorrorAuraEffect extends ActiveMovableEffect {

    private final RandomSource random = RandomSource.create();
    private final List<ShadowTendril> shadowTendrils = new ArrayList<>();
    private final List<DarkParticle> darkParticles = new ArrayList<>();
    private final List<HorrorEye> horrorEyes = new ArrayList<>();
    private final List<TwistedForm> twistedForms = new ArrayList<>();
    
    private float intensity;
    private float pulsePhase;
    private static final float AURA_RADIUS = 4f;

    public HorrorAuraEffect(Location location, int maxDuration, boolean infinite) {
        super(location, maxDuration, infinite);
        
        // Initialize shadow tendrils reaching out
        for (int i = 0; i < 12; i++) {
            shadowTendrils.add(new ShadowTendril());
        }
        
        // Initialize dark particles swirling around
        for (int i = 0; i < 80; i++) {
            darkParticles.add(new DarkParticle());
        }
        
        // Initialize horror eyes appearing in the darkness
        for (int i = 0; i < 8; i++) {
            horrorEyes.add(new HorrorEye());
        }
        
        // Initialize twisted shadowy forms
        for (int i = 0; i < 6; i++) {
            twistedForms.add(new TwistedForm());
        }
    }

    @Override
    protected void render(PoseStack poseStack, float tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float progress = getProgress();
        
        // For infinite effects, use a cycling intensity
        if (infinite) {
            intensity = 0.8f + 0.2f * Mth.sin(tick * 0.05f);
        } else {
            // Fade in, stay, fade out
            if (progress < 0.1f) {
                intensity = progress / 0.1f;
            } else if (progress > 0.85f) {
                intensity = 1f - ((progress - 0.85f) / 0.15f);
            } else {
                intensity = 1f;
            }
        }
        
        pulsePhase = Mth.sin(tick * 0.15f);
        intensity *= (0.9f + 0.1f * pulsePhase);

        poseStack.pushPose();
        poseStack.translate(getX(), getY(), getZ());

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Render base dark aura
        renderDarkAura(consumer, matrix, tick);
        
        // Render shadow tendrils
        for (ShadowTendril tendril : shadowTendrils) {
            tendril.update(tick, intensity);
            renderShadowTendril(consumer, matrix, tendril);
        }
        
        // Render twisted forms
        for (TwistedForm form : twistedForms) {
            form.update(tick, intensity);
            renderTwistedForm(consumer, matrix, form);
        }
        
        // Render dark particles
        for (DarkParticle particle : darkParticles) {
            particle.update(tick, intensity);
            if (particle.alpha > 0f) {
                renderBillboardQuad(consumer, matrix, particle.x, particle.y, particle.z,
                    particle.size, particle.r, particle.g, particle.b, particle.alpha);
            }
        }
        
        // Render horror eyes
        for (HorrorEye eye : horrorEyes) {
            eye.update(tick, intensity);
            if (eye.alpha > 0f) {
                renderHorrorEye(consumer, matrix, eye);
            }
        }
        
        // Render pulsing darkness waves
        renderDarknessWaves(consumer, matrix, tick);

        poseStack.popPose();
    }

    private void renderDarkAura(VertexConsumer consumer, Matrix4f matrix, float tick) {
        // Multiple layers of dark smoke-like rings
        int layers = 4;
        for (int layer = 0; layer < layers; layer++) {
            float layerOffset = layer * 0.5f;
            float radius = AURA_RADIUS * (0.6f + layer * 0.2f);
            float heightOffset = -0.5f + layer * 0.3f;
            float rotation = tick * 0.02f * (layer % 2 == 0 ? 1f : -1f);
            
            int segments = 32;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments) + rotation;
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments) + rotation;
                
                float wave1 = Mth.sin(angle1 * 3f + tick * 0.1f) * 0.2f;
                float wave2 = Mth.sin(angle2 * 3f + tick * 0.1f) * 0.2f;
                
                float x1 = Mth.cos(angle1) * (radius + wave1);
                float z1 = Mth.sin(angle1) * (radius + wave1);
                float x2 = Mth.cos(angle2) * (radius + wave2);
                float z2 = Mth.sin(angle2) * (radius + wave2);
                
                float y = heightOffset + Mth.sin(angle1 * 2f + tick * 0.15f) * 0.3f;
                
                // Dark colors with slight red tint
                float r = 0.15f + layer * 0.05f;
                float g = 0.05f;
                float b = 0.1f + layer * 0.03f;
                float alpha = intensity * 0.4f * (1f - layer * 0.15f);
                
                addVertex(consumer, matrix, 0f, heightOffset, 0f, r * 0.5f, g * 0.5f, b * 0.5f, alpha * 0.2f);
                addVertex(consumer, matrix, x1, y, z1, r, g, b, alpha);
                addVertex(consumer, matrix, x2, y, z2, r, g, b, alpha);
                addVertex(consumer, matrix, 0f, heightOffset, 0f, r * 0.5f, g * 0.5f, b * 0.5f, alpha * 0.2f);
            }
        }
    }

    private void renderShadowTendril(VertexConsumer consumer, Matrix4f matrix, ShadowTendril tendril) {
        if (tendril.alpha <= 0f) return;
        
        int segments = 20;
        for (int i = 0; i < segments - 1; i++) {
            float t1 = i / (float) segments;
            float t2 = (i + 1) / (float) segments;
            
            Vec3 pos1 = tendril.getPositionAtT(t1);
            Vec3 pos2 = tendril.getPositionAtT(t2);
            
            float width = tendril.width * (1f - t1 * 0.7f) * 0.15f;
            
            Vec3 dir = pos2.subtract(pos1).normalize();
            Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(width);
            
            float alpha = tendril.alpha * (1f - t1 * 0.5f);
            float darkness = 1f - t1 * 0.3f;
            
            addVertex(consumer, matrix,
                (float) (pos1.x - perp.x), (float) (pos1.y - perp.y), (float) (pos1.z - perp.z),
                0.1f * darkness, 0.02f * darkness, 0.08f * darkness, alpha);
            addVertex(consumer, matrix,
                (float) (pos1.x + perp.x), (float) (pos1.y + perp.y), (float) (pos1.z + perp.z),
                0.1f * darkness, 0.02f * darkness, 0.08f * darkness, alpha);
            addVertex(consumer, matrix,
                (float) (pos2.x + perp.x), (float) (pos2.y + perp.y), (float) (pos2.z + perp.z),
                0.08f * darkness, 0.01f * darkness, 0.06f * darkness, alpha * 0.8f);
            addVertex(consumer, matrix,
                (float) (pos2.x - perp.x), (float) (pos2.y - perp.y), (float) (pos2.z - perp.z),
                0.08f * darkness, 0.01f * darkness, 0.06f * darkness, alpha * 0.8f);
        }
    }

    private void renderTwistedForm(VertexConsumer consumer, Matrix4f matrix, TwistedForm form) {
        if (form.alpha <= 0f) return;
        
        // Render as a distorted vertical shape
        int segments = 16;
        float height = 2f;
        
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);
            
            for (int h = 0; h < 4; h++) {
                float y1 = (h / 4f) * height + form.y;
                float y2 = ((h + 1) / 4f) * height + form.y;
                
                float twist1 = form.twistPhase + y1 * 2f;
                float twist2 = form.twistPhase + y2 * 2f;
                
                float radius1 = form.radius * (1f - h * 0.15f);
                float radius2 = form.radius * (1f - (h + 1) * 0.15f);
                
                float x1 = form.x + Mth.cos(angle1 + twist1) * radius1;
                float z1 = form.z + Mth.sin(angle1 + twist1) * radius1;
                float x2 = form.x + Mth.cos(angle2 + twist1) * radius1;
                float z2 = form.z + Mth.sin(angle2 + twist1) * radius1;
                
                float x3 = form.x + Mth.cos(angle2 + twist2) * radius2;
                float z3 = form.z + Mth.sin(angle2 + twist2) * radius2;
                float x4 = form.x + Mth.cos(angle1 + twist2) * radius2;
                float z4 = form.z + Mth.sin(angle1 + twist2) * radius2;
                
                float alpha = form.alpha * (1f - h * 0.2f);
                
                addVertex(consumer, matrix, x1, y1, z1, 0.08f, 0.02f, 0.06f, alpha);
                addVertex(consumer, matrix, x2, y1, z2, 0.08f, 0.02f, 0.06f, alpha);
                addVertex(consumer, matrix, x3, y2, z3, 0.06f, 0.01f, 0.04f, alpha * 0.8f);
                addVertex(consumer, matrix, x4, y2, z4, 0.06f, 0.01f, 0.04f, alpha * 0.8f);
            }
        }
    }

    private void renderHorrorEye(VertexConsumer consumer, Matrix4f matrix, HorrorEye eye) {
        // Render glowing eye as two quads (pupil + glow)
        float eyeSize = eye.size;
        float glowSize = eyeSize * 1.8f;
        
        // Glow layer
        renderBillboardQuad(consumer, matrix, eye.x, eye.y, eye.z, glowSize,
            0.4f, 0.05f, 0.05f, eye.alpha * 0.3f);
        
        // Eye layer
        renderBillboardQuad(consumer, matrix, eye.x, eye.y, eye.z, eyeSize,
            0.8f, 0.1f, 0.1f, eye.alpha * eye.brightness);
    }

    private void renderDarknessWaves(VertexConsumer consumer, Matrix4f matrix, float tick) {
        // Expanding waves of darkness
        int waveCount = 3;
        for (int w = 0; w < waveCount; w++) {
            float wavePhase = (tick * 0.05f + w * 0.33f) % 1f;
            float waveRadius = AURA_RADIUS * wavePhase;
            float waveAlpha = intensity * 0.3f * Mth.sin(wavePhase * Mth.PI);
            
            int segments = 32;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (i * Math.PI * 2 / segments);
                float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);
                
                float x1 = Mth.cos(angle1) * waveRadius;
                float z1 = Mth.sin(angle1) * waveRadius;
                float x2 = Mth.cos(angle2) * waveRadius;
                float z2 = Mth.sin(angle2) * waveRadius;
                
                float y = 0.05f;
                
                addVertex(consumer, matrix, 0f, y, 0f, 0.1f, 0.02f, 0.05f, waveAlpha * 0.5f);
                addVertex(consumer, matrix, x1, y, z1, 0.12f, 0.03f, 0.07f, waveAlpha);
                addVertex(consumer, matrix, x2, y, z2, 0.12f, 0.03f, 0.07f, waveAlpha);
                addVertex(consumer, matrix, 0f, y, 0f, 0.1f, 0.02f, 0.05f, waveAlpha * 0.5f);
            }
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                          float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a);
    }

    private void renderBillboardQuad(VertexConsumer consumer, Matrix4f matrix,
                                     float x, float y, float z, float size,
                                     float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        Vec3 toCamera = new Vec3(
            cameraPos.x - (getX() + x),
            cameraPos.y - (getY() + y),
            cameraPos.z - (getZ() + z)
        ).normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCamera.cross(up).normalize().scale(size);
        up = right.cross(toCamera).normalize().scale(size);

        addVertex(consumer, matrix,
            (float) (x - right.x - up.x), (float) (y - right.y - up.y), (float) (z - right.z - up.z),
            r, g, b, a);
        addVertex(consumer, matrix,
            (float) (x - right.x + up.x), (float) (y - right.y + up.y), (float) (z - right.z + up.z),
            r, g, b, a);
        addVertex(consumer, matrix,
            (float) (x + right.x + up.x), (float) (y + right.y + up.y), (float) (z + right.z + up.z),
            r, g, b, a);
        addVertex(consumer, matrix,
            (float) (x + right.x - up.x), (float) (y + right.y - up.y), (float) (z + right.z - up.z),
            r, g, b, a);
    }

    // Shadow tendrils reaching out from the center
    private class ShadowTendril {
        float angle;
        float elevation;
        float length;
        float width;
        float alpha;
        float waveSpeed;
        float wavePhase;

        ShadowTendril() {
            this.angle = random.nextFloat() * Mth.TWO_PI;
            this.elevation = -0.3f + random.nextFloat() * 0.6f;
            this.length = AURA_RADIUS * (0.8f + random.nextFloat() * 0.4f);
            this.width = 0.8f + random.nextFloat() * 0.5f;
            this.waveSpeed = 0.8f + random.nextFloat() * 0.6f;
            this.wavePhase = random.nextFloat() * Mth.TWO_PI;
        }

        void update(float tick, float intensity) {
            this.alpha = intensity * (0.6f + 0.4f * Mth.sin(tick * 0.1f * waveSpeed + wavePhase));
        }

        Vec3 getPositionAtT(float t) {
            float currentLength = length * t;
            float wave = Mth.sin(t * 5f + currentTick * 0.1f * waveSpeed) * 0.3f;
            
            float x = Mth.cos(angle) * currentLength;
            float y = elevation + wave + t * 0.5f;
            float z = Mth.sin(angle) * currentLength;
            
            return new Vec3(x, y, z);
        }
    }

    // Dark particles swirling around
    private class DarkParticle {
        float x, y, z;
        float orbitRadius;
        float orbitSpeed;
        float orbitAngle;
        float heightOffset;
        float size;
        float alpha;
        float r, g, b;

        DarkParticle() {
            respawn();
        }

        void respawn() {
            this.orbitRadius = random.nextFloat() * AURA_RADIUS;
            this.orbitSpeed = 0.02f + random.nextFloat() * 0.03f;
            this.orbitAngle = random.nextFloat() * Mth.TWO_PI;
            this.heightOffset = -0.5f + random.nextFloat() * 2f;
            this.size = 0.08f + random.nextFloat() * 0.1f;
            
            // Dark colors with occasional red hints
            if (random.nextFloat() > 0.8f) {
                this.r = 0.3f;
                this.g = 0.05f;
                this.b = 0.05f;
            } else {
                this.r = 0.1f;
                this.g = 0.05f;
                this.b = 0.15f;
            }
        }

        void update(float tick, float intensity) {
            this.orbitAngle += orbitSpeed;
            
            float verticalWave = Mth.sin(tick * 0.08f + orbitAngle * 2f) * 0.3f;
            
            this.x = Mth.cos(orbitAngle) * orbitRadius;
            this.y = heightOffset + verticalWave;
            this.z = Mth.sin(orbitAngle) * orbitRadius;
            
            this.alpha = intensity * (0.4f + 0.3f * Mth.sin(tick * 0.15f + orbitAngle));
            this.alpha = Mth.clamp(this.alpha, 0f, 0.7f);
        }
    }

    // Horror eyes that blink in the darkness
    private class HorrorEye {
        float x, y, z;
        float orbitRadius;
        float orbitAngle;
        float orbitSpeed;
        float size;
        float alpha;
        float brightness;
        float blinkTimer;
        float blinkDuration;
        boolean isBlinking;

        HorrorEye() {
            this.orbitRadius = AURA_RADIUS * (0.6f + random.nextFloat() * 0.3f);
            this.orbitAngle = random.nextFloat() * Mth.TWO_PI;
            this.orbitSpeed = 0.01f + random.nextFloat() * 0.015f;
            this.size = 0.12f + random.nextFloat() * 0.08f;
            this.blinkTimer = random.nextFloat() * 100f;
            this.blinkDuration = 10f + random.nextFloat() * 10f;
            this.isBlinking = false;
        }

        void update(float tick, float intensity) {
            this.orbitAngle += orbitSpeed;
            
            this.x = Mth.cos(orbitAngle) * orbitRadius;
            this.y = 0.5f + Mth.sin(orbitAngle * 2f) * 0.3f;
            this.z = Mth.sin(orbitAngle) * orbitRadius;
            
            // Blinking behavior
            blinkTimer++;
            if (!isBlinking && blinkTimer > 60 + random.nextInt(100)) {
                isBlinking = true;
                blinkTimer = 0;
            }
            
            if (isBlinking) {
                if (blinkTimer < 5) {
                    brightness = 1f - (blinkTimer / 5f);
                } else if (blinkTimer < 10) {
                    brightness = (blinkTimer - 5) / 5f;
                } else {
                    isBlinking = false;
                    blinkTimer = 0;
                    brightness = 1f;
                }
            } else {
                brightness = 1f;
            }
            
            this.alpha = intensity * brightness * (0.7f + 0.3f * Mth.sin(tick * 0.2f));
        }
    }

    // Twisted shadowy forms
    private class TwistedForm {
        float x, y, z;
        float radius;
        float alpha;
        float twistPhase;
        float twistSpeed;
        float orbitAngle;
        float orbitRadius;
        float orbitSpeed;

        TwistedForm() {
            this.orbitRadius = AURA_RADIUS * 0.5f;
            this.orbitAngle = random.nextFloat() * Mth.TWO_PI;
            this.orbitSpeed = 0.015f + random.nextFloat() * 0.01f;
            this.radius = 0.3f + random.nextFloat() * 0.2f;
            this.twistSpeed = 0.03f + random.nextFloat() * 0.02f;
        }

        void update(float tick, float intensity) {
            this.orbitAngle += orbitSpeed;
            this.twistPhase += twistSpeed;
            
            this.x = Mth.cos(orbitAngle) * orbitRadius;
            this.y = -0.5f;
            this.z = Mth.sin(orbitAngle) * orbitRadius;
            
            this.alpha = intensity * 0.4f * (0.8f + 0.2f * Mth.sin(tick * 0.1f));
        }
    }
}