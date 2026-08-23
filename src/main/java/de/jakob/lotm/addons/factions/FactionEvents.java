package de.jakob.lotm.addons.factions;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.command.FactionAdminCommand;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FactionEvents {
    private static int ticks = 0;

    public static Map<ChunkPos, Tuple<Integer, Integer>> warProgress = new HashMap<>();

    public static Map<ChunkPos, List<String>> posToPlayerMap = new ConcurrentHashMap<>();
    public static Map<String, ChunkPos> playerToPosMap = new ConcurrentHashMap<>();

    private static boolean shouldFail(ServerPlayer player, ChunkPos pos) {
        var factions = BeyonderData.factionStorage.getFaction(pos);

        if (factions.isEmpty()) return false;

        if (FactionAdminCommand.opState.contains(player.getUUID())) return false;

        var name = player.getName().getString();

        var nation = factions.stream().filter(obj -> obj.getType() == 1).findFirst().get();
        var churchOP = factions.stream().filter(obj -> obj.getType() == 2).findFirst();
        FactionCore church = null;

        if (churchOP.isPresent()) {
            church = churchOP.get();
        }

        int seq = BeyonderData.playerMap.get(player.getUUID()).get().sequence();

        if (church == null) {
//            var data = BeyonderData.playerMap.get(BeyonderData.playerMap.getKeyByName(nation.getLeader())).get();
//            int leaderSeq = data.pathway().equals("justiciar") ? data.sequence() - 1 : data.sequence();
//
//            if (!nation.isPartOfFaction(name)) {
//                if (seq <= 2 && seq <= leaderSeq) return false;
//            }

            return !nation.canDoAnything(name, pos);
        } else {
//            var data = BeyonderData.playerMap.get(BeyonderData.playerMap.getKeyByName(nation.getLeader())).get();
//            int leaderSeq = data.pathway().equals("justiciar") ? data.sequence() - 1 : data.sequence();
//
//            if (!nation.isPartOfFaction(name) && !church.isPartOfFaction(name)) {
//                if (seq <= 2 && seq <= leaderSeq) return false;
//            }

            int playerLevelNation = nation.getPlayerLevel(name);
            int playerLevelChurch = church.getPlayerLevel(name);
            boolean checkToNation = playerLevelNation >= playerLevelChurch;

            if (checkToNation) {
                return !(playerLevelNation != 1 && church.getClaimLevel(pos) - 3 <= playerLevelNation);
            } else
                return !church.canDoAnything(name, pos);

        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        var pos = player.level().getChunkAt(event.getPos()).getPos();

        if (!shouldFail(player, pos)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var pos = player.level().getChunkAt(event.getPos()).getPos();

        if (!shouldFail(player, pos)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        var pos = player.level().getChunkAt(event.getPos()).getPos();

        if (!shouldFail(player, pos)) return;

        var comp = player.getData(ModAttachments.PARASITE_COMPONENT.get());
        if (comp.isParasiting()) {
            var id = comp.getParasitingUUID();
            var target = level.getEntity(id);

            if (target instanceof ServerPlayer targetPlayer) {
                if (!shouldFail(targetPlayer, pos)) {

                    if (BeyonderData.getPathway(player).equals("error") &&
                            BeyonderData.getSequence(player) == 4) {
                        var ritual = player.getData(ModAttachments.RITUALS.get());
                        ritual.setStage(1);
                    }

                    return;
                }
            }
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        var pos = player.level().getChunkAt(event.getPos()).getPos();

        if (!shouldFail(player, pos)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        List<BlockPos> buff = new LinkedList<>();
        for (var block : event.getAffectedBlocks()) {
            var pos = serverLevel.getChunk(block).getPos();

            if (BeyonderData.factionStorage.isClaimed(pos, 1)
                    || BeyonderData.factionStorage.isClaimed(pos, 2))
                buff.add(block);
        }

        event.getAffectedBlocks().removeAll(buff);
    }

    @SubscribeEvent
    public static void onMobGrief(EntityMobGriefingEvent event) {
        var pos = new ChunkPos(event.getEntity().blockPosition());

        if (BeyonderData.factionStorage.isClaimed(pos, 1)
                || BeyonderData.factionStorage.isClaimed(pos, 2)) {
            event.setCanGrief(false);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ticks++;

        for (var entry : posToPlayerMap.entrySet()) {

            for (var player : entry.getValue()) {
                int type = BeyonderData.factionStorage.isAtWar(player, entry.getKey());
                if (type != 0) {

                    int attackers = 1;
                    int defenders = 0;

                    List<ServerPlayer> attackersList = new LinkedList<>();

                    for (var obj : entry.getValue()) {
                        int value = BeyonderData.factionStorage.isAtWar(obj, entry.getKey());

                        if (value != 0) {
                            attackers += 1;
                            if (value == 1)
                                type = 1;
                            var attacker = event.getServer().overworld().getPlayerByUUID(Objects.requireNonNull(BeyonderData.playerMap.getKeyByName(obj)));
                            if (attacker == null) continue;

                            attackersList.add((ServerPlayer) attacker);

                        } else if (BeyonderData.factionStorage.isPartOfAndClaimed(obj, entry.getKey()))
                            defenders += 1;
                    }

                    var faction = BeyonderData.factionStorage.getFactionIdFromPosType(entry.getKey(), type);

                    var factionObj = BeyonderData.factionStorage.getFaction(faction);
                    if (factionObj == null) {
                        break;
                    }

                    int amount = 0;
                    var onlinePlayers = new LinkedList<String>(List.of(event.getServer().getPlayerNames()));
                    for (var obj : factionObj.getAllPlayers()) {
                        if (onlinePlayers.contains(obj))
                            amount++;
                    }

                    if (factionObj.getAllPlayers().size() / 2 > amount) break;

                    if (defenders != 0) break;

                    int claimedAmount = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dz == 0) continue;

                            if (factionObj.isClaimed(new ChunkPos(entry.getKey().x + dx, entry.getKey().z + dz))) {
                                claimedAmount++;
                            }
                        }
                    }

                    if (claimedAmount == 8) break;

                    if (ticks % (20 * 10 * BeyonderData.factionStorage.getClaimLevel(entry.getKey(), type)) == 0) {
                        Tuple<Integer, Integer> pair;

                        if (warProgress.containsKey(entry.getKey())) {
                            pair = warProgress.get(entry.getKey());
                        } else {
                            pair = new Tuple<>(type, 0);
                        }

                        pair.setA(attackers); //amount of attacks
                        pair.setB(pair.getB() + 1); //progress

                        warProgress.put(entry.getKey(), pair);

                        if (pair.getB() == 100) {
                            var blockpos = entry.getKey().getWorldPosition();
                            BeyonderData.factionStorage.messageEveryoneInFaction(event.getServer().overworld(), faction,
                                    Component.literal("Chunk [x=" + blockpos.getX() + ", z=" + blockpos.getZ() + "] was lost").withStyle(ChatFormatting.RED));

                            messageAll(attackersList, Component.literal("Chunk [x=" + blockpos.getX() + ", z=" + blockpos.getZ() + "] was conquered").withStyle(ChatFormatting.DARK_GREEN));
                            BeyonderData.factionStorage.unclaim(faction, entry.getKey());
                        } else {
                            if (pair.getB() % 25 == 0 || pair.getB() == 1) {
                                var blockpos = entry.getKey().getWorldPosition();
                                BeyonderData.factionStorage.messageEveryoneInFaction(event.getServer().overworld(), faction,
                                        Component.literal("Chunk [x=" + blockpos.getX() + ", z=" + blockpos.getZ() + "] is under attack - " + pair.getB() + "%").withStyle(ChatFormatting.RED));

                                messageAll(attackersList, Component.literal("Chunk [x=" + blockpos.getX() + ", z=" + blockpos.getZ() + "] - " + pair.getB() + "%").withStyle(ChatFormatting.DARK_GREEN));
                            }
                        }

                        if (factionObj.getCore().equals(entry.getKey()) && pair.getB() == 100) {
                            BeyonderData.factionStorage.winWar(factionObj.getAllAtWar(), faction, event.getServer().overworld());
                        }
                    }

                    break;
                }
            }

        }

//        long oneWeekMillis = 7L * 24 * 60 * 60 * 1000;
//        long now = System.currentTimeMillis();
//        List<Integer> toDisband = new LinkedList<>();
//
//        var set = BeyonderData.factionStorage.getLastOnlineEntrySet();
//        for (var obj : set) {
//            if (BeyonderData.factionStorage.isAtAnyWar(obj.getKey()) &&
//                    (now - obj.getValue() >= oneWeekMillis)) {
//                BeyonderData.factionStorage.messageEveryoneInFaction(event.getServer().overworld(), obj.getKey(),
//                        Component.literal("Your faction was disbanded due to inactivity during the war").withStyle(ChatFormatting.RED));
//
//                toDisband.add(obj.getKey());
//            }
//        }
//        for(var obj : toDisband){
//            BeyonderData.factionStorage.disband(obj);
//        }

        for (var obj : warProgress.entrySet()) {
            if (obj.getValue().getA() == 0) {
                warProgress.remove(obj.getKey());
            }
        }
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var pos = player.chunkPosition();
        var id = player.getName().getString();

        if (playerToPosMap.containsKey(id)) {
            var previousPos = playerToPosMap.get(id);

            if (previousPos.equals(pos)) return;

            var plist = posToPlayerMap.get(previousPos);
            plist.remove(id);
            posToPlayerMap.put(previousPos, plist);
        }

        List<String> list;
        if (posToPlayerMap.containsKey(pos)) {
            list = posToPlayerMap.get(pos);
        } else {
            list = new LinkedList<>();
        }

        list.add(id);
        posToPlayerMap.put(pos, list);

        playerToPosMap.put(id, pos);
    }

//    @SubscribeEvent
//    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
//        if (!(event.getEntity() instanceof ServerPlayer player)) return;
//        if (!(player.level() instanceof ServerLevel level)) return;
//
//        var list = BeyonderData.factionStorage.get
//    }
//
//    @SubscribeEvent
//    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
//        if (!(event.getEntity() instanceof ServerPlayer player)) return;
//        if (!(player.level() instanceof ServerLevel level)) return;
//
//        var list = BeyonderData.factionStorage.getPartOfFaction(player.getName().getString());
//        for (var obj : list) {
//            BeyonderData.factionStorage.updateLastOnline(obj.getId(), level);
//        }
//
//    }

    private static void messageAll(List<ServerPlayer> list, Component msg) {
        for (var obj : list) {
            obj.sendSystemMessage(msg);
        }
    }
}