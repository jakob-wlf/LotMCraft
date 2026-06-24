package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MobEvents {

    @SubscribeEvent
    public static void onMobSpawnCheck(MobSpawnEvent.PositionCheck event) {
        var level = event.getLevel();
        if(!(level instanceof ServerLevel serverLevel)) return;

        if(!(event.getEntity() instanceof BeyonderNPCEntity npc)) return;

        BlockPos pos = npc.getOnPos();

        var biomeKey = level.getBiome(pos).unwrapKey();

        if (biomeKey.isPresent()){
            for(var biom : Config.biomes){
             if(biom.equals(biomeKey.get().location()))
                 event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
            }
        }
        else if(!BeyonderData.playerMap.check(npc.getPathway(), npc.getSequence())){
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }

    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        var level = event.getLevel();
        if(!(level instanceof ServerLevel serverLevel)) return;

        if(!(event.getEntity() instanceof BeyonderNPCEntity npc)) return;

        BlockPos pos = npc.getOnPos();

        if(!BeyonderData.playerMap.check(npc.getPathway(), npc.getSequence())){
            event.setCanceled(true);
        }
    }

}
