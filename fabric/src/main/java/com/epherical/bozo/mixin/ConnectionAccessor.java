package com.epherical.bozo.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Connection.class)
public interface ConnectionAccessor {

    @Accessor("channel")
    Channel getChannel();

    @Accessor("sentPackets")
    int getsentPackets();

    @Accessor("sentPackets")
    void setSendPackets(int set);
}
