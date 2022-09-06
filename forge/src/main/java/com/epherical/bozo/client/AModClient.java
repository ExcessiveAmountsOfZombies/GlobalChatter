package com.epherical.bozo.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AModClient {

    private static CommonClient commonClient;

    public static void initClient() {
        commonClient = new CommonClient();
    }

}
