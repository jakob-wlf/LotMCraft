package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import de.jakob.lotm.network.packets.toServer.RequestSefirotSyncPacket;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.ServerLocation;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;
import java.util.HashMap;
import java.util.UUID;

public class SefirahHandler {

    public static final String[] implementedSefirah = new String[]{
            "sefirah_castle",
            "river_of_eternal_darkness",
            "chaos_sea",
            "brood_hive",
            "city_of_calamity",
            "nation_of_disorder",
            "tenebrous_world",
            "knowledge_moor",
            "key_of_light",
            "!empty"
    };
    private static HashMap<UUID, String> clientSefirotPlayers = new HashMap<>();

    public static boolean claimSefirot(ServerPlayer player, String sefirot) {
        return claimSefirot(player, sefirot, false);
    }

    public static boolean claimSefirot(ServerPlayer player, String sefirot, boolean playClaimEffect) {
        if(!Arrays.asList(implementedSefirah).contains(sefirot)) {
            return false;
        }

        if(sefirot.equals("empty")){
            unclaimSefirot(player);
            return false;
        }

        boolean buff =  SefirotData.get(player.server).claimSefirot(player.getUUID(), sefirot);

        if (buff) {
            BeyonderData.playerMap.setSefirot(player.getUUID(), sefirot);

            SefirotData data = SefirotData.get(player.server);

            // Record the first-ever owner (no-op if already set)
            data.setFirstOwnerIfAbsent(sefirot, player.getUUID());

            // If someone other than the original owner just claimed an imprinted sefirot,
            // apply the initial corruption burst and reset their reduction counter.
            UUID firstOwner = data.getFirstOwner(sefirot);
            if (!player.getUUID().equals(firstOwner)) {
                data.resetCurrentOwnerSeconds(sefirot);
                int imprint = data.getMentalImprint(sefirot);
                if (imprint > 0) {
                    SefirotImprintEventHandler.applyInitialImprintCorruption(player, imprint);
                }
            }
        }

        // Grant Sefirot Authority ability to anyone who owns a sefirot
        if (hasSefirot(player)) {
            removeSefrotInvasionAbility(player);
            AbilityWheelHelper.addAbility(player, "sefirot_authority_ability");
            SefirotAuthorityManager.updatePlayerAuthority(player);
        }

        return buff;
    }

    public static void inviteToSefirot(ServerPlayer player, ServerPlayer invitedPlayer) {
        SefirotData sefirotData = SefirotData.get(player.server);

        String claimedSefirot = sefirotData.getClaimedSefirot(player.getUUID());

        if(!sefirotData.isInSefirot(player, claimedSefirot)) {
            player.sendSystemMessage(Component.translatable("lotm.sefirot.not_in_sefirot").withStyle(ChatFormatting.RED));
            return;
        }

        if(sefirotData.isInSefirot(invitedPlayer)) {
            player.sendSystemMessage(Component.translatable("lotm.sefirot.cannot_reach_player").withStyle(ChatFormatting.RED));
            return;
        }

        sefirotData.inviteToSefirot(player.getUUID(), invitedPlayer.getUUID(), claimedSefirot);

        Component message = Component.translatable("lotm.sefirot.invited", Component.translatable("lotm.sefirot." + claimedSefirot).getString())
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept_sefirot_invite"))
                );

        invitedPlayer.sendSystemMessage(message);

        player.sendSystemMessage(Component.translatable("lotm.sefirot.invite_sent", invitedPlayer.getName().getString()).withStyle(ChatFormatting.GREEN));
    }

    public static boolean isInSefirot(ServerPlayer player, String sefirot) {
        SefirotData sefirotData = SefirotData.get(player.server);

        return sefirotData.isInSefirot(player, sefirot);
    }

    public static void kickOutOfSefirot(ServerPlayer player, ServerPlayer kickedPlayer) {
        SefirotData sefirotData = SefirotData.get(player.server);

        String claimedSefirot = sefirotData.getClaimedSefirot(player.getUUID());

        if(!sefirotData.isInSefirot(player, claimedSefirot)) {
            player.sendSystemMessage(Component.translatable("lotm.sefirot.not_in_sefirot").withStyle(ChatFormatting.RED));
            return;
        }

        if(!sefirotData.isInSefirot(kickedPlayer, claimedSefirot)) {
            player.sendSystemMessage(Component.translatable("lotm.sefirot.target_not_in_sefirot").withStyle(ChatFormatting.RED));
            return;
        }

        leaveSefirah(kickedPlayer, true);
    }

    public static void acceptInvite(ServerPlayer player) {
        SefirotData sefirotData = SefirotData.get(player.server);

        String invitedSefirot = sefirotData.getInvitedSefirot(player.getUUID());

        if(invitedSefirot == null) {

            return;
        }

        sefirotData.acceptInvite(player.getUUID());
        teleportToSefirot(player, invitedSefirot, true);

    }

    public static boolean hasSefirot(ServerPlayer player) {
        return !SefirotData.get(player.server).getClaimedSefirot(player.getUUID()).isEmpty();
    }

    public static void removeSefrotInvasionAbility(ServerPlayer player) {
        AbilityWheelHelper.removeAbility(player, "sefrot_invasion_ability");
        AbilityBarHelper.removeAbility(player, "sefrot_invasion_ability");
    }

    public static String getSefirot(ServerPlayer player){
        return SefirotData.get(player.server).getClaimedSefirot(player.getUUID());
    }

    public static void clearAll(String sefirot, MinecraftServer server){
        SefirotData.get(server).unclaimAllByString(sefirot);
    }

    public static void unclaimSefirot(ServerPlayer player){
        BeyonderData.playerMap.setSefirot(player.getUUID(), "");
        SefirotData.get(player.server).unclaimSefirot(player.getUUID());
        AbilityWheelHelper.removeAbility(player, "sefirot_authority_ability");
        AbilityBarHelper.removeAbility(player, "sefirot_authority_ability");
        SefirotAuthorityManager.clearPlayerAuthority(player);
        RiverBlessingManager.clearBlessingsForOwner(player.getUUID());
        RiverBlessingManager.clearAudience(player.server);
        // Clear all ability seals that this sefirot owner had placed
        DeathImprintData.get(player.server).clearAllSealedAbilitiesAndUnapply(player.server);
    }

    public static void transferOwnership(ServerPlayer winner, @Nullable ServerPlayer loser, String sefirot) {
        if (loser != null && sefirot.equals(getClaimedSefirot(loser))) {
            unclaimSefirot(loser);
        } else {
            SefirotData.get(winner.server).unclaimAllByString(sefirot);
        }
        if (hasSefirot(winner)) {
            unclaimSefirot(winner);
        }
        claimSefirot(winner, sefirot, true);
    }

    public static void teleportToOwnSefirot(ServerPlayer player) {
        teleportToOwnSefirot(player, false);
    }

    /**
     * @param player The player to get the claimed Sefirot for
     * @return the id of the claimed Sefirot or an empty String if none is claimed
     */
    public static String getClaimedSefirot(ServerPlayer player) {
        return SefirotData.get(player.server).getClaimedSefirot(player.getUUID());
    }

    private static boolean isSefirotLevel(ServerLevel level) {
        ResourceKey<Level> sefirotDimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));
        return level.dimension().equals(sefirotDimension);
    }

    public static void handleSefirotKey(ServerPlayer player) {
        SefirotData sefirotData = SefirotData.get(player.server);
        if(sefirotData.isInSefirot(player))  {
            leaveSefirah(player, true);
        }
        else {
            if(!hasSefirot(player)) {
                AbilityUtil.sendActionBar(player, Component.translatable("lotm.sefirot.no_sefirot").withColor(0x942de3));
                return;
            }

            if((player.tickCount - player.getCombatTracker().lastDamageTime) < (20 * 10)) {
                AbilityUtil.sendActionBar(player, Component.translatable("lotm.sefirot.in_combat").withColor(0x942de3));
                return;
            }

            teleportToOwnSefirot(player, true);
        }
    }

    public static void leaveSefirah(ServerPlayer player, boolean playTeleportEffect) {
        SefirotData sefirotData = SefirotData.get(player.server);

        // Teleport back to previous location
        if(sefirotData.isInSefirot(player)) {
            ServerLocation returnLocation = sefirotData.getReturnLocationForPlayer(player);
            if(returnLocation == null) {
                return;
            }

            if(returnLocation.getLevel().dimension().equals(player.level().dimension())) {
                if(isSefirotLevel(player.serverLevel())) {
                    ServerLevel level = player.serverLevel();
                    Vec3 newPos = level.getServer().overworld().getSharedSpawnPos().getCenter();
                    ServerLevel returnLevel = level.getServer().overworld();
                    player.teleportTo(returnLevel, newPos.x, newPos.y, newPos.z, 0, 0);

                    sefirotData.setIsInSefirot(player.getUUID(), false, "none");
                    sefirotData.setLastReturnLocation(player);

                    if (playTeleportEffect) {
                        EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, returnLocation.getPosition().x, returnLocation.getPosition().y, returnLocation.getPosition().z, returnLocation.getLevel());
                    }

                    return;
                }
                else {
                    sefirotData.setIsInSefirot(player.getUUID(), false, "none");
                    sefirotData.setLastReturnLocation(player);
                }
            }

            player.teleportTo(returnLocation.getLevel(), returnLocation.getPosition().x, returnLocation.getPosition().y, returnLocation.getPosition().z, 0, 0);

            sefirotData.setIsInSefirot(player.getUUID(), false, "none");

            boolean isOwner = sefirotData.getClaimedSefirot(player.getUUID()).equals(sefirotData.getClaimedSefirot(player.getUUID()));

            if(playTeleportEffect) {
                playCorrectEffect(BlockPos.containing(returnLocation.getPosition()), sefirotData.getClaimedSefirot(player.getUUID()), isOwner, returnLocation.getLevel());
            }

            return;
        }
    }

    public static void teleportToSefirot(ServerPlayer player, String sefirot, boolean playTeleportEffect) {
        SefirotData sefirotData = SefirotData.get(player.server);

        // Set return location
        sefirotData.setLastReturnLocation(player);
        sefirotData.setIsInSefirot(player.getUUID(), true, sefirot);

        // Teleport to Sefirot
        switch (sefirot) {
            case "sefirah_castle" -> {
                ServerLevel sefirotLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY);
                if (sefirotLevel == null) {
                    return;
                }

                boolean isOwner = sefirotData.getClaimedSefirot(player.getUUID()).equals(sefirot);

                float x = isOwner ? 24.5f : 17.5f;
                int y =  -58;
                float z = 0.5f;
                int yaw = isOwner ? 90 : -90;

                player.teleportTo(sefirotLevel,
                        x,
                        y,
                        z,
                        yaw,
                        0);

                sefirotLevel.setBlockAndUpdate(BlockPos.containing(21, -58, 0), ModBlocks.SEFIRAH_BLOCK.get().defaultBlockState());

                if(playTeleportEffect) {
                    playCorrectEffect(BlockPos.containing(x, y, z), sefirot, isOwner, sefirotLevel);
                }
            }
            case "brood_hive" -> {
                ResourceKey<Level> sefirotDimension = ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "brood_hive"));
                ServerLevel sefirotLevel = player.serverLevel().getServer().getLevel(sefirotDimension);
                if (sefirotLevel == null) {
                    return;
                }

                boolean isOwner = sefirotData.getClaimedSefirot(player.getUUID()).equals(sefirot);

                float x = 127.5f;
                int y = isOwner ? 121 : 118;
                float z = isOwner ? 116.5f : 125.5f;
                int yaw = isOwner ? 0 : -180;

                player.teleportTo(sefirotLevel,
                        x,
                        y,
                        z,
                        yaw,
                        0);

                sefirotLevel.setBlockAndUpdate(BlockPos.containing(127, 122, 119), ModBlocks.SEFIRAH_BLOCK.get().defaultBlockState());

                SefrotInvasionManager.recordOwnerEntry(player);

                if (playTeleportEffect) {
                    playCorrectEffect(BlockPos.containing(x, y, z), sefirot, isOwner, sefirotLevel);
                }
            }
            case "river_of_eternal_darkness" -> {
                ServerLevel riverLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY);
                if (riverLevel == null) {
                    return;
                }

                player.teleportTo(riverLevel,
                        RiverBlessingManager.audienceX,
                        RiverBlessingManager.audienceY,
                        RiverBlessingManager.audienceZ,
                        0,
                        0);
                SefrotInvasionManager.recordOwnerEntry(player);
            }
            case "chaos_sea" -> {
                ServerLevel chaosSeaLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.CHAOS_SEA_DIMENSION_KEY);
                if (chaosSeaLevel == null) {
                    return;
                }

                player.teleportTo(chaosSeaLevel,
                        23568,
                        66,
                        299,
                        -90,
                        0);

                SefrotInvasionManager.recordOwnerEntry(player);

                if (playTeleportEffect) {
                    EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, 23568, 66, 299, chaosSeaLevel);
                }

            }
            case "brood_hive" -> {
                ServerLevel broodHiveLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.BROOD_HIVE_DIMENSION_KEY);
                if (broodHiveLevel == null) {
                    return;
                }

                player.teleportTo(broodHiveLevel,
                        23568,
                        66,
                        299,
                        -90,
                        0);

                SefrotInvasionManager.recordOwnerEntry(player);

                if (playTeleportEffect) {
                    EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, 23568, 66, 299, broodHiveLevel);
                }
            }
            case "city_of_calamity" -> {
                ServerLevel cityLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.CITY_OF_CALAMITY_DIMENSION_KEY);
                if (cityLevel == null) {
                    return;
                }

                player.teleportTo(cityLevel,
                        23568,
                        66,
                        299,
                        -90,
                        0);

                SefrotInvasionManager.recordOwnerEntry(player);

                if (playTeleportEffect) {
                    EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, 23568, 66, 299, cityLevel);
                }
            }
            case "nation_of_disorder" -> {
                ServerLevel nationLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.NATION_OF_DISORDER_DIMENSION_KEY);
                if (nationLevel == null) {
                    return;
                }

                player.teleportTo(nationLevel,
                        23568,
                        66,
                        299,
                        -90,
                        0);

                SefrotInvasionManager.recordOwnerEntry(player);

                if (playTeleportEffect) {
                    EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, 23568, 66, 299, nationLevel);
                }
            }
            case "tenebrous_world" -> {
                ServerLevel tenebrousWorldLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.TENEBROUS_WORLD_DIMENSION_KEY);
                if (tenebrousWorldLevel == null) {
                    return;
                }

                player.teleportTo(tenebrousWorldLevel,
                        23568,
                        66,
                        299,
                        -90,
                        0);

                SefrotInvasionManager.recordOwnerEntry(player);

                if (playTeleportEffect) {
                    EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, 23568, 66, 299, tenebrousWorldLevel);
                }
            }
            case "knowledge_moor" -> {
                ServerLevel knowledgeMoorLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.KNOWLEDGE_MOOR_DIMENSION_KEY);
                if (knowledgeMoorLevel == null) {
                    return;
                }

                player.teleportTo(knowledgeMoorLevel,
                        23568,
                        66,
                        299,
                        -90,
                        0);

                SefrotInvasionManager.recordOwnerEntry(player);

                if (playTeleportEffect) {
                    EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, 23568, 66, 299, knowledgeMoorLevel);
                }
            }
            case "key_of_light" -> {
                ServerLevel keyOfLightLevel = player.serverLevel().getServer().getLevel(
                        ModDimensions.KEY_OF_LIGHT_DIMENSION_KEY);
                if (keyOfLightLevel == null) {
                    return;
                }

                player.teleportTo(keyOfLightLevel,
                        23568,
                        66,
                        299,
                        -90,
                        0);

                SefrotInvasionManager.recordOwnerEntry(player);

                if (playTeleportEffect) {
                    EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, 23568, 66, 299, keyOfLightLevel);
                }
            }
        }
    }

    /**
     * Returns the dimension key for the given sefirot, or null if it has no associated dimension.
     */
    @Nullable
    public static ResourceKey<Level> getSefirotDimensionKey(String sefirot) {
        if (sefirot == null || sefirot.isEmpty()) return null;
        return switch (sefirot) {
            case "sefirah_castle"            -> ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY;
            case "chaos_sea"                 -> ModDimensions.CHAOS_SEA_DIMENSION_KEY;
            case "river_of_eternal_darkness" -> ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY;
            case "brood_hive"                -> ModDimensions.BROOD_HIVE_DIMENSION_KEY;
            case "city_of_calamity"          -> ModDimensions.CITY_OF_CALAMITY_DIMENSION_KEY;
            case "nation_of_disorder"        -> ModDimensions.NATION_OF_DISORDER_DIMENSION_KEY;
            case "tenebrous_world"           -> ModDimensions.TENEBROUS_WORLD_DIMENSION_KEY;
            case "knowledge_moor"            -> ModDimensions.KNOWLEDGE_MOOR_DIMENSION_KEY;
            case "key_of_light"              -> ModDimensions.KEY_OF_LIGHT_DIMENSION_KEY;
            default                          -> null;
        };
    }

    public static void teleportToOwnSefirot(ServerPlayer player, boolean playTeleportEffect) {
        if(!hasSefirot(player)) {
            return;
        }

        SefirotData sefirotData = SefirotData.get(player.server);

        String claimedSefirot = sefirotData.getClaimedSefirot(player.getUUID());

        teleportToSefirot(player, claimedSefirot, playTeleportEffect);
    }

    public static void playCorrectEffect(BlockPos pos, String sefirot, boolean isOwner, ServerLevel sefirotLevel) {
        switch (sefirot) {
            case "sefirah_castle" -> {
                if(isOwner) {
                    EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, pos.getX(), pos.getY(), pos.getZ(), sefirotLevel);
                }
                else {
                    PacketHandler.sendToAllPlayersInSameLevel(new PlayPhotonBlockEffectPacket(
                            "sefirah_player",
                            pos,
                            0, 1, 0,
                            1.5,
                            null,
                            -1,
                            true,
                            false
                    ), sefirotLevel);

                }
            }
            case "brood_hive" -> {
                PacketHandler.sendToAllPlayersInSameLevel(new PlayPhotonBlockEffectPacket(
                        "brood_hive_player",
                        pos,
                        0, 1, 0,
                        1.5,
                        null,
                        -1,
                        true,
                        false
                ), sefirotLevel);
            }
        }
    }

    public static void syncPlayerSefirotToClient(Player player, String sefirot) {
        if(player instanceof ServerPlayer) return;
        clientSefirotPlayers.put(player.getUUID(), sefirot);
    }

    public static int getSefirotProgress(Player player) {
        if(!(player instanceof ServerPlayer)) {
            PacketHandler.sendToServer(new RequestSefirotSyncPacket());
        }
        String sefirot = player instanceof ServerPlayer ? getClaimedSefirot((ServerPlayer) player) : clientSefirotPlayers.get(player.getUUID());
        if(sefirot == null || sefirot.isEmpty() || !Arrays.asList(implementedSefirah).contains(sefirot.toLowerCase())) {
            return 0;
        }

        return switch (BeyonderData.getSequence(player)) {
            case 4 -> 2;
            case 3, 2 -> 3;
            case 1, 0 -> 4;
            default -> 1;
        };
    }

    public static String[] getAdditionalPathwaysForPlayer(Player player) {
        if(getSefirotProgress(player) < 3) {
            return new String[]{};
        }

        if(!(player instanceof ServerPlayer serverPlayer)) {
            PacketHandler.sendToServer(new RequestSefirotSyncPacket());
            if(clientSefirotPlayers.containsKey(player.getUUID())) {
                String claimedSefirot = clientSefirotPlayers.get(player.getUUID());
                return getPathwaysForSefirot(claimedSefirot);
            }
            return new String[]{};
        }

        String claimedSefirot = getClaimedSefirot(serverPlayer);
        return getPathwaysForSefirot(claimedSefirot);
    }

    public static String[] getPathwaysForSefirot(String sefirot) {
        switch (sefirot) {
            case "sefirah_castle" -> {
                return new String[]{"fool", "door", "error"};
            }
            case "brood_hive" -> {
                return new String[]{"mother", "moon"};
            }
            default -> {
                return new String[]{};
            }
        }
    }

}
