package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.rendering.effectRendering.DirectionalEffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.util.data.Location;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class HangedRenderEffectUtil {
    private HangedRenderEffectUtil() {
    }

    public static void playShadowBlade(ServerLevel level, LivingEntity anchor, Vec3 start, Vec3 end, int durationTicks) {
        DirectionalEffectManager.playEffect(
                DirectionalEffectManager.DirectionalEffect.SHADOW_BLADE,
                start.x, start.y, start.z,
                end.x, end.y, end.z,
                durationTicks,
                level,
                anchor
        );
    }

    public static void playBurst(EffectManager.Effect effect, ServerLevel level, Vec3 center) {
        EffectManager.playEffect(effect, center.x, center.y, center.z, level);
    }

    public static void playBurst(EffectManager.Effect effect, ServerLevel level, Vec3 center, LivingEntity anchor) {
        EffectManager.playEffect(effect, center.x, center.y, center.z, level, anchor);
    }

    public static void playMovable(MovableEffectManager.MovableEffect effect, ServerLevel level, LivingEntity anchor,
                                   int durationTicks, boolean infinite) {
        MovableEffectManager.playEffect(
                effect,
                new Location(anchor.position(), level),
                durationTicks,
                infinite,
                level,
                anchor
        );
    }

    public static void playMovableAt(MovableEffectManager.MovableEffect effect, ServerLevel level, LivingEntity target,
                                     int durationTicks, boolean infinite) {
        MovableEffectManager.playEffect(
                effect,
                new Location(target.position(), level),
                durationTicks,
                infinite,
                level,
                target
        );
    }
}
