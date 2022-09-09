package com.epherical.bozo.mixin;

import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerGamePacketListenerImpl.class)
public class VerifyMixin {

    @Inject(method = "verifyChatMessage", at = @At("HEAD"))
    public void globalchatter$verification(PlayerChatMessage playerChatMessage, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("Mixin Bozo " + playerChatMessage.signer());
        System.out.println("very mixiny");
    }
}
