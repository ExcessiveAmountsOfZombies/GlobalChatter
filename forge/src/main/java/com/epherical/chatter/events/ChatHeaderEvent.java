package com.epherical.chatter.events;

import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraftforge.eventbus.api.Event;

public class ChatHeaderEvent extends Event {

    private final byte[] bytes;
    private final SignedMessageHeader header;
    private final MessageSignature signature;

    public ChatHeaderEvent(byte[] bytes, SignedMessageHeader header, MessageSignature signature) {
        this.bytes = bytes;
        this.header = header;
        this.signature = signature;
    }

    public SignedMessageHeader getHeader() {
        return header;
    }

    public MessageSignature getSignature() {
        return signature;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
