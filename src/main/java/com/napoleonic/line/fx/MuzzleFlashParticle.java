package com.napoleonic.line.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MuzzleFlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected MuzzleFlashParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.xd = vx * 0.15D;
        this.yd = vy * 0.15D;
        this.zd = vz * 0.15D;
        this.lifetime = 3 + this.random.nextInt(2);
        this.quadSize = 0.32F + this.random.nextFloat() * 0.12F;
        this.rCol = 1.0F;
        this.gCol = 0.92F;
        this.bCol = 0.72F;
        this.alpha = 0.98F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.hasPhysics = false;
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.setSpriteFromAge(this.sprites);
        float t = (float) this.age / (float) this.lifetime;
        this.quadSize *= 0.85F;
        this.alpha = 1.0F - t * t;
        this.move(this.xd, this.yd, this.zd);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new MuzzleFlashParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
