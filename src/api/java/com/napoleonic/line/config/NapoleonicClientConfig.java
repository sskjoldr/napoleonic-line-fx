package com.napoleonic.line.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class NapoleonicClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue SMOKE_PARTICLES_MIN;
    public static final ForgeConfigSpec.IntValue SMOKE_PARTICLES_MAX;
    public static final ForgeConfigSpec.IntValue MAX_TOTAL_SMOKE_PARTICLES;
    public static final ForgeConfigSpec.DoubleValue SMOKE_LIFETIME_MIN_SECONDS;
    public static final ForgeConfigSpec.DoubleValue SMOKE_LIFETIME_MAX_SECONDS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MUZZLE_FLASH;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PAN_SMOKE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WIND;
    public static final ForgeConfigSpec.DoubleValue WIND_STRENGTH_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BULLET_TRACERS;
    public static final ForgeConfigSpec.DoubleValue TRACER_LENGTH;
    public static final ForgeConfigSpec.DoubleValue TRACER_OPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Smoke density, flash, and tracers apply when Napoleonic Line Effects is installed. The main mod uses plain campfire smoke.").push("particles");
        SMOKE_PARTICLES_MIN = builder
                .comment("Minimum number of gunpowder smoke cloud particles spawned per musket shot (default: 20).")
                .defineInRange("smokeParticlesPerShotMin", 20, 1, 100);
        SMOKE_PARTICLES_MAX = builder
                .comment("Maximum number of gunpowder smoke cloud particles spawned per musket shot (default: 40).")
                .defineInRange("smokeParticlesPerShotMax", 40, 1, 150);
        MAX_TOTAL_SMOKE_PARTICLES = builder
                .comment("Global maximum number of active musket smoke particles. When exceeded during large volleys, older particles fade out faster.")
                .defineInRange("maxTotalSmokeParticles", 800, 50, 5000);
        SMOKE_LIFETIME_MIN_SECONDS = builder
                .comment("Minimum lifetime of musket smoke particles in seconds (default: 10.0s).")
                .defineInRange("smokeLifetimeMinSeconds", 10.0D, 1.0D, 60.0D);
        SMOKE_LIFETIME_MAX_SECONDS = builder
                .comment("Maximum lifetime of musket smoke particles in seconds (default: 20.0s).")
                .defineInRange("smokeLifetimeMaxSeconds", 20.0D, 2.0D, 120.0D);
        ENABLE_MUZZLE_FLASH = builder
                .comment("Render bright muzzle flash at the barrel tip on shot.")
                .define("enableMuzzleFlash", true);
        ENABLE_PAN_SMOKE = builder
                .comment("Render priming pan smoke puff at the breech slightly before/during the shot.")
                .define("enablePanSmoke", true);
        ENABLE_WIND = builder
                .comment("Enable deterministic world-time wind drift for gunpowder smoke clouds.")
                .define("enableWind", true);
        WIND_STRENGTH_MULTIPLIER = builder
                .comment("Multiplier for wind drift speed on smoke particles.")
                .defineInRange("windStrengthMultiplier", 1.0D, 0.0D, 5.0D);
        ENABLE_BULLET_TRACERS = builder
                .comment("Render lightweight, non-lagging bullet tracers on musket and arquebus shots.")
                .define("enableBulletTracers", true);
        TRACER_LENGTH = builder
                .comment("Length of each bullet tracer streak in blocks (default: 3.2).")
                .defineInRange("tracerLength", 3.2D, 0.5D, 12.0D);
        TRACER_OPACITY = builder
                .comment("Maximum opacity of bullet tracers (default: 0.68).")
                .defineInRange("tracerOpacity", 0.68D, 0.1D, 1.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private NapoleonicClientConfig() {
    }
}

