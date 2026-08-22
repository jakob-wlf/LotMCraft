package de.jakob.lotm.rendering.effectRendering;

import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class EffectFactory {

    public static ActiveEffect createEffect(int effectId, UUID id, double x, double y, double z,
                                            LivingEntity entity, boolean followEntity,
                                            EffectParams overrides) {
        ClientLevel level = Minecraft.getInstance().level;

        Location location = (followEntity && entity != null && level != null)
                ? new EntityLocation(entity)
                : new Location(new Vec3(x, y, z), level);

        ActiveEffect effect = EffectRegistry.create(effectId, location, overrides);
        effect.setId(id);

        if (level != null) {
            effect.setTimeMultiplier(
                    () -> AbilityUtil.getTimeInArea(entity, effect.getLocation())
            );
        }

        return effect;
    }
}