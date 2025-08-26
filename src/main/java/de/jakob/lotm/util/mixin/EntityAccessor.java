package de.jakob.lotm.util.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("DATA_SHARED_FLAGS_ID")
    static EntityDataAccessor<Byte> getSharedFlagsId() {
        throw new AssertionError(); // Mixin replaces this at runtime
    }
}
