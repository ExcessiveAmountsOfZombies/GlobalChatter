package com.epherical.chatter.packets.handler;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ListenerPacketHandler implements ClientGamePacketListener {

    private final Connection connection;
    private final MinecraftServer server;

    public ListenerPacketHandler(Connection connection, MinecraftServer server) {
        this.connection = connection;
        this.server = server;
    }

    @Override
    public void handleSystemChat(ClientboundSystemChatPacket packet) {
        server.getPlayerList().broadcastAll(packet);
    }

    @Override
    public void handlePlayerChat(ClientboundPlayerChatPacket packet) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.addPendingMessage(packet.message());
            player.connection.send(packet);
        }
    }

    @Override
    public void handlePlayerChatHeader(ClientboundPlayerChatHeaderPacket clientboundPlayerChatHeaderPacket) {
        server.getPlayerList().broadcastAll(clientboundPlayerChatHeaderPacket);
    }

    @Override
    public void handleChatPreview(ClientboundChatPreviewPacket clientboundChatPreviewPacket) {
        //System.out.println(clientboundChatPreviewPacket.preview());
    }

    @Override
    public void handleSetDisplayChatPreview(ClientboundSetDisplayChatPreviewPacket clientboundSetDisplayChatPreviewPacket) {
        //System.out.println(clientboundSetDisplayChatPreviewPacket.enabled());
    }

    @Override
    public void handleDeleteChat(ClientboundDeleteChatPacket clientboundDeleteChatPacket) {
        server.getPlayerList().broadcastAll(clientboundDeleteChatPacket);
    }

    @Override
    public void handleCustomPayload(ClientboundCustomPayloadPacket packet) {

    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket packet) {
        //System.out.println("DISCONNECT CALLED, PLEASE NO! BAD BAD!");
    }

    @Override
    public void setActionBarText(ClientboundSetActionBarTextPacket packet) {
        server.getPlayerList().broadcastAll(packet);
    }

    @Override
    public void handleCustomSoundEvent(ClientboundCustomSoundPacket packet) {

    }

    @Override
    public void handleServerData(ClientboundServerDataPacket clientboundServerDataPacket) {
        //System.out.println(clientboundServerDataPacket.enforcesSecureChat());
    }

    @Override
    public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket clientboundCustomChatCompletionsPacket) {
        //System.out.println(clientboundCustomChatCompletionsPacket.action());
    }

    @Override
    public void handleAddEntity(ClientboundAddEntityPacket packet) {}

    @Override
    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket packet) {}

    @Override
    public void handleAddObjective(ClientboundSetObjectivePacket packet) {}

    @Override
    public void handleAddPlayer(ClientboundAddPlayerPacket packet) {}

    @Override
    public void handleAnimate(ClientboundAnimatePacket packet) {}

    @Override
    public void handleAwardStats(ClientboundAwardStatsPacket packet) {}

    @Override
    public void handleAddOrRemoveRecipes(ClientboundRecipePacket packet) {}

    @Override
    public void handleBlockDestruction(ClientboundBlockDestructionPacket packet) {}

    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket packet) {}

    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket packet) {}

    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket packet) {}

    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket packet) {}

    @Override
    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket packet) {}

    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket packet) {}

    @Override
    public void handleContainerClose(ClientboundContainerClosePacket packet) {}

    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket packet) {}

    @Override
    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket packet) {}

    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket packet) {}

    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket packet) {}

    @Override
    public void handleEntityEvent(ClientboundEntityEventPacket packet) {}

    @Override
    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket packet) {}

    @Override
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket packet) {}

    @Override
    public void handleExplosion(ClientboundExplodePacket packet) {}

    @Override
    public void handleGameEvent(ClientboundGameEventPacket packet) {}

    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket packet) {}

    @Override
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet) {}

    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket packet) {}

    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket packet) {}

    @Override
    public void handleLogin(ClientboundLoginPacket packet) {}

    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket packet) {}

    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket packet) {}

    @Override
    public void handleParticleEvent(ClientboundLevelParticlesPacket packet) {}

    @Override
    public void handlePing(ClientboundPingPacket packet) {}

    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket packet) {}

    @Override
    public void handlePlayerInfo(ClientboundPlayerInfoPacket packet) {
        server.getPlayerList().broadcastAll(packet);
    }

    @Override
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket) {}

    @Override
    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket packet) {}

    @Override
    public void handleRespawn(ClientboundRespawnPacket packet) {}

    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket packet) {}

    @Override
    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket packet) {}

    @Override
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket packet) {}

    @Override
    public void handleSetEntityData(ClientboundSetEntityDataPacket packet) {}

    @Override
    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket packet) {}

    @Override
    public void handleSetEquipment(ClientboundSetEquipmentPacket packet) {}

    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket packet) {}

    @Override
    public void handleSetHealth(ClientboundSetHealthPacket packet) {}

    @Override
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket packet) {}

    @Override
    public void handleSetScore(ClientboundSetScorePacket packet) {}

    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket packet) {}

    @Override
    public void handleSetTime(ClientboundSetTimePacket packet) {}

    @Override
    public void handleSoundEvent(ClientboundSoundPacket packet) {}

    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket packet) {}

    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket packet) {}

    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket packet) {}

    @Override
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket packet) {}

    @Override
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket packet) {}

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket packet) {}

    @Override
    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket packet) {}

    @Override
    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket packet) {}

    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket packet) {}

    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket packet) {}

    @Override
    public void handleSetCamera(ClientboundSetCameraPacket packet) {}

    @Override
    public void handleInitializeBorder(ClientboundInitializeBorderPacket packet) {}

    @Override
    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket packet) {}

    @Override
    public void handleSetBorderSize(ClientboundSetBorderSizePacket packet) {}

    @Override
    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket packet) {}

    @Override
    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket packet) {}

    @Override
    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket packet) {}

    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket packet) {}

    @Override
    public void handleResourcePack(ClientboundResourcePackPacket packet) {}

    @Override
    public void handleBossUpdate(ClientboundBossEventPacket packet) {}

    @Override
    public void handleItemCooldown(ClientboundCooldownPacket packet) {}

    @Override
    public void handleMoveVehicle(ClientboundMoveVehiclePacket packet) {}

    @Override
    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket packet) {}

    @Override
    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket packet) {}

    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket packet) {}

    @Override
    public void handleCommands(ClientboundCommandsPacket packet) {}

    @Override
    public void handleStopSoundEvent(ClientboundStopSoundPacket packet) {}

    @Override
    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket packet) {}

    @Override
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket packet) {}

    @Override
    public void handleLookAt(ClientboundPlayerLookAtPacket packet) {}

    @Override
    public void handleTagQueryPacket(ClientboundTagQueryPacket packet) {}

    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket packet) {}

    @Override
    public void handleOpenBook(ClientboundOpenBookPacket packet) {}

    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket packet) {}

    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket packet) {}

    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket packet) {}

    @Override
    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundSetSimulationDistancePacket) {}

    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket packet) {}

    @Override
    public void handleBlockChangedAck(ClientboundBlockChangedAckPacket clientboundBlockChangedAckPacket) {}

    @Override
    public void setSubtitleText(ClientboundSetSubtitleTextPacket packet) {}

    @Override
    public void setTitleText(ClientboundSetTitleTextPacket packet) {}

    @Override
    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket packet) {}

    @Override
    public void handleTitlesClear(ClientboundClearTitlesPacket packet) {}

    @Override
    public void onDisconnect(Component reason) {}

    @Override
    public Connection getConnection() {
        return connection;
    }
}
