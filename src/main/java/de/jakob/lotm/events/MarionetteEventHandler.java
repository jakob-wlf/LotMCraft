package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.*;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MarionetteEventHandler {
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && !event.getLevel().isClientSide) {
            MarionetteComponent component = mob.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            
            // Re-add marionette goals if this entity is a marionette
            if (component.isMarionette()) {
                mob.targetSelector.removeAllGoals(goal ->
                        goal instanceof StrollThroughVillageGoal ||
                        goal instanceof BreedGoal ||
                        goal instanceof MoveToBlockGoal ||
                        goal instanceof PanicGoal ||
                        goal instanceof RandomStrollGoal ||
                        goal instanceof TargetGoal
                );

                mob.goalSelector.addGoal(0, new MarionetteFollowGoal(mob));
                mob.goalSelector.addGoal(0, new MarionetteLoadChunksGoal(mob));
                mob.goalSelector.addGoal(1, new MarionetteStayGoal(mob));
                mob.targetSelector.addGoal(0, new MarionetteTargetGoal(mob));
                mob.goalSelector.addGoal(10, new MarionetteLifelinkGoal(mob));
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

        MarionetteComponent component = mob.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if(!component.isMarionette()) {
            return;
        }

        loadChunksAroundEntity(level, mob, 2);
    }
}