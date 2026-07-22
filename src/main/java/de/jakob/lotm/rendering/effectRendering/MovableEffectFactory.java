package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.rendering.effectRendering.impl.*;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;

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
        if (entity != null) {
            location = new EntityLocation(entity);
        }

        ActiveMovableEffect effect = switch (effectIndex) {
            case 0 -> new HorrorAuraEffect(location, duration, infinite);
            case 1 -> new LifeAuraEffect(location, duration, infinite);
            case 2 -> new FearAuraEffect(location, duration, infinite);
            case 3 -> new BeamsOfLightEffect(location, duration);
            case 4 -> new SpaceTearEffect(location, duration, infinite);
            case 5 -> new SefirahSkyBeamEffect(location, duration, infinite);
            case 6 -> new RiverSkyBeamEffect(location, duration, infinite);
            default -> throw new IllegalArgumentException("Unknown movable effect index: " + effectIndex);
        };

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && entity != null) {
            // Use the live entity reference in the lambda too, not a snapshot Vec3.
            effect.setTimeMultiplier(
                    () -> AbilityUtil.getTimeInArea(entity,
                            new Location(entity.position(), level))
            );
        }

        return effect;
    }
}