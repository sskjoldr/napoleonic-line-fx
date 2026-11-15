package com.napoleonic.line.fx;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NapoleonicLineFxMod.MODID)
public class NapoleonicLineFxMod {
    public static final String MODID = "napoleonic_line_fx";

    @SuppressWarnings("removal")
    public NapoleonicLineFxMod() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModParticles.register(modEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FxClient.init(modEventBus));
    }
}
