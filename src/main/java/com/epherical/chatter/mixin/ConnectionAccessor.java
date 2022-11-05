package com.epherical.chatter.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public interface ConnectionAccessor {

    @Accessor("channel")
    Channel getChannel();

    @Accessor("sentPackets")
    int getSentPackets();

    @Accessor("sentPackets")
    void setSendPackets(int set);
}
