package com.epherical.chatter;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.epherical.chatter.CommonPlatform.HOSTING;

@Mod("globalchatter")
public class ChatterForge {

    private static ChatterForge mod;


    public ChatterForge() {
        mod = this;
        CommonPlatform.create(new ForgePlatform());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonInit);

        //MinecraftForge.EVENT_BUS.register(this);
    }


    private void commonInit(FMLDedicatedServerSetupEvent event) {
        if (HOSTING) {
            MinecraftForge.EVENT_BUS.register(new ForgeHostHandler());
        } else {
            MinecraftForge.EVENT_BUS.register(new ForgeClientHandler());
        }

    }


}
