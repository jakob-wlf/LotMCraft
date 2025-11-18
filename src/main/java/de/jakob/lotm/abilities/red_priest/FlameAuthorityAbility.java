package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.entity.custom.SpearOfDestructionProjectileEntity;
import de.jakob.lotm.entity.custom.SpearOfLightProjectileEntity;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class FlameAuthorityAbility extends SelectableAbilityItem {
    public FlameAuthorityAbility(Properties properties) {
        super(properties, 2.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.flame_authority.destruction_spear", "ability.lotmcraft.flame_authority.inferno", "ability.lotmcraft.flame_authority.vortex"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (abilityIndex) {
            case 0 -> destructionSpear(serverLevel, entity);
            case 1 -> inferno(serverLevel, entity);
            case 2 -> vortex(serverLevel, entity);
        }
    }

    private void vortex(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 startPos = entity.position();

        EffectManager.playEffect(EffectManager.Effect.FLAME_VORTEX, entity.getX(), entity.getY(), entity.getZ(), serverLevel);
        ParticleUtil.createParticleSpirals(serverLevel, ParticleTypes.FLAME, startPos, 1.5, 6, 5, .75, 1, 20 * 6, 120, 1);
        ParticleUtil.createParticleSpirals(serverLevel, ModParticles.PURPLE_FLAME.get(), startPos, 1.5, 6, 5, .75, 1, 20 * 6, 120, 1);

        ServerScheduler.scheduleForDuration(0, 5, 20 * 6, () -> AbilityUtil.damageNearbyEntities(serverLevel, entity, 9, DamageLookup.lookupDps(1, 1, 5, 20) * multiplier(entity), startPos, true, false, 20 * 40));
    }

    private void inferno(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = AbilityUtil.getTargetLocation(entity, 120, 2);

        // Sound
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRECHARGE_USE, entity.getSoundSource(), 10.0f, 1.0f);
        ServerScheduler.scheduleForDuration(0, 5, 20 * 4, () -> serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, random.nextFloat()));

        // VFX
        EffectManager.playEffect(EffectManager.Effect.INFERNO, pos.x, pos.y, pos.z, serverLevel);

        // Damage
        ServerScheduler.scheduleForDuration(0, 5, 20 * 4, () -> AbilityUtil.damageNearbyEntities(serverLevel, entity, 22.5, DamageLookup.lookupDps(1, .8, 5, 20) * multiplier(entity), pos, true, false, 20 * 40));
    }

    private void destructionSpear(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(4.5f, 8f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, 120, 1.4f).subtract(startPos).normalize();

        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);

        SpearOfDestructionProjectileEntity spear = new SpearOfDestructionProjectileEntity(serverLevel, entity, DamageLookup.lookupDamage(1, .8) * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        spear.setPos(startPos.x, startPos.y, startPos.z);
        spear.shoot(direction.x, direction.y, direction.z, 3f, 0);
        serverLevel.addFreshEntity(spear);
    }
}
