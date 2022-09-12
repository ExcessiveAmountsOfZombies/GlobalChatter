package com.epherical.chatter.mixin;

import com.epherical.chatter.CommonPlatform;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class HeaderPacketMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatSender;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"))
    public void globalchatter$broadcastMessageEvent(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, ServerPlayer serverPlayer, ChatSender chatSender, ChatType.Bound bound, CallbackInfo ci) {
        CommonPlatform.platform.fireChatHeader(playerChatMessage.signedBody().hash().asBytes(),
                playerChatMessage.signedHeader(), playerChatMessage.headerSignature());
    }


    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatSender;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/PlayerChatMessage;isFullyFiltered()Z"))
    public void globalchatter$broadcastChat(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, ServerPlayer serverPlayer, ChatSender chatSender, ChatType.Bound bound, CallbackInfo ci) {
        RegistryAccess registryAccess = this.server.registryAccess();
        ChatType.BoundNetwork boundNetwork = bound.toNetwork(registryAccess);
        CommonPlatform.platform.fireBroadcastChat(playerChatMessage, boundNetwork);
    }


}
