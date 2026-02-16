package de.jakob.lotm.util.shapeShifting.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin(WalkAnimationState.class)
public interface LimbAnimatorAccessor {
    @Accessor("speedOld")
    float getPrevSpeed();

    @Accessor("speedOld")
    void setPrevSpeed(float speed);

    @Accessor("position")
    void setPos(float position);

    @Accessor("speed")
    void setSpeed(float speed);

    @Accessor("speed")
    float getSpeed();
}