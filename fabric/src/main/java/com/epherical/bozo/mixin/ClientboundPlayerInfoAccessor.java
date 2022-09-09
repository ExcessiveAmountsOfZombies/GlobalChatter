package com.epherical.bozo.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientboundPlayerInfoPacket.class)
public interface ClientboundPlayerInfoAccessor {

    @Accessor("entries")
    void setEntries(List<ClientboundPlayerInfoPacket.PlayerUpdate> entries);
}
