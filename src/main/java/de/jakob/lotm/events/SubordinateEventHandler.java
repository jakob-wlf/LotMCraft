package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.SubordinateFollowGoal;
import de.jakob.lotm.entity.custom.goals.SubordinateLoadChunksGoal;
import de.jakob.lotm.entity.custom.goals.SubordinateTargetGoal;
import de.jakob.lotm.entity.custom.goals.SuordinateStayGoal;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import static de.jakob.lotm.util.BeyonderData.beyonderMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SubordinateEventHandler {
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && !event.getLevel().isClientSide) {
            SubordinateComponent component = mob.getData(ModAttachments.SUBORDINATE_COMPONENT.get());

            // Re-add marionette goals if this entity is a marionette
            if (component.isSubordinate()) {
                mob.targetSelector.removeAllGoals(goal ->
                        goal instanceof StrollThroughVillageGoal ||
                                goal instanceof BreedGoal ||
                                goal instanceof MoveToBlockGoal ||
                                goal instanceof PanicGoal ||
                                goal instanceof RandomStrollGoal ||
                                goal instanceof TargetGoal
                );

                mob.goalSelector.addGoal(0, new SubordinateFollowGoal(mob));
                mob.goalSelector.addGoal(0, new SubordinateLoadChunksGoal(mob));
                mob.goalSelector.addGoal(1, new SuordinateStayGoal(mob));
                mob.targetSelector.addGoal(0, new SubordinateTargetGoal(mob));
                mob.setTarget(null);

                if(!(event.getLevel() instanceof ServerLevel serverLevel)) {
                    return;
                }

                loadChunksAroundEntity(serverLevel, mob, 2);
            }
        }
    }

    private static void loadChunksAroundEntity(ServerLevel level, Mob mob, int radius) {
        ChunkPos center = new ChunkPos(mob.blockPosition());

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                level.setChunkForced(pos.x, pos.z, true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if(!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        SubordinateComponent component = mob.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if(!component.isSubordinate()) {
            return;
        }

        loadChunksAroundEntity(level, mob, 2);
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BeyonderData.initBeyonderMap(serverLevel);
        }
    }

}