package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepInducementAbility extends AbilityItem {
    public SleepInducementAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 90;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 80, 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        // Add sleep effect
        target.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 20 * 12, 1, false, false, false));

        // Animate particle line from caster to target
        for(int i = 0; i < 4; i++) {
            Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(),  .5, random.nextDouble(-4, 4), random.nextDouble(-1, 1));
            Vec3 targetLoc = target.getEyePosition();

            final float step = .5f;
            final float length = (float) startPos.distanceTo(targetLoc);
            final int duration = (int) Math.ceil(length / step) + 20 * 3;

            animateParticleLine(new Location(startPos, level), targetLoc, 6, duration);
        }
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    private void animateParticleLine(Location startLoc, Vec3 end, int step, int duration) {
        if(!(startLoc.getLevel() instanceof ServerLevel level))
            return;

        float distance = (float) end.distanceTo(startLoc.getPosition());
        float bezierSteps = .5f / distance;

        int maxPoints = Math.max(2, Math.min(10, (int) Math.ceil(distance * 1.5)));

        List<Vec3> points = VectorUtil.createBezierCurve(startLoc.getPosition(), end, bezierSteps, random.nextInt(1, maxPoints + 1));

        for(int k = 0; k < duration; k++) {
            for(int i = 0; i < Math.min(k, points.size() - step); i+=step) {
                for(int j = 0; j < step; j++) {
                    ParticleUtil.spawnParticles(level, dust, points.get(i + j), 1, 0, 0);
                }
            }
        }
    }
}
