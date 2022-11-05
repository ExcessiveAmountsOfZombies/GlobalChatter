package com.epherical.chatter.mixin;

import com.epherical.chatter.CommonPlatform;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Function;

@Mixin(PlayerList.class)
public class HeaderPacketMixin {

    @Shadow
    @Final
    private MinecraftServer server;


    @Inject(method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V",
            at = @At("HEAD"))
    public void globalchatter$broadcastChat(Component message, Function<ServerPlayer, Component> predicate, ChatType type, UUID fromUUID, CallbackInfo ci) {
        CommonPlatform.platform.fireBroadcastChat(message, type, fromUUID);
    }
}
