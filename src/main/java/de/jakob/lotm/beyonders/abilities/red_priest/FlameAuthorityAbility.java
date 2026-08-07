package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.projectiles.SpearOfDestructionProjectileEntity;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FlameAuthorityAbility extends SelectableAbility {
    public FlameAuthorityAbility(String id) {
        super(id, 8f, "burning");
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(3, 5));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(6800f, 40000f));

        baseDamage = 40;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1800;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.flame_authority.destruction_spear", "ability.lotmcraft.flame_authority.inferno", "ability.lotmcraft.flame_authority.vortex"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
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

        EffectManager.playEffect(EffectManager.Effect.FLAME_VORTEX, entity.getX(), entity.getY(), entity.getZ(), serverLevel, entity);
        ParticleUtil.createParticleSpirals(serverLevel, ParticleTypes.FLAME, startPos, 1.5, 6, 5, .75, 1, 20 * 6, 120, 1);
        ParticleUtil.createParticleSpirals(serverLevel, ModParticles.PURPLE_FLAME.get(), startPos, 1.5, 6, 5, .75, 1, 20 * 6, 120, 1);

        float damage = baseDamage/4;

        ServerScheduler.scheduleForDuration(0, 5, (int) (20 * 6),
                () -> AbilityUtil.damageNearbyEntities(serverLevel, entity, 15, ModDamageTypes.SOUL_FIRE,damage, startPos, true, false, 20 * 40),
                null,
                serverLevel,
                () -> AbilityUtil.getTimeInArea(entity, new Location(startPos, serverLevel)));
    }

    private void inferno(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = AbilityUtil.getTargetLocation(entity, baseDistance, 2);

        // Sound
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRECHARGE_USE, entity.getSoundSource(), 10.0f, 1.0f);
        ServerScheduler.scheduleForDuration(0, 5, (int) (20 * 4), () -> serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, random.nextFloat()));

        // VFX
        EffectManager.playEffect(EffectManager.Effect.INFERNO, pos.x, pos.y, pos.z, serverLevel, entity);

        //Damage
        double multiplier = multiplier(entity);
        float damage = baseDamage/4;

        ServerScheduler.scheduleForDuration(
                0, 5, (int) (20 * 4),
                () -> AbilityUtil.damageNearbyEntities(serverLevel, entity, 22.5, ModDamageTypes.SOUL_FIRE,damage, pos, true, false, 20 * 40),
                null,
                serverLevel,
                () -> AbilityUtil.getTimeInArea(entity, new Location(pos, serverLevel)));
    }

    private void destructionSpear(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(4.5f, 8f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, baseDistance, 1.4f).subtract(startPos).normalize();

        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);

        float damage = baseDamage;

        SpearOfDestructionProjectileEntity spear = new SpearOfDestructionProjectileEntity(serverLevel, entity, damage, BeyonderData.isGriefingEnabled(entity));

        spear.setPos(startPos.x, startPos.y, startPos.z);
        spear.shoot(direction.x, direction.y, direction.z, 3f, 0);
        serverLevel.addFreshEntity(spear);
    }
}
