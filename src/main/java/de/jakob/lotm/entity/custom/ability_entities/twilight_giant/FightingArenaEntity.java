package de.jakob.lotm.entity.custom.ability_entities.twilight_giant;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class FightingArenaEntity extends TwilightVisualEntity {
    public static final float NATIVE_RADIUS = 10.0F;

    public FightingArenaEntity(EntityType<? extends FightingArenaEntity> type, Level level) {
        super(type, level);
    }
}
