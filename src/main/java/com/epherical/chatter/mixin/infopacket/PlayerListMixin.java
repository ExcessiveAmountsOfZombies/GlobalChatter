package com.epherical.chatter.mixin.infopacket;

import com.epherical.chatter.CommonPlatform;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Shadow
    @Final
    private List<ServerPlayer> players;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    public void globalchatter$onPacket(CallbackInfo ci) {
        CommonPlatform.platform.firePlayerInfoEvent(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, this.players));
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    public void globalchatter$placePlayer(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        CommonPlatform.platform.firePlayerInfoEvent(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    public void globallchatter$sendBack(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        CommonPlatform.platform.firePlayerInfoJoin(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this.players), player);
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    public void globalchatter$removePlayer(ServerPlayer player, CallbackInfo ci) {
        CommonPlatform.platform.firePlayerInfoEvent(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player));
    }
}
