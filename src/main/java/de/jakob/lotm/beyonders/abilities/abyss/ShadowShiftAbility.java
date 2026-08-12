package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShadowShiftAbility extends Ability {

    private static final DustParticleOptions SHADOW_DUST =
            new DustParticleOptions(new Vector3f(0.05f, 0.0f, 0.08f), 2.0f);
    private static final DustParticleOptions WISP_DUST =
            new DustParticleOptions(new Vector3f(0.25f, 0.0f, 0.35f), 1.2f);

    private static final Map<UUID, UUID> casterToClone = new HashMap<>();

    public ShadowShiftAbility(String id) {
        super(id, 18f, "darkness");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 80;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 originPos  = entity.position();
        Vec3 lookDir    = entity.getLookAngle();
        Vec3 escapeDir  = lookDir.scale(-1).normalize();
        double escapeDistance = 10 + random.nextDouble() * 4;
        Vec3 escapePos  = originPos.add(escapeDir.scale(escapeDistance));

        String pathway   = BeyonderData.getPathway(entity);
        int    sequence  = AbilityUtil.getSeqWithArt(entity, this);

        AvatarEntity clone = new AvatarEntity(ModEntities.AVATAR.get(), serverLevel,
                entity.getUUID(), pathway, sequence);
        clone.setPos(originPos.x, originPos.y, originPos.z);
        serverLevel.addFreshEntity(clone);
        casterToClone.put(entity.getUUID(), clone.getUUID());

        AbilityUtil.getNearbyEntities(entity, serverLevel, originPos, 24).forEach(e -> {
            if (e instanceof net.minecraft.world.entity.Mob mob
                    && mob.getTarget() != null
                    && mob.getTarget().getUUID().equals(entity.getUUID())) {
                mob.setTarget(clone);
            }
        });

        ParticleUtil.spawnParticles(serverLevel, SHADOW_DUST, originPos.add(0, 1, 0), 120, 0.8, 0.06);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, originPos.add(0, 1, 0), 80, 0.8, 0.05);
        serverLevel.playSound(null, originPos.x, originPos.y, originPos.z,
                SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.4f, 0.55f);

        entity.teleportTo(escapePos.x, escapePos.y, escapePos.z);
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 4 * (int) Math.max(multiplier(entity) / 2, 1), 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 3, 3, false, false, false));

        ParticleUtil.spawnParticles(serverLevel, WISP_DUST, escapePos.add(0, 1, 0), 30, 0.4, 0.05);

        ServerScheduler.scheduleDelayed(20 * 3, () -> {
            if (!clone.isAlive()) return;

            Vec3 clonePos = clone.position();
            ParticleUtil.spawnSphereParticles(serverLevel, SHADOW_DUST, clonePos.add(0, 1, 0), 5, 200);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, clonePos.add(0, 1, 0), 120, 4, 0.05);
            serverLevel.playSound(null, clonePos.x, clonePos.y, clonePos.z,
                    SoundEvents.WITHER_AMBIENT, entity.getSoundSource(), 1.5f, 1.6f);

            AbilityUtil.getNearbyEntities(entity, serverLevel, clonePos, 8)
                    .stream()
                    .filter(t -> AbilityUtil.mayDamage(entity, t))
                    .forEach(t -> {
                        t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,    20 * 3, 0, false, false));
                        t.addEffect(new MobEffectInstance(MobEffects.CONFUSION,    20 * 4, 0, false, false));
                        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 3, 2, false, false));
                    });

            clone.discard();
            casterToClone.remove(entity.getUUID());
        }, serverLevel);

        AbilityUtil.sendActionBar(entity,
                Component.translatable("ability.lotmcraft.shadow_shift.activated")
                        .withColor(0x1a0026));
    }
}