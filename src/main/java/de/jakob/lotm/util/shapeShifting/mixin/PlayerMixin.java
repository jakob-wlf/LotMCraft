package de.jakob.lotm.util.shapeShifting.mixin;

import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerMixin implements TransformData {
    @Unique
    private String currentShape = null;

    @Override
    public String getCurrentShape() {
        return currentShape;
    }

    @Override
    public void setCurrentShape(String shape) {
        this.currentShape = shape;
    }
}