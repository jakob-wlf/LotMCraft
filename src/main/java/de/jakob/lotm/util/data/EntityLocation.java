package de.jakob.lotm.util.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityLocation extends Location {

    private Entity entity;

    public EntityLocation(Entity entity) {
        super(entity.position(), entity.level());
        this.entity = entity;
    }

    @Override
    public Vec3 getPosition() {
        return getEntity().position();
    }

    @Override
    public Level getLevel() {
        return getEntity().level();
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
