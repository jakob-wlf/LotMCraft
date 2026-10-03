package de.jakob.lotm.events.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class AbilityWheelOpenEvent extends Event {
    private final ServerLevel level;
    private final LivingEntity entity;

    public AbilityWheelOpenEvent(ServerLevel serverLevel, LivingEntity entity) {
        this.level = serverLevel;
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ServerLevel getLevel() {
        return level;
    }
}
