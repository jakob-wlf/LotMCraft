package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.rendering.effectRendering.impl.HorrorAuraEffect;
import de.jakob.lotm.rendering.effectRendering.impl.LifeAuraEffect;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MovableEffectFactory {

    /**
     * Create a movable effect with no time scaling (normal speed).
     */
    public static ActiveMovableEffect createEffect(int effectIndex, Location location,
                                                   int duration, boolean infinite) {
        return createEffect(effectIndex, location, duration, infinite, null);
    }

    /**
     * Create a movable effect whose playback speed is continuously driven by
     * the time multiplier at {@code entity}'s position.
     *
     * @param entity Entity whose position is used for the time lookup.
     *               Pass {@code null} to get the same behaviour as the
     *               no-entity overload.
     */
    public static ActiveMovableEffect createEffect(int effectIndex, Location location,
                                                   int duration, boolean infinite,
                                                   LivingEntity entity) {
        ActiveMovableEffect effect = switch (effectIndex) {
            case 0 -> new HorrorAuraEffect(location, duration, infinite);
            case 1 -> new LifeAuraEffect(location, duration, infinite);
            default -> throw new IllegalArgumentException("Unknown movable effect index: " + effectIndex);
        };

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Vec3 pos = location.getPosition();
            effect.setTimeMultiplier(
                    () -> AbilityUtil.getTimeInArea(entity,
                            new Location(new Vec3(pos.x, pos.y, pos.z), level))
            );
        }

        return effect;
    }
}