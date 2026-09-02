package de.jakob.lotm.addons.rituals.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq5 {
    private static Map<UUID, Set<Integer>> map = new HashMap<>();
    private static final int WEST = 1;
    private static final int EAST = 2;
    private static final int NORTH = 3;
    private static final int SOUTH = 4;

    private static final int WEST_N = 5;
    private static final int EAST_N = 6;
    private static final int NORTH_N = 7;
    private static final int SOUTH_N = 8;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("door") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(player.level().dimension() == Level.OVERWORLD) {
            WorldBorder border = level.getWorldBorder();

            double minX = border.getMinX();
            double maxX = border.getMaxX();
            double minZ = border.getMinZ();
            double maxZ = border.getMaxZ();

            double x = player.getX();
            double z = player.getZ();

            double distanceWest = Math.abs(x - minX);
            double distanceEast = Math.abs(x - maxX);
            double distanceNorth = Math.abs(z - minZ);
            double distanceSouth = Math.abs(z - maxZ);

            double distance = Math.min(
                    Math.min(distanceWest, distanceEast),
                    Math.min(distanceNorth, distanceSouth)
            );

            if (!map.containsKey(player.getUUID())) {
                map.put(player.getUUID(), new HashSet<>());
            }

            if (distance <= 1.0) {
                if (distanceWest == distance) {
                    var set = map.get(player.getUUID());
                    set.add(WEST);
                    map.put(player.getUUID(), set);
                } else if (distanceEast == distance) {
                    var set = map.get(player.getUUID());
                    set.add(EAST);
                    map.put(player.getUUID(), set);
                } else if (distanceNorth == distance) {
                    var set = map.get(player.getUUID());
                    set.add(NORTH);
                    map.put(player.getUUID(), set);
                } else {
                    var set = map.get(player.getUUID());
                    set.add(SOUTH);
                    map.put(player.getUUID(), set);
                }
            }
        }
        else if(player.level().dimension() == Level.NETHER) {
            WorldBorder border = level.getWorldBorder();

            double minX = border.getMinX();
            double maxX = border.getMaxX();
            double minZ = border.getMinZ();
            double maxZ = border.getMaxZ();

            double x = player.getX();
            double z = player.getZ();

            double distanceWest = Math.abs(x - minX);
            double distanceEast = Math.abs(x - maxX);
            double distanceNorth = Math.abs(z - minZ);
            double distanceSouth = Math.abs(z - maxZ);

            double distance = Math.min(
                    Math.min(distanceWest, distanceEast),
                    Math.min(distanceNorth, distanceSouth)
            );

            if (!map.containsKey(player.getUUID())) {
                map.put(player.getUUID(), new HashSet<>());
            }

            if (distance <= 1.0) {
                if (distanceWest == distance) {
                    var set = map.get(player.getUUID());
                    set.add(WEST_N);
                    map.put(player.getUUID(), set);
                } else if (distanceEast == distance) {
                    var set = map.get(player.getUUID());
                    set.add(EAST_N);
                    map.put(player.getUUID(), set);
                } else if (distanceNorth == distance) {
                    var set = map.get(player.getUUID());
                    set.add(NORTH_N);
                    map.put(player.getUUID(), set);
                } else {
                    var set = map.get(player.getUUID());
                    set.add(SOUTH_N);
                    map.put(player.getUUID(), set);
                }
            }
        }

        if(map.get(player.getUUID()).size() >= 8){
            component.setCompleted(true);
            map.remove(player.getUUID());
        }
    }


}
