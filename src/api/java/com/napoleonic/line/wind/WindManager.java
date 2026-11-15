package com.napoleonic.line.wind;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Deterministic global wind calculation based purely on world game time (level.getGameTime()).
 * Guarantees identical wind direction and strength across all clients without network packets.
 */
public final class WindManager {
    private static long cachedGameTime = Long.MIN_VALUE;
    private static double cachedWindX = 0.0D;
    private static double cachedWindZ = 0.0D;

    /**
     * Returns horizontal wind velocity in blocks/tick for the given world gameTime.
     */
    public static Vec3 getWindVector(Level level, double multiplier) {
        if (level == null || multiplier <= 0.0D) {
            return Vec3.ZERO;
        }
        long gameTime = level.getGameTime();
        updateCache(gameTime);
        return new Vec3(cachedWindX * multiplier, 0.0D, cachedWindZ * multiplier);
    }

    public static double getWindX(long gameTime, double multiplier) {
        updateCache(gameTime);
        return cachedWindX * multiplier;
    }

    public static double getWindZ(long gameTime, double multiplier) {
        updateCache(gameTime);
        return cachedWindZ * multiplier;
    }

    private static void updateCache(long gameTime) {
        // Recompute every 4 ticks since wind changes slowly over minutes
        long quantized = gameTime & ~3L;
        if (quantized == cachedGameTime) {
            return;
        }
        cachedGameTime = quantized;

        // Smooth layered trigonometric waves over ~3-10 minute periods (3600..12000 ticks)
        float t1 = (float) (quantized * 0.00055D);
        float t2 = (float) (quantized * 0.00135D + 1.7D);
        float t3 = (float) (quantized * 0.00023D + 4.1D);

        float angle = (Mth.sin(t1) * 1.8F) + (Mth.cos(t2) * 0.85F) + (Mth.sin(t3) * 1.15F);
        // Wind strength between 0.012 and 0.038 blocks/tick (~0.24 to 0.76 blocks/sec)
        float strength = 0.015F + 0.012F * (0.5F + 0.5F * Mth.sin(t2 * 0.7F + Mth.cos(t3)));

        cachedWindX = Mth.cos(angle) * strength;
        cachedWindZ = Mth.sin(angle) * strength;
    }

    private WindManager() {
    }
}
