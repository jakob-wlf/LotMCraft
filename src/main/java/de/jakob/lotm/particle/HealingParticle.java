package de.jakob.lotm.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HealingParticle extends TextureSheetParticle {
    protected HealingParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 0.86F;
        this.xd *= (double)0.01F;
        this.yd *= (double)0.01F;
        this.zd *= (double)0.01F;
        this.yd += 0.1;
        this.quadSize *= 1.5F;
        this.lifetime = 16;
        this.hasPhysics = false;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float scaleFactor) {
        return this.quadSize * Mth.clamp(((float)this.age + scaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class AngryVillagerProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public AngryVillagerProvider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            HealingParticle healingParticle = new HealingParticle(level, x, y + (double)0.5F, z);
            healingParticle.pickSprite(this.sprite);
            healingParticle.setColor(1.0F, 1.0F, 1.0F);
            return healingParticle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            HealingParticle healingParticle = new HealingParticle(level, x, y, z);
            healingParticle.pickSprite(this.sprite);
            return healingParticle;
        }
    }
}
