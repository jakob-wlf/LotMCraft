package de.jakob.lotm.util.shapeShifting;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ShapeShiftComponent;
import net.minecraft.world.entity.player.Player;

// de/jakob/lotm/util/shapeShifting/ShapeShiftAccess.java
public final class ShapeShiftAccess {
    private ShapeShiftAccess() {}

    public static ShapeShiftComponent getShapeShift(Player player) {
        return player.getData(ModAttachments.SHAPE_SHIFT);
    }
}