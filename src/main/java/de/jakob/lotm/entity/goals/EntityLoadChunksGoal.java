package de.jakob.lotm.entity.goals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ChunkPos;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class EntityLoadChunksGoal extends Goal {

    private final Mob marionette;
    private static final int RADIUS = 1;
    private final Set<ChunkPos> currentlyForced = new HashSet<>();

    public EntityLoadChunksGoal(Mob marionette) {
        this.marionette = marionette;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public void tick() {
        if (!(marionette.level() instanceof ServerLevel level)) return;

        ChunkPos center = new ChunkPos(marionette.blockPosition());
        Set<ChunkPos> shouldBeForced = new HashSet<>();

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                shouldBeForced.add(pos);
            }
        }

        for (ChunkPos oldPos : currentlyForced) {
            if (!shouldBeForced.contains(oldPos)) {
                level.setChunkForced(oldPos.x, oldPos.z, false);
            }
        }

        for (ChunkPos newPos : shouldBeForced) {
            if (!currentlyForced.contains(newPos)) {
                level.setChunkForced(newPos.x, newPos.z, true);
            }
        }

        currentlyForced.clear();
        currentlyForced.addAll(shouldBeForced);
    }

    @Override
    public void stop() {
        if (!(marionette.level() instanceof ServerLevel level)) return;

        for (ChunkPos pos : currentlyForced) {
            level.setChunkForced(pos.x, pos.z, false);
        }
        currentlyForced.clear();
    }
}

