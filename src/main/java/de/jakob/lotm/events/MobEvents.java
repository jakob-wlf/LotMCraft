package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
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
        else if(!BeyonderData.playerMap.check(npc.getPathway(), npc.get_sequence())){
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }

    }

    private static int npcAmount = 0;

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        var level = event.getLevel();
        if(!(level instanceof ServerLevel serverLevel)) return;

        if(!(event.getEntity() instanceof BeyonderNPCEntity npc)) return;

        BlockPos pos = npc.getOnPos();

        if(!BeyonderData.playerMap.check(npc.getPathway(), npc.get_sequence())){
            event.setCanceled(true);
        }
        else{
            if(!npc.getShouldIgnoreGamerule()){
                if(npcAmount + 1 > level.getGameRules().getInt(ModGameRules.MAX_NPC_AMOUNT)){
                    event.setCanceled(true);
                    return;
                }


            }
        }
    }

    @SubscribeEvent
    public static  void onDeath(LivingDeathEvent event){
        if(!(event.getEntity() instanceof BeyonderNPCEntity npc)) return;
        if(!(npc.level() instanceof ServerLevel serverLevel)) return;

        npcAmount--;
    }

}
