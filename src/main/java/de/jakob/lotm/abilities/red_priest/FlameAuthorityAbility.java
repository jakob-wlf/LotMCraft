package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
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
import java.util.Map;

public class FlameAuthorityAbility extends SelectableAbility {
    public FlameAuthorityAbility(String id) {
        super(id, 8f, "burning");
        canBeCopied = false;
        canBeShared = false;
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


        ServerScheduler.scheduleForDuration(0, 5, (int) (20 * 6*multiplier(entity)),
                () -> AbilityUtil.damageNearbyEntities(serverLevel, entity, 9*multiplier(entity), DamageLookup.lookupDps(1, 0.4, 3, 9) *multiplier(entity), startPos, true, false, 20 * 40),
                null,
                serverLevel,
                () -> AbilityUtil.getTimeInArea(entity, new Location(startPos, serverLevel)));
    }

    private void inferno(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = AbilityUtil.getTargetLocation(entity, (int) (120*multiplier(entity)), 2);

        // Sound
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRECHARGE_USE, entity.getSoundSource(), 10.0f, 1.0f);
        ServerScheduler.scheduleForDuration(0, 5, (int) (20 * 4*multiplier(entity)), () -> serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, random.nextFloat()));

        // VFX
        EffectManager.playEffect(EffectManager.Effect.INFERNO, pos.x, pos.y, pos.z, serverLevel, entity);

        double multiplier = multiplier(entity);
        // Damage
        ServerScheduler.scheduleForDuration(
                0, 5, (int) (20 * 4*multiplier(entity)),
                () -> AbilityUtil.damageNearbyEntities(serverLevel, entity, 22.5*multiplier(entity), DamageLookup.lookupDps(1, 1, 3, 12) *multiplier(entity), pos, true, false, 20 * 40),
                null,
                serverLevel,
                () -> AbilityUtil.getTimeInArea(entity, new Location(pos, serverLevel)));
    }

    private void destructionSpear(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(4.5f, 8f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, (int) (120*multiplier(entity)), 1.4f).subtract(startPos).normalize();

        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, 1.0f);

        SpearOfDestructionProjectileEntity spear = new SpearOfDestructionProjectileEntity(serverLevel, entity, DamageLookup.lookupDamage(1, 0.8) *multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        //LOTMCraft.LOGGER.info("damage {} multiplier {}",DamageLookup.lookupDamage(1, 0.8),multiplier(entity));
        spear.setPos(startPos.x, startPos.y, startPos.z);
        spear.shoot(direction.x, direction.y, direction.z, 3f*multiplier(entity), 0);
        serverLevel.addFreshEntity(spear);
    }
}
