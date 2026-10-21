package de.jakob.lotm.entity.custom.npc;

import net.minecraft.world.entity.EntityType;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class ForsWallEntity extends BeyonderNPCEntity{
    public ForsWallEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level, false, "fors_wall", "door", 2, false, false);
    }
}
