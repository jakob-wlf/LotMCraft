package de.jakob.lotm.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoldenNoteParticle extends TextureSheetParticle {
    protected GoldenNoteParticle(ClientLevel level, double x, double y, double z, double color) {
        super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
        this.friction = 0.66F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.xd *= (double)0.01F;
        this.yd *= (double)0.01F;
        this.zd *= (double)0.01F;
        this.yd += 0.2;
        this.quadSize *= 1.5F;
        this.lifetime = 6;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float scaleFactor) {
        return this.quadSize * Mth.clamp(((float)this.age + scaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            GoldenNoteParticle noteparticle = new GoldenNoteParticle(level, x, y, z, xSpeed);
            noteparticle.pickSprite(this.sprite);
            return noteparticle;
        }
    }
}