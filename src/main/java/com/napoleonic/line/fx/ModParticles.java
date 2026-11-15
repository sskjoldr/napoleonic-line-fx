package com.napoleonic.line.fx;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, NapoleonicLineFxMod.MODID);

    public static final RegistryObject<SimpleParticleType> MUSKET_SMOKE =
            PARTICLE_TYPES.register("musket_smoke", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> MUZZLE_FLASH =
            PARTICLE_TYPES.register("muzzle_flash", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> PAN_SMOKE =
            PARTICLE_TYPES.register("pan_smoke", () -> new SimpleParticleType(true));

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }

    private ModParticles() {
    }
}
