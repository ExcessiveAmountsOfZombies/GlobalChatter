package com.epherical.bozo.mixin;

import com.epherical.bozo.event.Events;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class HeaderPacketMixin {

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatSender;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("HEAD"))
    public void globalchatter$broadcastMessageEvent(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, ServerPlayer serverPlayer, ChatSender chatSender, ChatType.Bound bound, CallbackInfo ci) {
        Events.HEADER_EVENT.invoker().onBroadcastHeader(playerChatMessage.signedBody().hash().asBytes(),
                playerChatMessage.signedHeader(), playerChatMessage.headerSignature());
    }

}
