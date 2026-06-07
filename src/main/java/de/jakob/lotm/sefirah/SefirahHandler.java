package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import de.jakob.lotm.util.data.ServerLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class SefirahHandler {

    public static final String[] implementedSefirah = new String[]{
            "sefirah_castle",
            "river_of_eternal_darkness",
            "empty"
    };

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

        // Grant Sefirot Authority ability to anyone who owns a sefirot
        if (hasSefirot(player)) {
            AbilityWheelHelper.addAbility(player, "sefirot_authority_ability");
            SefirotAuthorityManager.updatePlayerAuthority(player);
        }

        return buff;
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
        AbilityWheelHelper.removeAbility(player, "sefirot_authority_ability");
        AbilityBarHelper.removeAbility(player, "sefirot_authority_ability");
        SefirotAuthorityManager.clearPlayerAuthority(player);
        RiverBlessingManager.clearBlessingsForOwner(player.getUUID());
        RiverBlessingManager.clearAudience(player.server);
        // Clear all ability seals that this sefirot owner had placed
        DeathImprintData.get(player.server).clearAllSealedAbilitiesAndUnapply(player.server);
    }

    public static void teleportToSefirot(ServerPlayer player) {
        teleportToSefirot(player, false);
    }

    /**
     * @param player The player to get the claimed Sefirot for
     * @return the id of the claimed Sefirot or an empty String if none is claimed
     */
    public static String getClaimedSefirot(ServerPlayer player) {
        return SefirotData.get(player.server).getClaimedSefirot(player.getUUID());
    }

    public static void teleportToSefirot(ServerPlayer player, boolean playTeleportEffect) {
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

            if(playTeleportEffect) {
                EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, returnLocation.getPosition().x, returnLocation.getPosition().y, returnLocation.getPosition().z, returnLocation.getLevel());
            }

            return;
        }

        // Set return location
        sefirotData.setLastReturnLocation(player);
        sefirotData.setIsInSefirot(player.getUUID(), true);

        // Teleport to Sefirot
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
                        -57,
                        0,
                        90,
                        0);

                if(playTeleportEffect) {
                    EffectManager.playEffect(EffectManager.Effect.SEFIRAH_CASTLE, 24, -57, 0, sefirotLevel);
                }
            }
            case "river_of_eternal_darkness" -> {
                ServerLevel riverLevel = player.serverLevel().getServer().getLevel(
                        de.jakob.lotm.dimension.ModDimensions.RIVER_OF_ETERNAL_DARKNESS_DIMENSION_KEY);
                if (riverLevel == null) {
                    return;
                }

                player.teleportTo(riverLevel,
                        0,
                        65,
                        -90,
                        0,
                        0);
            }
        }
    }

}
