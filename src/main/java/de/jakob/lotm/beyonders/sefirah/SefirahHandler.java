package de.jakob.lotm.beyonders.sefirah;

import com.lowdragmc.photon.client.fx.BlockEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.ServerLocation;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class SefirahHandler {

    public static final String[] implementedSefirah = new String[]{"sefirah_castle", "empty"};

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

        if (buff)
            BeyonderData.playerMap.setSefirot(player.getUUID(), sefirot);

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

    public static String getSefirot(ServerPlayer player){
        return SefirotData.get(player.server).getClaimedSefirot(player.getUUID());
    }

    public static void clearAll(String sefirot, MinecraftServer server){
        SefirotData.get(server).unclaimAllByString(sefirot);
    }

    public static void unclaimSefirot(ServerPlayer player){
        BeyonderData.playerMap.setSefirot(player.getUUID(), "");
        SefirotData.get(player.server).unclaimSefirot(player.getUUID());
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
                ServerLevel level = player.serverLevel();
                Vec3 newPos = level.getServer().overworld().getSharedSpawnPos().getCenter();
                ServerLevel returnLevel = level.getServer().overworld();
                player.teleportTo(returnLevel, newPos.x, newPos.y, newPos.z, 0, 0);

                sefirotData.setIsInSefirot(player.getUUID(), false, "none");
                sefirotData.setLastReturnLocation(player);

                if(playTeleportEffect) {
                    EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, returnLocation.getPosition().x, returnLocation.getPosition().y, returnLocation.getPosition().z, returnLocation.getLevel());
                }

                return;
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
                ResourceKey<Level> sefirotDimension = ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));
                ServerLevel sefirotLevel = player.serverLevel().getServer().getLevel(sefirotDimension);
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
        }
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
                    EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, 24, -57, 0, sefirotLevel);
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
        }
    }

}
