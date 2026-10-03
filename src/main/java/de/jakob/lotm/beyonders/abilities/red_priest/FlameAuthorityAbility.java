package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.projectiles.SpearOfDestructionProjectileEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
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
        super(id, 12f, "burning");
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

        PacketHandler.sendToNearbyPlayers(
                new PlayPhotonBlockEffectPacket("flamevortex", BlockPos.containing(startPos), 0, 0, 0, .1, null, -1, true, false),
                serverLevel,
                startPos,
                512
        );

        ServerScheduler.scheduleForDuration(0, 5, 20 * 12,
                () -> AbilityUtil.damageNearbyEntities(serverLevel, entity, 10, DamageLookup.lookupDps(1, 0.95, 5, 20) *multiplier(entity), startPos, true, false, true, 10, 20 * 6),
                null,
                serverLevel,
                () -> AbilityUtil.getTimeInArea(entity, new Location(startPos, serverLevel)));
    }

    private void inferno(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = AbilityUtil.getTargetLocation(entity, (int) (120*multiplier(entity)), 2);

        // Sound
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, entity.getSoundSource(), 10.0f, 1.0f);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRECHARGE_USE, entity.getSoundSource(), 10.0f, 1.0f);
        ServerScheduler.scheduleForDuration(0, 5, 20 * 7, () -> serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 10.0f, random.nextFloat()));

        PacketHandler.sendToNearbyPlayers(
                new PlayPhotonBlockEffectPacket("inferno", BlockPos.containing(pos), 0, 0, 0, .2, null, -1, true, false),
                serverLevel,
                pos,
                512
        );

        // Damage
        ServerScheduler.scheduleForDuration(
                0, 5, 20 * 7,
                () -> AbilityUtil.damageNearbyEntities(serverLevel, entity, 17, DamageLookup.lookupDps(1, .85, 5, 20) *multiplier(entity), pos, true, false, true, 10, 20 * 6),
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

        SpearOfDestructionProjectileEntity spear = new SpearOfDestructionProjectileEntity(serverLevel, entity, DamageLookup.lookupDamage(1, 0.9) *multiplier(entity), BeyonderData.isGriefingEnabled(entity));
        spear.setPos(startPos.x, startPos.y, startPos.z);
        spear.shoot(direction.x, direction.y, direction.z, 6, 0);
        serverLevel.addFreshEntity(spear);
    }
}
