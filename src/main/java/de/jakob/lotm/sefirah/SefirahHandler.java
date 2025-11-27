package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.util.data.ServerLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class SefirahHandler {

    private static final String[] implementedSefirah = new String[]{"sefirah_castle"};

    public static boolean claimSefirot(ServerPlayer player, String sefirot) {
        if(!Arrays.asList(implementedSefirah).contains(sefirot)) {
            return false;
        }

        return SefirotData.get(player.server).claimSefirot(player.getUUID(), sefirot);
    }

    public static boolean hasSefirot(ServerPlayer player) {
        return !SefirotData.get(player.server).getClaimedSefirot(player.getUUID()).isEmpty();
    }

    public static void teleportToSefirot(ServerPlayer player) {
        if(!hasSefirot(player)) {
            return;
        }

        SefirotData sefirotData = SefirotData.get(player.server);

        // Teleport back to previous location
        if(sefirotData.isInSefirot(player)) {
            ServerLocation returnLocation = sefirotData.getReturnLocationForPlayer(player);
            if(returnLocation == null) {
                return;
            }

            player.teleportTo(returnLocation.getLevel(), returnLocation.getPosition().x, returnLocation.getPosition().y, returnLocation.getPosition().z, 0, 0);

            sefirotData.setIsInSefirot(player.getUUID(), false);
            return;
        }

        // Set return location
        sefirotData.setLastReturnLocation(player);
        sefirotData.setIsInSefirot(player.getUUID(), true);

        String sefirot = sefirotData.getClaimedSefirot(player.getUUID());
        switch (sefirot) {
            case "sefirah_castle" -> {
                ResourceKey<Level> sefirotDimension = ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sefirah_castle"));
                ServerLevel sefirotLevel = player.serverLevel().getServer().getLevel(sefirotDimension);
                if (sefirotLevel == null) {
                    return;
                }

                player.teleportTo(sefirotLevel,
                        24,
                        59,
                        0,
                        player.getYRot(),
                        player.getXRot());
            }
        }
    }

}
