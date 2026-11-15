package com.napoleonic.line.fx;

import com.napoleonic.line.client.MusketShotClientEvent;
import com.napoleonic.line.config.NapoleonicClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public final class FxClient {
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(FxClient::onRegisterParticleProviders);
        MinecraftForge.EVENT_BUS.register(FxClient.class);
        MinecraftForge.EVENT_BUS.addListener(ClientBulletTracerRenderer::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientBulletTracerRenderer::onRenderLevelStage);
    }

    private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.MUSKET_SMOKE.get(), MusketSmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.MUZZLE_FLASH.get(), MuzzleFlashParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PAN_SMOKE.get(), PanSmokeParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onShot(MusketShotClientEvent event) {
        spawnFancy(event.getMuzzleOrigin(), event.getDirection(), event.getPanOrigin());
        event.setCanceled(true);
    }

    private static void spawnFancy(Vec3 muzzleOrigin, Vec3 direction, Vec3 panOrigin) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        RandomSource rng = level.random;
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        ClientBulletTracerRenderer.spawnTracer(muzzleOrigin, normDir);

        if (NapoleonicClientConfig.ENABLE_PAN_SMOKE.get()) {
            for (int i = 0; i < 4; i++) {
                double vx = (rng.nextDouble() - 0.5D) * 0.03D;
                double vy = 0.02D + rng.nextDouble() * 0.03D;
                double vz = (rng.nextDouble() - 0.5D) * 0.03D;
                level.addAlwaysVisibleParticle(
                        ModParticles.PAN_SMOKE.get(), true,
                        panOrigin.x + (rng.nextDouble() - 0.5D) * 0.1D,
                        panOrigin.y + (rng.nextDouble() - 0.5D) * 0.08D,
                        panOrigin.z + (rng.nextDouble() - 0.5D) * 0.1D,
                        vx, vy, vz
                );
            }
        }

        if (NapoleonicClientConfig.ENABLE_MUZZLE_FLASH.get()) {
            for (int i = 0; i < 3; i++) {
                double dist = 0.08D * i;
                level.addAlwaysVisibleParticle(
                        ModParticles.MUZZLE_FLASH.get(), true,
                        muzzleOrigin.x + normDir.x * dist,
                        muzzleOrigin.y + normDir.y * dist,
                        muzzleOrigin.z + normDir.z * dist,
                        normDir.x * 0.08D,
                        normDir.y * 0.08D,
                        normDir.z * 0.08D
                );
            }
        }

        int minCount = NapoleonicClientConfig.SMOKE_PARTICLES_MIN.get();
        int maxCount = Math.max(minCount, NapoleonicClientConfig.SMOKE_PARTICLES_MAX.get());
        int count = minCount + rng.nextInt(maxCount - minCount + 1);

        Vec3 right = new Vec3(-normDir.z, 0.0D, normDir.x);
        if (right.lengthSqr() < 1.0E-4D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = normDir.cross(right).normalize();

        for (int i = 0; i < count; i++) {
            double forwardFrac = rng.nextDouble();
            double coneSpread = 0.08D + forwardFrac * 0.45D;
            double rx = (rng.nextGaussian() * 0.45D) * coneSpread;
            double ry = (rng.nextGaussian() * 0.45D) * coneSpread;

            double forwardDist = 0.15D + forwardFrac * 2.2D;
            double px = muzzleOrigin.x + normDir.x * forwardDist + right.x * rx + up.x * ry;
            double py = muzzleOrigin.y + normDir.y * forwardDist + right.y * rx + up.y * ry;
            double pz = muzzleOrigin.z + normDir.z * forwardDist + right.z * rx + up.z * ry;

            double speed = 0.025D + (1.0D - forwardFrac) * 0.09D;
            double vx = normDir.x * speed + (rng.nextDouble() - 0.5D) * 0.025D;
            double vy = normDir.y * speed * 0.5D + 0.008D + (rng.nextDouble() - 0.5D) * 0.015D;
            double vz = normDir.z * speed + (rng.nextDouble() - 0.5D) * 0.025D;

            level.addAlwaysVisibleParticle(
                    ModParticles.MUSKET_SMOKE.get(), true,
                    px, py, pz, vx, vy, vz
            );
        }
    }

    private FxClient() {
    }
}
