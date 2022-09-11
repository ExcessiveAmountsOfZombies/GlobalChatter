package com.epherical.bozo;

import com.epherical.bozo.mixin.ClientboundPlayerInfoAccessor;
import com.epherical.bozo.packets.HostBoundPlayerChatPacket;
import com.epherical.bozo.packets.HostBoundSystemChatPacket;
import com.epherical.bozo.packets.HostboundPlayerChatHeaderPacket;
import com.epherical.bozo.packets.HostboundPlayerInfoPacket;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerPacketListener implements ServerGamePacketListener {

    private final Connection connection;
    private final MinecraftServer server;

    private Map<GameProfile, ClientboundPlayerInfoPacket.PlayerUpdate> playersFromOtherServers = new HashMap<>();

    public ServerPacketListener(Connection connection, MinecraftServer server) {
        this.connection = connection;
        this.server = server;
    }

    public void handleHostPlayerInfo(HostboundPlayerInfoPacket packet) {
        ClientboundPlayerInfoPacket infoPacket = new ClientboundPlayerInfoPacket(packet.getAction(), Collections.emptyList());
        ClientboundPlayerInfoAccessor accessor = (ClientboundPlayerInfoAccessor) infoPacket;
        accessor.setEntries(packet.getEntries());
        // TODO: we need a packet solution, this only works in one direction.
        if (packet.getAction() != ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
            for (ClientboundPlayerInfoPacket.PlayerUpdate entry : packet.getEntries()) {
                GameProfile profile = entry.getProfile();
                if (profile.getName() != null) {
                    playersFromOtherServers.put(entry.getProfile(), entry);
                }
            }
        } else {
            for (ClientboundPlayerInfoPacket.PlayerUpdate entry : packet.getEntries()) {
                playersFromOtherServers.remove(entry.getProfile());
            }
        }

        server.getPlayerList().broadcastAll((Packet<?>) accessor);
    }

    public void handleHostHeader(HostboundPlayerChatHeaderPacket packet) {
        System.out.println("Previous Signature: " + packet.getHeader().previousSignature());
        System.out.println("Current Signature?: " + packet.getHeaderSignature());
        System.out.println("Sender: " + packet.getHeader().sender());
        ClientboundPlayerChatHeaderPacket headerPacket = new ClientboundPlayerChatHeaderPacket(packet.getHeader(), packet.getHeaderSignature(), packet.getBodyDigest());
        server.getPlayerList().broadcastAll(headerPacket);
    }

    public void handleHostChat(HostBoundPlayerChatPacket packet) {
        ClientboundPlayerChatPacket chatPacket = new ClientboundPlayerChatPacket(packet.getPlayerChatMessage(), packet.getBoundNetwork());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.addPendingMessage(packet.getPlayerChatMessage());
            player.connection.send(chatPacket);
        }
    }

    public void handleHostSystem(HostBoundSystemChatPacket packet) {
        ClientboundSystemChatPacket chatPacket = new ClientboundSystemChatPacket(packet.getComponent(), packet.isOverlay());
        server.getPlayerList().broadcastAll(chatPacket);
    }

    @Override
    public void onDisconnect(Component reason) {

    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void handleAnimate(ServerboundSwingPacket packet) {

    }

    @Override
    public void handleChat(ServerboundChatPacket packet) {

    }

    @Override
    public void handleChatCommand(ServerboundChatCommandPacket serverboundChatCommandPacket) {

    }

    @Override
    public void handleChatPreview(ServerboundChatPreviewPacket serverboundChatPreviewPacket) {

    }

    @Override
    public void handleChatAck(ServerboundChatAckPacket serverboundChatAckPacket) {

    }

    @Override
    public void handleClientCommand(ServerboundClientCommandPacket packet) {

    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket packet) {

    }

    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {

    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket packet) {

    }

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {

    }

    @Override
    public void handleContainerClose(ServerboundContainerClosePacket packet) {

    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {

    }

    @Override
    public void handleInteract(ServerboundInteractPacket packet) {

    }

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket packet) {

    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket packet) {

    }

    @Override
    public void handlePong(ServerboundPongPacket serverboundPongPacket) {

    }

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {

    }

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket packet) {

    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {

    }

    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket packet) {

    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {

    }

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {

    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket packet) {

    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket packet) {

    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket packet) {

    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {

    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {

    }

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {

    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {

    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {

    }

    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {

    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {

    }

    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {

    }

    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {

    }

    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {

    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {

    }

    @Override
    public void handlePickItem(ServerboundPickItemPacket packet) {

    }

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket packet) {

    }

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {

    }

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {

    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket packet) {

    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket packet) {

    }

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQuery packet) {

    }

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) {

    }

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {

    }

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {

    }

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {

    }

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {

    }

    public Map<GameProfile, ClientboundPlayerInfoPacket.PlayerUpdate> getPlayersFromOtherServers() {
        return playersFromOtherServers;
    }
}
