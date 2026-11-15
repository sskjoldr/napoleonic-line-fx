package com.napoleonic.line.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PanSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected PanSmokeParticle(ClientLevel level, double x, double y, double z,
                               double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.xd = vx;
        this.yd = vy + 0.025D;
        this.zd = vz;
        this.lifetime = 18 + this.random.nextInt(14);
        this.quadSize = 0.12F + this.random.nextFloat() * 0.06F;
        this.rCol = 0.88F;
        this.gCol = 0.88F;
        this.bCol = 0.86F;
        this.alpha = 0.82F;
        this.hasPhysics = false;
        this.pickSprite(this.sprites);
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
        float t = (float) this.age / (float) this.lifetime;
        this.quadSize = Mth.lerp(t, 0.14F, 0.34F);
        this.alpha = 0.82F * (1.0F - t);
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.88D;
        this.yd *= 0.92D;
        this.zd *= 0.88D;
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
            return new PanSmokeParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
