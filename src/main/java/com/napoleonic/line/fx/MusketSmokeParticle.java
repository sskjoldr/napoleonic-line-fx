package com.napoleonic.line.fx;

import com.napoleonic.line.config.NapoleonicClientConfig;
import com.napoleonic.line.wind.WindManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayDeque;

@OnlyIn(Dist.CLIENT)
public class MusketSmokeParticle extends TextureSheetParticle {
    private static final ArrayDeque<MusketSmokeParticle> ACTIVE_PARTICLES = new ArrayDeque<>(1024);

    private final SpriteSet sprites;
    private final float initialQuadSize;
    private final float maxQuadSize;
    private final float baseGrey;
    private boolean fastFading = false;

    protected MusketSmokeParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        double minSec = NapoleonicClientConfig.SMOKE_LIFETIME_MIN_SECONDS.get();
        double maxSec = Math.max(minSec, NapoleonicClientConfig.SMOKE_LIFETIME_MAX_SECONDS.get());
        int minTicks = Math.max(20, (int) (minSec * 20.0D));
        int maxTicks = Math.max(minTicks + 1, (int) (maxSec * 20.0D));
        this.lifetime = minTicks + this.random.nextInt(maxTicks - minTicks + 1);

        this.initialQuadSize = 0.24F + this.random.nextFloat() * 0.14F;
        this.maxQuadSize = 1.05F + this.random.nextFloat() * 0.55F;
        this.quadSize = this.initialQuadSize;

        // Starts warm medium-light grey and lightens to near-white as it expands
        this.baseGrey = 0.72F + this.random.nextFloat() * 0.10F;
        this.rCol = this.baseGrey;
        this.gCol = this.baseGrey;
        this.bCol = this.baseGrey;
        this.alpha = 0.86F;

        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.hasPhysics = true;
        this.friction = 0.90F;

        this.pickSprite(this.sprites);
        registerActiveParticle(this);
    }

    private static void registerActiveParticle(MusketSmokeParticle particle) {
        while (!ACTIVE_PARTICLES.isEmpty() && !ACTIVE_PARTICLES.peekFirst().isAlive()) {
            ACTIVE_PARTICLES.pollFirst();
        }
        int maxAllowed = NapoleonicClientConfig.MAX_TOTAL_SMOKE_PARTICLES.get();
        int overflow = (ACTIVE_PARTICLES.size() + 1) - maxAllowed;
        if (overflow > 0) {
            int accelerated = 0;
            for (MusketSmokeParticle old : ACTIVE_PARTICLES) {
                if (!old.isAlive()) continue;
                if (!old.fastFading) {
                    old.fastFading = true;
                    accelerated++;
                    if (accelerated >= overflow + 8) {
                        break;
                    }
                }
            }
            // Hard cap safety: if still far above cap, expire oldest immediately
            while (ACTIVE_PARTICLES.size() >= maxAllowed + 64) {
                MusketSmokeParticle oldest = ACTIVE_PARTICLES.pollFirst();
                if (oldest != null) {
                    oldest.remove();
                }
            }
        }
        ACTIVE_PARTICLES.addLast(particle);
    }

    public static int getActiveSmokeCount() {
        ACTIVE_PARTICLES.removeIf(p -> !p.isAlive());
        return ACTIVE_PARTICLES.size();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        int ageStep = this.fastFading ? 5 : 1;
        this.age += ageStep;
        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = Mth.clamp((float) this.age / (float) this.lifetime, 0.0F, 1.0F);

        // Grow quickly in first 25% of lifetime to form a thick wall, then continue expanding gently
        float sizeCurve = 1.0F - (1.0F - progress) * (1.0F - progress);
        this.quadSize = Mth.lerp(sizeCurve, this.initialQuadSize, this.maxQuadSize);

        // Lighten color as smoke disperses
        float shade = Mth.lerp(progress, this.baseGrey, 0.96F);
        this.rCol = shade;
        this.gCol = shade;
        this.bCol = shade;

        // Smooth alpha fade out
        if (progress < 0.12F) {
            this.alpha = Mth.lerp(progress / 0.12F, 0.45F, 0.85F);
        } else {
            float fade = (progress - 0.12F) / 0.88F;
            this.alpha = 0.85F * (1.0F - fade * fade);
        }

        // Gentle upward thermal buoyancy + deterministic global wind drift
        this.yd += 0.00085D * (1.0F - 0.6F * progress);
        if (NapoleonicClientConfig.ENABLE_WIND.get()) {
            double windMult = NapoleonicClientConfig.WIND_STRENGTH_MULTIPLIER.get();
            long gameTime = this.level.getGameTime();
            double wx = WindManager.getWindX(gameTime, windMult);
            double wz = WindManager.getWindZ(gameTime, windMult);
            this.xd = Mth.lerp(0.08D, this.xd, wx);
            this.zd = Mth.lerp(0.08D, this.zd, wz);
        }

        this.roll += 0.015F * (1.0F - progress);
        this.move(this.xd, this.yd, this.zd);

        this.xd *= this.friction;
        this.yd *= 0.94D;
        this.zd *= this.friction;
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
            return new MusketSmokeParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
