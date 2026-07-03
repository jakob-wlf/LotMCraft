package de.jakob.lotm.addons.factions;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.LinkedList;
import java.util.List;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FactionEvents {

    private static boolean shouldFail(ServerPlayer player, ChunkPos pos){
        var factions = BeyonderData.factionStorage.getFaction(pos);

        if(factions.isEmpty()) return false;

        if(player.isCreative()) return false;

        var name = player.getName().getString();

        var nation = factions.stream().filter(obj -> obj.getType() == 1).findFirst().get();
        var churchOP = factions.stream().filter(obj -> obj.getType() == 2).findFirst();
        FactionCore church = null;

        if(churchOP.isPresent()){
            church = churchOP.get();
        }

        int seq = BeyonderData.playerMap.get(player.getUUID()).get().sequence();

        if(church == null){
            var data = BeyonderData.playerMap.get(BeyonderData.playerMap.getKeyByName(nation.getLeader())).get();
            int leaderSeq = data.pathway().equals("justiciar") ? data.sequence() - 1 : data.sequence();

            if(!nation.isPartOfFaction(name)){
                if(seq <= 2 && seq <= leaderSeq) return false;
            }

            return !nation.canDoAnything(name, pos);
        }
        else{
            var data = BeyonderData.playerMap.get(BeyonderData.playerMap.getKeyByName(nation.getLeader())).get();
            int leaderSeq = data.pathway().equals("justiciar") ? data.sequence() - 1 : data.sequence();

            if(!nation.isPartOfFaction(name) && !church.isPartOfFaction(name)){
                if(seq <= 2 && seq <= leaderSeq) return false;
            }

            int playerLevelNation = nation.getPlayerLevel(name);
            int playerLevelChurch = church.getPlayerLevel(name);
            boolean checkToNation = playerLevelNation >= playerLevelChurch;

            if(checkToNation){
                return !(playerLevelNation != 1 && church.getClaimLevel(pos) - 3 <= playerLevelNation);
            }
            else
                return !church.canDoAnything(name, pos);

        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if(!(event.getPlayer() instanceof ServerPlayer player)) return;

        var pos = player.level().getChunkAt(event.getPos()).getPos();

        if(!shouldFail(player, pos)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        var pos = player.level().getChunkAt(event.getPos()).getPos();

        if(!shouldFail(player, pos)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        var pos = player.level().getChunkAt(event.getPos()).getPos();

        if(!shouldFail(player, pos)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if(!(event.getPlayer() instanceof ServerPlayer player)) return;

        var pos = player.level().getChunkAt(event.getPos()).getPos();

        if(!shouldFail(player, pos)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if(!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        List<BlockPos> buff = new LinkedList<>();
        for(var block : event.getAffectedBlocks()){
            var pos = serverLevel.getChunk(block).getPos();

            if(BeyonderData.factionStorage.isClaimed(pos, 1)
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
}
