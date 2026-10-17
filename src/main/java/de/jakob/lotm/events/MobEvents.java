package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Skeleton;
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

        if((event.getEntity() instanceof BeyonderNPCEntity npc)) {

            BlockPos pos = npc.getOnPos();

            var biomeKey = level.getBiome(pos).unwrapKey();

            if (biomeKey.isPresent()) {
                for (var biom : Config.biomes) {
                    if (biom.equals(biomeKey.get().location()))
                        event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
                }
            } else if (!BeyonderData.playerMap.check(npc.getPathway(), npc.get_sequence())) {
                event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
            }
        } else if(event.getEntity() instanceof Skeleton skeleton) {
            var biomeKey = level.getBiome(skeleton.getOnPos()).unwrapKey();
            if(biomeKey.isPresent() && biomeKey.get().equals(ModDimensions.BIOME_STYX_CHASM))
                event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
        }


    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        var level = event.getLevel();
        if(!(level instanceof ServerLevel serverLevel)) return;

        if(!(event.getEntity() instanceof BeyonderNPCEntity npc)) return;

        BlockPos pos = npc.getOnPos();

        if(!BeyonderData.playerMap.check(npc.getPathway(), npc.get_sequence())){
            event.setCanceled(true);
        }
    }

}
