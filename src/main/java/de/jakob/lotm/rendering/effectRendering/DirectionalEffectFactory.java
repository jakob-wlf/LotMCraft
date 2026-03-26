package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.rendering.effectRendering.impl.FateSiphoningEffect;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class DirectionalEffectFactory {

    /**
     * Create a directional effect with no time scaling (normal speed).
     */
    public static ActiveDirectionalEffect createEffect(int effectIndex,
                                                       double startX, double startY, double startZ,
                                                       double endX, double endY, double endZ,
                                                       int duration) {
        return createEffect(effectIndex, startX, startY, startZ, endX, endY, endZ, duration, null);
    }

    /**
     * Create a directional effect whose playback speed is continuously driven
     * by the time multiplier at {@code entity}'s position.
     * <p>
     * The midpoint between start and end is used as the reference position for
     * the time-area lookup, matching the visual centre of the effect.
     *
     * @param entity Entity whose position is used for the time lookup.
     *               Pass {@code null} to get the same behaviour as the
     *               no-entity overload.
     */
    public static ActiveDirectionalEffect createEffect(int effectIndex,
                                                       double startX, double startY, double startZ,
                                                       double endX, double endY, double endZ,
                                                       int duration,
                                                       LivingEntity entity) {
        ActiveDirectionalEffect effect = switch (effectIndex) {
            case 0 -> new FateSiphoningEffect(startX, startY, startZ, endX, endY, endZ, duration);
            default -> throw new IllegalArgumentException("Unknown directional effect index: " + effectIndex);
        };

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            // Use the midpoint as the reference location for the time-area lookup.
            double midX = (startX + endX) / 2.0;
            double midY = (startY + endY) / 2.0;
            double midZ = (startZ + endZ) / 2.0;
            effect.setTimeMultiplier(
                    () -> AbilityUtil.getTimeInArea(entity,
                            new Location(new Vec3(midX, midY, midZ), level))
            );
        }

        return effect;
    }
}