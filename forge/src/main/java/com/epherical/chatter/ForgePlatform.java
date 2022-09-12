package com.epherical.chatter;

import com.epherical.chatter.events.BroadcastChat;
import com.epherical.chatter.events.ChatHeaderEvent;
import com.epherical.chatter.events.GatherPlayerInfo;
import com.epherical.chatter.events.PlayerInfoJoined;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgePlatform extends CommonPlatform<ForgePlatform> {

    @Override
    public ForgePlatform getPlatform() {
        return this;
    }

    @Override
    public boolean isClientEnvironment() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public boolean isServerEnvironment() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    @Override
    public Path getRootConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void firePlayerInfoEvent(ClientboundPlayerInfoPacket packet) {
        MinecraftForge.EVENT_BUS.post(new GatherPlayerInfo(packet));
    }

    @Override
    public void firePlayerInfoJoin(ClientboundPlayerInfoPacket packet, ServerPlayer player) {
        MinecraftForge.EVENT_BUS.post(new PlayerInfoJoined(packet, player));
    }

    @Override
    public void fireBroadcastChat(PlayerChatMessage message, ChatType.BoundNetwork network) {
        MinecraftForge.EVENT_BUS.post(new BroadcastChat(message, network));
    }

    @Override
    public void fireChatHeader(byte[] bytes, SignedMessageHeader header, MessageSignature signature) {
        MinecraftForge.EVENT_BUS.post(new ChatHeaderEvent(bytes, header, signature));
    }

}
