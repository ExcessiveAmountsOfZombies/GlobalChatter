package com.epherical.chatter.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientboundPlayerInfoPacket.class)
public interface ClientboundPlayerInfoAccessor {

    @Accessor("entries") @Mutable
    void setEntries(List<ClientboundPlayerInfoPacket.PlayerUpdate> entries);
}
