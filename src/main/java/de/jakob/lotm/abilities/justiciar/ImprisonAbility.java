package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ImprisonAbility extends Ability {

    public static final Set<UUID> IMPRISONED = ConcurrentHashMap.newKeySet();
    // Maps caster UUID -> [targetId, velTaskId, vfxTaskId, drainTaskId]
    private static final Map<UUID, UUID[]> IMPRISON_DATA = new ConcurrentHashMap<>();

    public ImprisonAbility(String id) {
        super(id, 3f, "imprison");
        interactionRadius = 15;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity caster) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        // Re-cast cancels current imprisonment
        if (IMPRISON_DATA.containsKey(caster.getUUID())) {
            cancelImprisonment(caster.getUUID());
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(caster, 15*(int) Math.max(multiplier(caster)/4,1), 1.4f);
        if (target == null) return;

        final UUID targetId = target.getUUID();
        IMPRISONED.add(targetId);

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 127, false, false));

        // Velocity zeroing + slowness refresh every 5 ticks
        UUID velTaskId = ServerScheduler.scheduleRepeating(0, 5, -1, () -> {
            Entity e = serverLevel.getEntity(targetId);
            if (!(e instanceof LivingEntity t) || !t.isAlive()) return;
            t.setDeltaMovement(Vec3.ZERO);
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 127, false, false));
            t.setOnGround(true);
            var pos = t.position();
            t.setDeltaMovement(new Vec3(0, 0, 0));
            t.hurtMarked = true;

            t.teleportTo(pos.x, pos.y, pos.z);
        }, serverLevel, () -> IMPRISONED.contains(targetId));

        // VFX every 100 ticks
        UUID vfxTaskId = ServerScheduler.scheduleRepeating(0, 100, -1, () -> {
            Entity e = serverLevel.getEntity(targetId);
            if (!(e instanceof LivingEntity t) || !t.isAlive()) return;
            EffectManager.playEffect(EffectManager.Effect.IMPRISON,
                    t.getX(), t.getY(), t.getZ(), serverLevel);
        }, serverLevel, () -> IMPRISONED.contains(targetId));

        // Spirituality drain every 80 ticks from caster
        UUID drainTaskId = ServerScheduler.scheduleRepeating(80, 80, -1, () -> {
            if (!IMPRISONED.contains(targetId) || !caster.isAlive()) {
                cancelImprisonment(caster.getUUID());
                return;
            }
            float current = BeyonderData.getSpirituality(caster);
            if (current < 300) {
                cancelImprisonment(caster.getUUID());
                return;
            }
            BeyonderData.incrementSpirituality(caster, -300);
        }, serverLevel, () -> IMPRISONED.contains(targetId) && caster.isAlive());

        IMPRISON_DATA.put(caster.getUUID(), new UUID[]{targetId, velTaskId, vfxTaskId, drainTaskId});

        EffectManager.playEffect(EffectManager.Effect.IMPRISON,
                target.getX(), target.getY(), target.getZ(), serverLevel);
    }

    private static void cancelImprisonment(UUID casterUUID) {
        UUID[] data = IMPRISON_DATA.remove(casterUUID);
        if (data == null) return;
        IMPRISONED.remove(data[0]);
        for (int i = 1; i < data.length; i++) {
            if (data[i] != null) ServerScheduler.cancel(data[i]);
        }
    }
}
