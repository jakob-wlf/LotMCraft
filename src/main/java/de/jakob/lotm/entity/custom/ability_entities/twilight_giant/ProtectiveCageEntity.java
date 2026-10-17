package de.jakob.lotm.entity.custom.ability_entities.twilight_giant;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ProtectiveCageEntity extends TwilightVisualEntity {
    public static final float NATIVE_HALF = 2.5F;

    public ProtectiveCageEntity(EntityType<? extends ProtectiveCageEntity> type, Level level) {
        super(type, level);
    }
}
