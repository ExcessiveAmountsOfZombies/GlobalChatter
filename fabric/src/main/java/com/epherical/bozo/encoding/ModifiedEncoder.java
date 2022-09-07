package com.epherical.bozo.encoding;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.slf4j.Logger;

/**
 * Modified version of {@link PacketEncoder}
 */
public class ModifiedEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PacketFlow flow;

    public ModifiedEncoder(PacketFlow flow) {
        this.flow = flow;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> msg, ByteBuf out) throws Exception {

    }
}
